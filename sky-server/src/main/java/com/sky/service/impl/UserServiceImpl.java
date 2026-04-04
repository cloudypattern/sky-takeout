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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private WeChatProperties wechatProperties;

    private static final String host = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(UserLoginDTO userLoginDTO) {

        String openId = getOpenId(userLoginDTO.getCode());

        if (openId ==  null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }


        User selected = userMapper.selectOneByOpenid(openId);
        if (selected == null){
            User user = User.builder()
                    .openid(openId)
                    .build();
            userMapper.insert(user);
            return user;

        }

        return selected;
    }

    private String getOpenId(String code){
        HashMap<String, String> query = new HashMap<>();
        query.put("appid", wechatProperties.getAppid());
        query.put("secret", wechatProperties.getSecret());
        query.put("js_code", code);

        // 发送请求
        String json = HttpClientUtil.doGet(host, query);

        // 解析JSON，只拿openid！！！
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }
}
