package com.fisk.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认证白名单类
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_chart")
public class AuthenticateWhiteList extends BasePO {
    public String path;
    public String details;
}
