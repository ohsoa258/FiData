package com.fisk.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.auth.entity.ClientInfo;

/**
 * @author Lock
 * @date 2021/5/17 14:49
 */
public interface ClientInfoService extends IService<ClientInfo> {

    /**
     * 调用此接口,获取秘钥fk-auth-service配置文件中的秘钥(fk.jwt.key)
     *
     * @param clientId 服务名称
     * @param secret   每个服务自己的秘钥
     * @return 查询结果
     */
    String getSecretKey(String clientId, String secret);
}
