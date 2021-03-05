package com.offcn.order.service.impl;

import com.offcn.dycommon.response.AppResponse;
import com.offcn.order.mapper.TOrderMapper;
import com.offcn.order.po.TOrder;
import com.offcn.order.service.OrderService;
import com.offcn.order.service.ProjectServiceFegin;
import com.offcn.order.vo.req.OrderInfoSubmitVo;
import com.offcn.order.vo.resp.TReturn;
import com.offcn.utils.AppDateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.offcn.dycommon.enums.OrderStatusEnumes;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl  implements OrderService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ProjectServiceFegin projectServiceFegin;
    @Autowired
    private TOrderMapper orderMapper;
    @Override
    public TOrder saveOrder(OrderInfoSubmitVo vo) {
//        创建订单对象
        TOrder order=new TOrder();
//       通过令牌获取memberId
        String memberId = stringRedisTemplate.opsForValue().get(vo.getAccessToken());
//        设置订单的用户Id
        order.setMemberid(Integer.parseInt(memberId));
        BeanUtils.copyProperties(vo,order);
//        设置订单号
        String orderNum = UUID.randomUUID().toString().replace("-", "");
        order.setOrdernum(orderNum);
//        创建的时间
        order.setCreatedate(AppDateUtils.getFormaTime());
//        设置支付的状态
        order.setStatus(OrderStatusEnumes.UNPAY.getCode() + "");
//        设置发票的状态
        order.setInvoice(vo.getInvoice().toString());

//      远程调用
        AppResponse<List<TReturn>> response = projectServiceFegin.getReturnList(vo.getProjectid());
        List<TReturn> returnList = response.getData();
        TReturn myReturn=null;
        for (TReturn tReturn : returnList) {
            if(tReturn.getId().intValue()==vo.getReturnid().intValue()){
                myReturn=tReturn;
                break;
            }
        }
        //支持金额  回报数量*回报支持金额+运费
        Integer money = order.getRtncount() * myReturn.getSupportmoney() + myReturn.getFreight();
        order.setMoney(money);
        //4.执行保存操作
        orderMapper.insertSelective(order);
        return order;
    }
}
