package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 创建订单
     *
     * @param orders
     * @return
     */
    void insert(Orders orders);


    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);


    /**
     * 历史订单分页查询 需要查询 订单and订单详情表
     *
     * @param ordersPageQueryDTO
     * @return
     */
    Page<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id};")
    Orders getById(Long id);

    List<Orders> list();

    @Select("select * from orders where status = #{status} and order_time < #{localDateTime}")
    List<Orders> getByStatusAndOrderTimeLt(Integer status, LocalDateTime localDateTime);

    /**
     * 统计营业额
     *
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 统计订单数量
     *
     * @param map
     */
    Integer countByMap(Map map);
}
