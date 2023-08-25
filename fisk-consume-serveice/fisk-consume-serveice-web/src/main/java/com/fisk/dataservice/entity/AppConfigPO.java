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
     * 应用申请人
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

    /**
     * 应用类型：1本地应用、2代理应用
     */
    public int appType;

    /**
     * 应用白名单，多个逗号分隔
     */
    public String appWhiteList;

    /**
     * 应用白名单状态：1启用、2禁用
     */
    public int appWhiteListState;

    /**
     * 部门名称
     */
    public String departName;
}
