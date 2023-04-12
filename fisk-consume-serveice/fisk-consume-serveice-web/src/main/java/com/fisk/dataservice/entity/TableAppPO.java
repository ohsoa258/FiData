package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
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
}
