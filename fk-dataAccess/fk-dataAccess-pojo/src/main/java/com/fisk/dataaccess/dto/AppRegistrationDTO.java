package com.fisk.dataaccess.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

/**
 * @author: Lock
 * @data: 2021/5/26 14:59 
 *
 * 应用注册添加应用
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AppRegistrationDTO {

    private String id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用描述
     */
    private String appDes;

    /**
     * 应用类型
     */
    private byte appType;

    /**
     * 应用负责人
     */
    private String appPrincipal;

    /**
     * 应用负责人邮箱
     */
    private String appPrincipalEmail;

    /**
     * 数据源
     */
    private AppDatasourceDTO appDatasourceDTO;
}
