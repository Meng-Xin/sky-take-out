package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "用户端商店状态")
@Slf4j
public class ShopController {


    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 用户端查看商店状态
     */
    @GetMapping("/status")
    @ApiOperation("用户端获取营业状态")
    public Result<Integer> getStatus(){
        String statusStr = (String) redisTemplate.opsForValue().get("SHOP_STATUS");
        Integer status = Integer.parseInt(statusStr);
        log.info("当前商店营业状态：{}",status == 1 ? "营业中" : "打样中");
        return Result.success(status);
    }
}
