package com.fisk.dataaccess.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Lock
 *
 * 应用注册分页对象
 */
@Data
public class AppRegistrationVO{

    /**
     * id
     */
    public long id;
    /**
     * 应用名称
     */
    public String appName;
    /**
     * 应用描述
     */
    public String appDes;
    /**
     * 应用类型
     */
    public int appType;
    /**
     * 应用负责人
     */
    public String appPrincipal;
    /**
     * 创建时间
     */
    public LocalDateTime createTime;

}
