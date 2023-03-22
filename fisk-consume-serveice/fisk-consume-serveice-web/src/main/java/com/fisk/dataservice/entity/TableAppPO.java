package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-22 11:51
 * @description
 */
@Data
@TableName("tb_table_app")
public class TableAppPO extends BasePO {
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
     * 应用负责人邮箱
     */
    public String appPrincipalEmail;

}
