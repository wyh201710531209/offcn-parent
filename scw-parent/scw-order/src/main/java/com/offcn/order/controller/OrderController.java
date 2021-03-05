package com.offcn.order.controller;

import com.offcn.dycommon.response.AppResponse;
import com.offcn.order.po.TOrder;
import com.offcn.order.service.OrderService;
import com.offcn.order.vo.req.OrderInfoSubmitVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags="保存订单")
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/createOrder")
    @ApiOperation("保存订单")
    public AppResponse<Object> createOrder(@RequestBody OrderInfoSubmitVo vo){
        String memberId = redisTemplate.opsForValue().get(vo.getAccessToken());
        if (memberId == null) {
            return AppResponse.fail("无此权限，请先登录");
        }
        try {
            TOrder order = orderService.saveOrder(vo);
            AppResponse orderAppResponse = AppResponse.ok(order);
            return orderAppResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return AppResponse.fail(null);
        }
    }
}