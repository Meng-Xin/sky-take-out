package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCaches", key = "#setmealDTO.categoryId") // key: setmealCaches::100
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐：{}",setmealDTO);
        // 1.新增套餐接口
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询：{}",setmealPageQueryDTO);
        // 1.进行分页查询
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping()
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmealCaches",allEntries = true) // key setmealCaches::*
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除套餐: {}",ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售、停售")
    @CacheEvict(cacheNames = "setmealCaches",allEntries = true) // key setmealCaches::*
    public Result onOrClose(@RequestParam Long id,@PathVariable Integer status){
        log.info("套餐起售、停售：id:{},status:{}",id,status);
        setmealService.onOrClose(id,status);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("根据id查询套餐信息：{}",id);
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    @PutMapping()
    @ApiOperation("修改套餐信息")
    @CacheEvict(cacheNames = "setmealCaches",allEntries = true) // key setmealCaches::*
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐信息：{}",setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }
}
