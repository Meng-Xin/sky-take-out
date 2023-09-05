package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询对应套餐id
     * @param dishIds
     * @return
     */
    // select setmeal_id from setmeal_dish where dish_id in (1,2,3,4)
    List<Long> getSetmealIdByDishIds(List<Long> dishIds);

    /**
     * 批量保存套餐的餐品信息 中间表
     * @param setmealDishs
     */
    void insertBatch(List<SetmealDish> setmealDishs);

    /**
     * 根据套餐Id,删除套餐-餐品中间表信息
     * @param setmealId
     */
    void deleteBySetmealId(Long setmealId);
}
