package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 根据 shopping 实体查找购物车所有数据 (动态查询)
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart); // select * from shopping_cart where user_id = ? and dish_id =? and setmeal_id = ?


    /**
     * 根据商品id来修改商品数量
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart); // update shopping_cart set number = ? where id = ?


    /**
     * 购物车中新增商品（菜品|套餐）
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time) " +
            "values (#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart); // insert into shopping_cart (...) values (...);

    @Delete("delete from shopping_cart where user_id = #{userId};")
    void deleteByUserId(Long userId); // delete from shopping_cart where user_id = ?

    /**
     * 删除购物车中的单个商品 `菜品`|`套餐`
     * @param shoppingCart
     */
    void deleteById(ShoppingCart shoppingCart);
}

