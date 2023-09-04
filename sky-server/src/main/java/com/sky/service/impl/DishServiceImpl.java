package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    @Transactional // 事务注解
    public void saveWithFlavor(DishDTO dishDTO) {
        // 新增菜品的同时需要插入口味数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        // 向菜品表插入1条数据
        dishMapper.insert(dish);

        // 获取insert语句生成的主键值 需要在xml中配置：useGeneratedKeys="true" keyProperty="id"
        Long dishId = dish.getId();

        // 向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0){
            // 遍历数组，并把获取到的dishId添加进去。
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });


            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
