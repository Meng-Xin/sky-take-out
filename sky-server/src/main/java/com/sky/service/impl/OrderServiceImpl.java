package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import jdk.jpackage.internal.Log;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

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
        BigDecimal sum = carts.stream().map(item -> item.getAmount()).reduce(BigDecimal::add).get();
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


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 历史订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO){
        // 使用工具函数查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.pageQuery(ordersPageQueryDTO);

        // 构造详情实体类 TODO 不清楚mybitys 中如何给不同的多表连接对象赋值，所以拆开查。
        if (page != null || page.size() > 0){
            page.forEach(order ->{
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(order.getId());
                order.setOrderDetailList(orderDetails);
            });
        }

        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 查询订单详情
     */
    public OrderVO getByid(Long id){
        // 1.获取订单数据
        Orders orders = orderMapper.getById(id);
        // 2.获取订单详情数据
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        // 3.组装数据返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 取消订单
     */
    public void cancel(Orders order){
        // 1.将原有订单数据状态更改为取消。这里直接模拟退款成功
        order.setStatus(Orders.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setPayStatus(Orders.REFUND);
        orderMapper.update(order);
    }

    /**
     * 再来一单
     */
    public void repetOrder(Long id){
        // 1.查询获取原有订单数据
        Orders sourceOrder = orderMapper.getById(id);
        // 2.构造新的订单数进行下单，新的订单创建时间、……
        sourceOrder.setStatus(sourceOrder.PENDING_PAYMENT);                 //设置订单初始状态
        sourceOrder.setOrderTime(LocalDateTime.now());                      //设置订单创建时间
        sourceOrder.setPayStatus(sourceOrder.UN_PAID);                      //设置订单初始支付状态
        sourceOrder.setNumber(String.valueOf(System.currentTimeMillis()));   //设置订单号
        orderMapper.insert(sourceOrder);
        // 3.关联插入订单详情数据
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        orderDetailMapper.insertBatch(orderDetailList);
    }

    /**
     * 催单 TODO 暂无消息中心，无法进行消息通知。
     */
    public void reminder(Long id){
        Log.info("暂无消息中心，无法进行催单通知。");
    }
}
