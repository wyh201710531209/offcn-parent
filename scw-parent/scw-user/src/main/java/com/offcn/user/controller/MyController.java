package com.offcn.user.controller;

import com.offcn.user.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags="此处是测试的控制层---swagger测试")
@RestController
public class MyController {
    @ApiOperation(value="这是一个hello的方法")
   @ApiImplicitParams(value = {
           @ApiImplicitParam(value = "姓名",name = "name",required = true),
           @ApiImplicitParam(value = "年龄",name = "age")
   })
    @GetMapping("/hello")
    public String hello(String name){
          return "success:"+ name;
    }
    @ApiOperation(value="这是一个保存用户方法")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "姓名",name = "name",required = true),
            @ApiImplicitParam(value = "邮箱",name = "email")
    })
    @GetMapping("/save")
    public User saveUser(String name,String email){
       User user = new User();
       user.setName(name);
       user.setEmail(email);
       return user;
    }
}
