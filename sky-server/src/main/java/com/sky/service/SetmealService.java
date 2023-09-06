package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    void saveWithDish(SetmealDTO setmealDTO);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void deleteBatch(List<Long> ids);

    void onOrClose(Long id, Integer status);

    void update(SetmealDTO setmealDTO);

    SetmealVO getByIdWithDish(Long id);

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}
