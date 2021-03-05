package com.offcn.order.service.impl;

import com.offcn.dycommon.response.AppResponse;
import com.offcn.order.service.ProjectServiceFegin;
import com.offcn.order.vo.resp.TReturn;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class ProjectServiceFeignException implements ProjectServiceFegin {
    @Override
    public AppResponse<List<TReturn>> getReturnList(Integer projectId) {
        AppResponse<List<TReturn>> fail=AppResponse.fail(null);
        fail.setMsg("远程调用失败【订单模块】");
        return fail;
    }
}
