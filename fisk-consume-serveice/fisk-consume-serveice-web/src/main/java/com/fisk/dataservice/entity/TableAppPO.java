package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 表应用
 * @date 2023/3/3 11:45
 */
@Data
@TableName("tb_table_app")
public class TableAppPO extends BasePO {
    /**
     * 表应用名称
     */
    public String appName;

    /**
     * 表应用描述
     */
    public String appDesc;

    /**
     * 表应用负责人
     */
    public String appPrincipal;

    /**
     * 表应用负责人邮箱
     */
    public String appPrincipalEmail;
    /**
     * 表应用类型 1:数据表（table_service） 2:接口（api）
     */
    public Integer appType;

    /**
     * 1:Rest API 2:Web Service
     */
    public Integer interfaceType;
    /**
     *0:无身份验证 1:基础验证 2:JWT 3:Bearer Token 4:OAuth2.0 5:apiKey
     */
    public Integer authenticationType;
    /**
     *验证地址
     */
    public String authenticationUrl;

    /**
     *1:header 2:Params
     */
    public Integer authType;

    /**
     *1:get 2:post
     */
    public Integer requestType;
}
