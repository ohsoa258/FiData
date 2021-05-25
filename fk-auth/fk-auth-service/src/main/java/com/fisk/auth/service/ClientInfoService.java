package com.fisk.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.auth.entity.ClientInfo;

/**
 * @author: Lock
 * @data: 2021/5/17 14:49
 */
public interface ClientInfoService extends IService<ClientInfo> {
    String getSecretKey(String clientId, String secret);
}
