package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 用户提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // 订单逻辑校验 地址为空、购物车为空。
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        if (addressBookId == null){
            // 地址为空异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        if (addressBook == null ){
            // 地址为空异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        // 查询用户购物车数据
        ShoppingCart shopingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> carts = shoppingCartMapper.list(shopingCart);
        if (carts == null || carts.size() ==0){
            // 购物车为空异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 构造订单数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,order);
        order.setStatus(order.PENDING_PAYMENT);   //设置订单初始状态
        order.setOrderTime(LocalDateTime.now());   //设置订单创建时间
        order.setPayStatus(order.UN_PAID);        //设置订单初始支付状态
        order.setNumber(String.valueOf(System.currentTimeMillis()));   //设置订单号
        order.setPhone(addressBook.getPhone());    //设置订单手机号（冗余）
        order.setConsignee(addressBook.getConsignee());//设置订单收货人（冗余）
        order.setUserId(userId);                   //设置订单的userid
        // 1.向订单表插入一条数据
        orderMapper.insert(order);
        // 2.向订单明细表插入多条数据
        // 构造订单明细实体类列表 TODO 校验总金额
        List<OrderDetail> orderDetails = new ArrayList<>();
        carts.forEach(cart ->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(order.getId());
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetails.add(orderDetail);
        });
        orderDetailMapper.insertBatch(orderDetails);
        // 3.删除用户原有购物车数据
        shoppingCartMapper.deleteByUserId(userId);
        // 4.返回Vo数据
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();
        return orderSubmitVO;
    }
}
