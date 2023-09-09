package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     *
     * @param openid
     * @return
     */
    @Select("select  * from user where  openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入数据
     *
     * @param userInfo
     */
    void insert(User userInfo);

    @Select("select * from user where id = #{userid}")
    User getById(Long userId);

    /**
     * 统计用户数量
     */
    Integer countByMap(Map map);
}
