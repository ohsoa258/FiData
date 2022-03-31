package com.fisk.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BaseEntity;
import lombok.Data;

/**
 * @author Lock
 * @date 2021/5/17 14:41
 */
@Data
@TableName("tb_client_info")
public class ClientInfo extends BaseEntity {
    @TableId
    private Long id;
    /**
     * 服务名称
     */
    private String clientId;

    /**
     * 客户端秘钥,用于微服务身份验证
     */
    private String secret;

    /**
     * 服务介绍
     */
    private String info;
}
