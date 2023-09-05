package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
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

    /**
     * 套餐页面的分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO){
        // 使用Spring工具进行分页查询
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        // 调用dao接口
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除套餐 删除套餐表的同时还需删除中间表记录的关联信息
     */
    @Transactional
    public void deleteBatch(List<Long> ids){
        // 当前套餐是否能够删除---当前套餐是否正在售卖中？
        for (Long id : ids){
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        // 套餐未售卖才可删除
        for (Long id : ids){
            // 1.删除套餐信息
            setmealMapper.deleteById(id);
            // 2.删除中间表记录的套餐关联信息
            setmealDishMapper.deleteBySetmealId(id);
        }
    }
}