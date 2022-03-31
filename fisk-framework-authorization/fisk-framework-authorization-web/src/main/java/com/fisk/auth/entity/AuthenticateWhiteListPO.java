package com.fisk.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认证白名单类
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_authenticate_whitelist")
public class AuthenticateWhiteListPO extends BasePO {
    public String path;
    public String details;
}
