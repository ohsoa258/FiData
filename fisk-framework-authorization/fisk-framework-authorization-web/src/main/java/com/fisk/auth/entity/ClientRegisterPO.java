package com.fisk.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * <p>
 * 客户端注册表
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-04 16:46:24
 */
@Data
@TableName("tb_client_register")
@EqualsAndHashCode(callSuper = true)
public class ClientRegisterPO extends BasePO {

    /**
     * 客户端名称
     */
    public String clientName;

    /**
     * token值
     */
    public String tokenValue;

    /**
     * token描述
     */
    public String tokenDes;

    /**
     * 是否有效
     */
    public boolean valid;

    /**
     * 过期时间(yyyy-MM-dd)
     */
    public Date expireTime;
}
