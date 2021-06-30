package com.fisk.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.auth.entity.ClientInfo;
import com.fisk.auth.mapper.ClientInfoMapper;
import com.fisk.auth.service.ClientInfoService;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author: Lock
 * @data: 2021/5/17 14:50
 */
@Service
public class ClientInfoServiceImpl extends ServiceImpl<ClientInfoMapper, ClientInfo> implements ClientInfoService {

    @Value("${fk.jwt.key}")
    private String key;

    @Autowired
    private PasswordEncoder passwordEncoder;



    /**
     * 调用此接口,获取秘钥fk-auth-service配置文件中的秘钥(fk.jwt.key)
     * @param clientId 服务名称
     * @param secret 每个服务自己的秘钥
     * @return
     */
    @Override
    public String getSecretKey(String clientId, String secret) {

        // 1.查询服务名
        ClientInfo clientInfo = this.query().eq("client_id", clientId).one();

        if (clientInfo == null) {
            throw new FkException(ResultEnum.AUTH_CLIENTINFO_ERROR, "客户端信息有误" + clientInfo + "不存在!");
        }

        // 2.校验client的secret
        if (!passwordEncoder.matches(secret, clientInfo.getSecret())) {
            throw new FkException(ResultEnum.AUTH_SECRET_ERROR);
        }

        // 验证通过,获取授权中心的秘钥,用于生成jwt的token
        return key;

    }
}
