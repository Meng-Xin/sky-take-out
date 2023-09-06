package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    public WeChatProperties weChatProperties;

    // 微信服务接口地址
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    public UserMapper userMapper;
    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    public User login(UserLoginDTO userLoginDTO) {
        // 调用为新接口服务，获取当前用户的openid
        String openid = getOpenid(userLoginDTO.getCode());

        // 判断openid是否为空，如果为空表示登陆失败，抛出业务异常
        if (openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        // 判断当前用户是否为新用户
        User user = userMapper.getByOpenid(openid);
        if (user == null){
            // 如果是新用户，自动完成注册
            user = User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            userMapper.insert(user);
        }
        return user;
    }

    private String getOpenid(String code){
        Map<String, String> option = new HashMap<>();
        option.put("appid",weChatProperties.getAppid());
        option.put("secret",weChatProperties.getSecret());
        option.put("js_code",code);
        option.put("grant_type","authorization_code");
        String sdkJson = HttpClientUtil.doGet(WX_LOGIN, option);

        JSONObject jsonObject = JSON.parseObject(sdkJson);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
