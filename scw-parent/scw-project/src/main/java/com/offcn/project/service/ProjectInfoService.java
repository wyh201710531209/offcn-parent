package com.offcn.project.service;

import com.offcn.project.po.*;

import java.util.List;

public interface ProjectInfoService {
//    根据项目的id查询回报列表
    List<TReturn> getReturnList(Integer projectId);

    /**
     * 获取系统中所有项目
     * @return
     */
    List<TProject> findAllProject();

    /**
     * 获取项目图片
     * @param id
     * @return
     */
    List<TProjectImages> getProjectImages(Integer id);
    /**
     * 获取项目信息
     * @param projectId
     * @return
     */
    TProject findProjectInfo(Integer projectId);
    List<TTag> findAllTag();
    /**
     * 获取项目分类
     * @return
     */
    List<TType> findAllType();
    /**
     * 获取回报信息
     * @param returnId
     * @return
     */
    TReturn findReturnInfo (Integer returnId);
}
