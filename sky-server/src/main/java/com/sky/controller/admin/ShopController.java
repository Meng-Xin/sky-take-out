package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@Api(tags = "商店营业状态")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {

    public static final  String Key = "SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 设置商店营业状态
     */
    @PutMapping("/{status}")
    @ApiOperation("设置商店营业状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置商店营业状态：{}",status == 1 ? "营业中":"打样中");
        redisTemplate.opsForValue().set(Key, status.toString());
        return Result.success();
    }

    /**
     * 获取商店营业状态
     */
    @GetMapping("/status")
    @ApiOperation("获取商店营业状态")
    public Result<Integer>getStatus(){
        String staStr = (String) redisTemplate.opsForValue().get(Key);
        Integer status = Integer.parseInt(staStr);
        log.info("获取商店营业状态：{}",status == 1 ? "营业中":"打样中");
        return Result.success(status);
    }
}
