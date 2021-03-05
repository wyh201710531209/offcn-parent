package com.offcn.project.controller;

import com.alibaba.fastjson.JSON;
import com.offcn.dycommon.enums.ProjectStatusEnume;
import com.offcn.dycommon.response.AppResponse;
import com.offcn.project.contants.ProjectContant;
import com.offcn.project.po.*;
import com.offcn.project.service.ProjectCreateService;
import com.offcn.project.service.ProjectInfoService;
import com.offcn.project.vo.BaseVo;
import com.offcn.project.vo.req.ProjectBaseInfoVo;
import com.offcn.project.vo.req.ProjectRedisStorageVo;
import com.offcn.project.vo.req.ProjectReturnVo;
import com.offcn.project.vo.resp.ProjectDetailVo;
import com.offcn.project.vo.resp.ProjectVo;
import com.offcn.utils.OSSTemplate;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Api(tags = "项目模块（文件上传,项目的初始化）")
@Slf4j
@RestController
@RequestMapping("/project")
public class ProjectController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ProjectCreateService projectCreateService;
    @Autowired
    private OSSTemplate ossTemplate;
    @Autowired
    private ProjectInfoService projectInfoService;
    @ApiOperation(value="文件上传")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "文件地址",name = "files",required = true)
    })
    @PostMapping("/upload")
    public AppResponse upload(@RequestParam("file") MultipartFile[] files) throws IOException {
        ArrayList<String> list = new ArrayList<>();

        if(files != null && files.length > 0){
            for (MultipartFile file : files) {
                if(!file.isEmpty()){
                    String upload = ossTemplate.upload(file.getInputStream(), file.getOriginalFilename());
                   list.add(upload);


                }
            }
        }
        HashMap<Object, Object> map = new HashMap<>();
        map.put("urls",list);
        log.debug("oss的信息：{},图片地址：{}",ossTemplate,list);
        return AppResponse.ok(map);
    }
    @ApiOperation(value="项目的第一步：阅读协议完成项目的初始化")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(value = "会员的令牌",name = "accessToken",required = true)
    })
    @PostMapping("/init")
    public AppResponse<String> init(BaseVo vo){
        //获取用户令牌
        String accessToken = vo.getAccessToken();
        //去redis里根据用户的令牌获取用户的id
        String memberId = stringRedisTemplate.opsForValue().get(accessToken);
        if(StringUtils.isEmpty(memberId)){
            //未登录
            return AppResponse.fail("未登录");
        }
        int id = Integer.parseInt(memberId);
        String projectToken = projectCreateService.initCreateProject(id);
        return AppResponse.ok(projectToken);

    }
    @ApiOperation(value="项目的第二步：保存项目的基本信息")

    @PostMapping("/savebaseVo")
    //保存项目的基本信息
    public AppResponse<String> savebaseInfo(ProjectBaseInfoVo vo){
        //根据项目令牌查找临时对象
        String s = stringRedisTemplate.opsForValue().get(ProjectContant.TEMP_PROJECT_PREFIX + vo.getProjectToken());
        //把json字符串转化为存储的临时对象
        ProjectRedisStorageVo projectRedisStorageVo = JSON.parseObject(s, ProjectRedisStorageVo.class);
        BeanUtils.copyProperties(vo,projectRedisStorageVo);//完成临时对象的赋值
        //临时对象存入redis
        String jsonString = JSON.toJSONString(projectRedisStorageVo);
        stringRedisTemplate.opsForValue().set(ProjectContant.TEMP_PROJECT_PREFIX + vo.getProjectToken(),jsonString);
        return AppResponse.ok(vo.getProjectToken());
    }
    @ApiOperation(value="项目的第三步：保存项目的回报信息")

    @PostMapping("/savereturn")
    public AppResponse<String> saveReturnInFo(@RequestBody List<ProjectReturnVo> pro) {
        ProjectReturnVo projectReturnVo = pro.get(0);
        String projectToken = projectReturnVo.getProjectToken();
//        从redis获取临时对象
        //1、取得redis中之前存储JSON结构的项目信息
        String projectContext = stringRedisTemplate.opsForValue().get(ProjectContant.TEMP_PROJECT_PREFIX + projectToken);
        //2、转换为redis存储对应的vo
        ProjectRedisStorageVo storageVo = JSON.parseObject(projectContext, ProjectRedisStorageVo.class);
        //3、将页面收集来的回报数据封装重新放入redis
        List<TReturn> returns = new ArrayList<>();

        for (ProjectReturnVo projectReturnVo1 : pro) {
            TReturn tReturn = new TReturn();
            BeanUtils.copyProperties(projectReturnVo1, tReturn);
            returns.add(tReturn);
        }
        //4、更新return集合
        storageVo.setProjectReturns(returns);
        String jsonString = JSON.toJSONString(storageVo);
        //5、重新更新到redis
        stringRedisTemplate.opsForValue().set(ProjectContant.TEMP_PROJECT_PREFIX + projectToken, jsonString);
        return AppResponse.ok("OK");
    }
    @ApiOperation("保存项目")
    @PostMapping("/save")
    //保存我们的项目
    public AppResponse<String> savaProjectInFo(String accessToken,String projectToken,String ops){
        //通过用户的令牌获取用户的id
        String memberId = stringRedisTemplate.opsForValue().get(accessToken);
        if(memberId==null){
            return AppResponse.fail("请登录");
        }//根据项目令牌，从redis获取临时对象
        String s = stringRedisTemplate.opsForValue().get(ProjectContant.TEMP_PROJECT_PREFIX + projectToken);
        ProjectRedisStorageVo projectRedisStorageVo = JSON.parseObject(s, ProjectRedisStorageVo.class);
        //判断ops 决定对项目如何操作 0--保存草稿 1--提交
        if(StringUtils.isNotEmpty(ops)){
              if(ops.equals("0")){
                  projectCreateService.saveProjectInfo(ProjectStatusEnume.DRAFT,projectRedisStorageVo);
                  return AppResponse.ok("保存草稿成功");
              }else if(ops.equals("1")){
                  projectCreateService.saveProjectInfo(ProjectStatusEnume.SUBMIT_AUTH,projectRedisStorageVo);
                  return AppResponse.ok("提交项目成功");
              }else {
                  return  AppResponse.fail("非法操作");
              }

        }
        return AppResponse.fail("操作失败");


    }
//    根据项目id查询回报列表
    @ApiOperation("得到回报列表")
    @GetMapping("details/returns/{projectId}")
    public AppResponse<List<TReturn>> getReturnList(@PathVariable("projectId") Integer projectId){
        List<TReturn> returnList = projectInfoService.getReturnList(projectId);
        return AppResponse.ok(returnList);
    }
    @ApiOperation("获取系统所有的项目")
    @GetMapping("/all")
    public AppResponse<List<ProjectVo>> findAllProject() {
        // 1、创建集合存储全部项目的VO
        List<ProjectVo> prosVo = new ArrayList<>();
        // 2、查询全部项目
        List<TProject> pros = projectInfoService. findAllProject();
        //3、遍历项目集合
        for (TProject tProject : pros) {
//获取项目编号
            Integer id = tProject.getId();
//根据项目编号获取项目配图
            List<TProjectImages> images = projectInfoService.getProjectImages(id);
            ProjectVo projectVo = new ProjectVo();
            BeanUtils.copyProperties(tProject, projectVo);
            //遍历项目配图集合
            for (TProjectImages tProjectImages : images) {
                //如果图片类型是头部图片，则设置头部图片路径到项目VO
                if (tProjectImages.getImgtype() == 0) {
                    projectVo.setHeaderImage(tProjectImages.getImgurl());
                }
            }
//把项目vo添加到项目vo集合
            prosVo.add(projectVo);
        }
        return AppResponse.ok(prosVo);
    }

    @ApiOperation("获取项目信息详情")
    @GetMapping("/findProjectInfo/{projectId}")
    public AppResponse<ProjectDetailVo> findProjectInfo(@PathVariable("projectId") Integer projectId) {
        TProject projectInfo = projectInfoService. findProjectInfo(projectId);
        ProjectDetailVo projectVo = new ProjectDetailVo();
        // 1、查出这个项目的所有图片
        List<TProjectImages> projectImages = projectInfoService.getProjectImages(projectInfo.getId());
        List<String> detailsImage = projectVo.getDetailsImage();
        if(detailsImage==null){
            detailsImage=new ArrayList<>();
        }
        for (TProjectImages tProjectImages : projectImages) {
            if (tProjectImages.getImgtype() == 0) {
                projectVo.setHeaderImage(tProjectImages.getImgurl());
            } else {
                detailsImage.add(tProjectImages.getImgurl());
            }
        }
        projectVo.setDetailsImage(detailsImage);

        // 2、项目的所有支持回报；
        List<TReturn> returns = projectInfoService.getReturnList(projectInfo.getId());
        projectVo.setProjectReturns(returns);
        BeanUtils.copyProperties(projectInfo, projectVo);
        return AppResponse.ok(projectVo);
    }
    @ApiOperation("获取系统所有的项目标签")
    @GetMapping("/findAllTag")
    public AppResponse<List<TTag>> findAllTag() {
        List<TTag> tags = projectInfoService. findAllTag ();
        return AppResponse.ok(tags);
    }
    @ApiOperation("获取系统所有的项目分类")
    @GetMapping("/findAllType")
    public AppResponse<List<TType>> findAllType() {
        List<TType> types = projectInfoService. findAllType();
        return AppResponse.ok(types);
    }
    @ApiOperation("获取回报信息")
    @GetMapping("/returns/info/{returnId}")
    public AppResponse<TReturn> findReturnInfo(@PathVariable("returnId") Integer returnId){
        TReturn tReturn = projectInfoService. findReturnInfo(returnId);
        return AppResponse.ok(tReturn);
    }


    }

