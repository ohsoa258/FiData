package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 应用实体类
 * @date 2022/1/6 14:51
 */
@Data
@TableName("tb_app_config")
public class AppConfigPO extends BasePO
{
    /**
     * 应用名称
     */
    public String appName;

    /**
     * 应用描述
     */
    public String appDesc;

    /**
     * 应用负责人
     */
    public String appPrincipal;

    /**
     * 应用账号
     */
    public String appAccount;

    /**
     * 密码/加密
     */
    public String appPassword;
}
