package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {


    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 添加商品到购物车
     * @param shoppingCartDTO
     */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        // 1.判断当前商品是否在购物车中已存在  ok -> 对应商品数量+1 !ok -> 购物车表中插入一条新的数据。
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        // 从线程上下文中获取 uid
        Long currentId = BaseContext.getCurrentId();
        // 为当前shoppingCard对象赋值
        shoppingCart.setUserId(currentId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        // ok -> 对应商品数量+1
        if (list != null && list.size() > 0){
            // 获取购物车列表数据的第一条（符合动态筛选的也仅有一条）
            ShoppingCart cart = list.get(0);
            // 为当前购物车数据+1
            cart.setNumber(cart.getNumber() +1);
            shoppingCartMapper.updateNumberById(cart);
        }else {
            // !ok -> 购物车表中插入一条新的数据。
            // 插入前先判断当前商品是 菜品 还是 套餐。 分别去关联查询对应的数据信息进行补充。
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null){
                // 菜品数据,准备新增dish数据
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor());
                // 不需要关联查询
//                List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(dish.getId());
//                if (dishFlavors != null && dishFlavors.size() > 0){
//                    dishFlavors.forEach(dishFlavor -> {
//                        // 没有前端规范，目前覆盖。
//                        shoppingCart.setDishFlavor(dishFlavor.getValue());
//                    });
//                }
            }else{
                // 套餐数据
                Long setmealId = shoppingCartDTO.getSetmealId();
                if (setmealId != null){
                    // 获取套餐数据，构造套餐商品信息
                    Setmeal setmeal = setmealMapper.getById(setmealId);
                    shoppingCart.setSetmealId(setmealId);
                    shoppingCart.setName(setmeal.getName());
                    shoppingCart.setImage(setmeal.getImage());
                    shoppingCart.setAmount(setmeal.getPrice());
                }
            }
            // 相同的构造信息抽离
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            // 插入数据
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 获取购物车数据
     */
    public List<ShoppingCart> showShoppingCart(){
        // 获取当前用户购物车数据
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    /**
     * 清空购物车数据
     */
    public void cleanShoppingCart(){
        // 获取当前用户数据
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    /**
     * 对当前购物车中的商品数量减少一份，如果为 0 则删除。
     */
    public void removeShoppingCart(ShoppingCartDTO shoppingCartDTO){
        //
        Long userId = BaseContext.getCurrentId();
        // 删除菜品
        if (shoppingCartDTO.getDishId() != null) {
            // 构造 购物车实体对象 菜品数据
            ShoppingCart shoppingCart = ShoppingCart.builder()
                    .dishId(shoppingCartDTO.getDishId())
                    .dishFlavor(shoppingCartDTO.getDishFlavor())
                    .userId(userId).build();
            List<ShoppingCart> shops = shoppingCartMapper.list(shoppingCart);
            if (shops != null && shops.size()>0){
                // 获取当前商品数量
                ShoppingCart shop = shops.get(0);
                if (shop.getNumber() - 1 > 0) {
                    shop.setNumber(shop.getNumber() -1);
                    shoppingCartMapper.updateNumberById(shop);
                }else{
                    shoppingCartMapper.deleteById(shoppingCart);
                }
            }
        }else{
            // 删除套餐
            if (shoppingCartDTO.getSetmealId() != null){
                // 构造 购物车实体对象 套餐数据
                ShoppingCart shoppingCart = ShoppingCart.builder()
                        .setmealId(shoppingCartDTO.getSetmealId())
                        .userId(userId).build();
                List<ShoppingCart> shops = shoppingCartMapper.list(shoppingCart);
                if (shops != null && shops.size()>0){
                    // 获取当前商品数量
                    ShoppingCart shop = shops.get(0);
                    if (shop.getNumber() - 1 > 0) {
                        shop.setNumber(shop.getNumber() -1);
                        shoppingCartMapper.updateNumberById(shop);
                    }else{
                        shoppingCartMapper.deleteById(shop);
                    }
                }
            }
        }
    }
}
