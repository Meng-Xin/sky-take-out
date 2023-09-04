package com.sky.service.impl;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 新增套餐接口实现类
     * @param setmealDTO
     */
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        // 新增套餐的同时需要保存多对多关系。 套餐 ： 餐品 -> N : N
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        // 1.向套餐表插入当前数据
        setmealMapper.insert(setmeal);
        
        // 插入完成后获取对应id
        Long setmealId = setmeal.getId();

        // 2.保存套餐和餐品的对应关系
        List<SetmealDish> setmealDishs = setmealDTO.getSetmealDishes();
        if (setmealDishs != null && setmealDishs.size() > 0){
            // 便利数组，将餐品id添加到中间表结构中
            setmealDishs.forEach(setmealDish ->{
                setmealDish.setSetmealId(setmealId);
            });
            // 将完整数据保存到中间表中
            setmealDishMapper.insertBatch(setmealDishs);
        }

    }
}
