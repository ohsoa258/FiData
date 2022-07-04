package com.fisk.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.auth.entity.ClientInfo;
import com.fisk.auth.mapper.ClientInfoMapper;
import com.fisk.auth.service.ClientInfoService;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: Lock
 * @data: 2021/5/17 14:50
 */
@Service
public class ClientInfoServiceImpl extends ServiceImpl<ClientInfoMapper, ClientInfo> implements ClientInfoService {

    @Value("${fk.jwt.key}")
    private String key;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public String getSecretKey(String clientId, String secret) {

        // 1.查询服务名
        ClientInfo clientInfo = this.query().eq("client_id", clientId).eq("del_flag", 1).one();

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
