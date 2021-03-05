package com.offcn.order.service;

import com.offcn.order.po.TOrder;
import com.offcn.order.vo.req.OrderInfoSubmitVo;

public interface OrderService {
   TOrder saveOrder(OrderInfoSubmitVo vo);
}
