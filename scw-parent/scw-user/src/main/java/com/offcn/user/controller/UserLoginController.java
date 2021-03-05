package com.offcn.user.controller;

import com.netflix.discovery.converters.Auto;
import com.offcn.dycommon.response.AppResponse;
import com.offcn.user.componet.SmsTemplate;
import com.offcn.user.po.TMember;
import com.offcn.user.service.UserService;
import com.offcn.user.vo.req.UserRegistVo;
import com.offcn.user.vo.resp.UserRespVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Api(tags = "用户登录/注册")
@Slf4j
public class UserLoginController {
    @Autowired
    private SmsTemplate smsTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserService userService;

    @ApiOperation(value="获取短信验证码")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "手机号码",name = "phoneNo",required = true)
    })
    @GetMapping("/sendCode")
    public AppResponse<Object> sendCode(String phoneNo){
        UUID uuid = UUID.randomUUID();
        String code = uuid.toString().substring(0, 6);
        stringRedisTemplate.opsForValue().set(phoneNo,code,60*60*24*7, TimeUnit.SECONDS);
        Map<String, String> querys = new HashMap<>();
        querys.put("mobile",phoneNo);
        querys.put("param","code:"+code);
        querys.put("tpl_id","TP1711063");
        //发短信
        String s = smsTemplate.sendCode(querys);
        if(s.equals("fail")){
            return AppResponse.fail("短信发送失败");
        }
        return AppResponse.ok(s);

    }

    @ApiOperation("用户注册")
    @PostMapping("/regist")
    public AppResponse<Object> regist(UserRegistVo registVo) {
        //1、校验验证码
        String code = stringRedisTemplate.opsForValue().get(registVo.getLoginacct());
        if (!StringUtils.isEmpty(code)) {
            //redis中有验证码
            boolean b = code.equalsIgnoreCase(registVo.getCode());
            if (b) {
                //2、将vo转业务能用的数据对象
                TMember member = new TMember();
                BeanUtils.copyProperties(registVo, member);
                //3、将用户信息注册到数据库
                try {
                    userService.registUser(member);
                    log.debug("用户信息注册成功：{}", member.getLoginacct());
                    //4、注册成功后，删除验证码
                    stringRedisTemplate.delete(registVo.getLoginacct());
                    return AppResponse.ok("注册成功...");
                } catch (Exception e) {
                    log.error("用户信息注册失败：{}", member.getLoginacct());
                    return AppResponse.fail(e.getMessage());
                }
            } else {
                return AppResponse.fail("验证码错误");
            }
        } else {
            return AppResponse.fail("验证码过期，请重新获取");
        }
    }
    @ApiOperation(value="用户登录")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "用户名",name = "username",required = true),
            @ApiImplicitParam(value = "密码",name = "password",required = true)
    })
    @GetMapping("/login")
    public AppResponse<UserRespVo> login(String username,String password){
        TMember member = userService.login(username, password);
        if(member == null){ //登录不成功
            AppResponse<UserRespVo> fail = AppResponse.fail(null);
            fail.setMsg("用户名或者密码错误");

            return fail;
        }
        //登录成功
        //发放令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        System.out.println(token);
        UserRespVo userRespVo = new UserRespVo();
        userRespVo.setAccessToken(token);
        BeanUtils.copyProperties(member,userRespVo);
        //令牌存入redis
        stringRedisTemplate.opsForValue().set(token,member.getId()+"",10,TimeUnit.DAYS);
        return AppResponse.ok(userRespVo);
    }

    @ApiOperation(value="根据用户id查询")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "用户id",name = "id",required = true)
    })
    @GetMapping("/findUser/{id}")
    public AppResponse<UserRespVo> findUser(@PathVariable("id") Integer id){
        TMember member = userService.findMemberById(id);
        UserRespVo userRespVo = new UserRespVo();
        BeanUtils.copyProperties(member,userRespVo);
       return AppResponse.ok(userRespVo);

    }
}
