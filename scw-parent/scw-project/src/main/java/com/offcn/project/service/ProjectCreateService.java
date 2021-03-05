package com.offcn.project.service;

import com.offcn.dycommon.enums.ProjectStatusEnume;
import com.offcn.project.vo.req.ProjectRedisStorageVo;

public interface ProjectCreateService {

    public String initCreateProject(Integer memberId);
    void saveProjectInfo(ProjectStatusEnume auth, ProjectRedisStorageVo project);

}
