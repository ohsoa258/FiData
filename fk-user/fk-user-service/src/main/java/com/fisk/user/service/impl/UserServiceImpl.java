package com.fisk.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.user.dto.UserDTO;
import com.fisk.user.entity.User;
import com.fisk.user.mapper.UserMapper;
import com.fisk.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: Lock
 * @data: 2021/5/14 16:40
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     *  校验手机号或用户名是否存在
     * @param data 用户名或手机号
     * @param type 数据类型：1是用户名；2是手机；其它是参数有误
     * @return true：可以使用; false：不可使用
     */
    @Override
    public Boolean exist(String data, Integer type) {

        // type只能是1,2 type=2时,手机号必须符合格式要求(测试阶段不要求)
        if ((type != 1 && type != 2) || StringUtils.isEmpty(data)/* || (type == 2 && !RegexUtils.isPhone(dataaccess))*/) {

            throw new FkException(400, "请求参数有误");
        }

        return this.query()
                .eq(type == 1, "username", data)
                .eq(type == 2, "phone", data)
                .count() == 1;// 只查询一条,为1即存在,0就是无数据
    }


    @Override
    @Transactional  // 方法执行失败回滚机制
    public void register(User user) {

        // 1.对密码进行加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 2.写入数据库
        this.save(user);
    }

    /**
     * 登录: 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @Override
    public UserDTO queryUserByPhoneAndPassword(String username, String password) {

        // 1.根据用户名查询用户,不能根据密码,参数是明文,数据库中的是加密后的
        User user = this.query().eq("username", username).one();
        // 2.判断是否存在
        if (user == null) {
            // 用户名错误
            throw new FkException(400, "用户名或密码错误");
        }

        // 3.校验密码(原理)先根据密文推算出盐值,然后明文+盐值,密码,再次比较新密文和密文
        if(!passwordEncoder.matches(password, user.getPassword())){
            // 密码错误
            throw new FkException(400, "用户名或密码错误");
        }
        // 4.转换DTO
        return new UserDTO(user);
    }
}
