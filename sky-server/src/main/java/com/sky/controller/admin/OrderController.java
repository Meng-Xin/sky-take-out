package com.sky.controller.admin;


import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "管理端-订单管理接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单分页搜索 TODO 没有定时任务模块，无法根据日期查询订单信息。
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("管理端订单模块-订单分页搜索")
    public Result<PageResult>conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单分页搜索：{}",ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 各个状态的订单数量统计
     */
    @GetMapping("/statistics")
    @ApiOperation("管理端订单模块-各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics(){
        log.info("管理端-各个状态的订单数量统计：{}");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/details/{id}")
    @ApiOperation("管理端-查询订单详情")
    public Result<OrderVO> details(@PathVariable Long id){
        log.info("管理端订单模块-查询订单详情：{}",id);
        OrderVO orderVO = orderService.getByid(id);
        return Result.success(orderVO);
    }

    /**
     *  接单
     */
    @PutMapping("/confirm")
    @ApiOperation("管理端订单模块-接单")
    public Result<OrderVO> confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("管理端-接单：{}",ordersConfirmDTO);
        OrderVO orderVO = orderService.confirm(ordersConfirmDTO);
        return Result.success(orderVO);
    }

    /**
     * 拒单
     */
    @PutMapping("/rejection")
    @ApiOperation("管理端订单模块-拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("管理端订单模块-拒单：{}",ordersRejectionDTO);

        return Result.success();
    }
}
