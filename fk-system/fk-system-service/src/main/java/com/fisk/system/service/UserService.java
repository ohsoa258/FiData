package com.fisk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEntity;
import com.fisk.system.dto.UserDTO;
import com.fisk.system.entity.User;

/**
 * @author Lock
 */
public interface UserService extends IService<User> {

    /**
     * 当前信息是否存在
     *
     * @param data data
     * @param type type
     * @return 执行结果
     */
    Boolean exist(String data, Integer type);

    /**
     * 添加用户
     *
     * @param user user
     */
    void register(User user);

    /**
     * 查询用户
     *
     * @param username username
     * @param password password
     * @return 查询结果
     */
    ResultEntity<UserDTO> queryUser(String username, String password);
}
