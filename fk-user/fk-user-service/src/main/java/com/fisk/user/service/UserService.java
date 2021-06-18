package com.fisk.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEntity;
import com.fisk.user.dto.UserDTO;
import com.fisk.user.entity.User;

/**
 * @author: Lock
 * @data: 2021/5/14 16:39
 */
public interface UserService extends IService<User> {
    Boolean exist(String data, Integer type);

    void register(User user);

    ResultEntity<UserDTO> queryUser(String username, String password);
}
