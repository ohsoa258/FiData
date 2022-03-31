package com.fisk.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.system.enums.serverModuleTypeEnum;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/11/2 17:58
 */
@TableName("tb_dataview")
@Data
public class DataviewPO extends BasePO {

    /**
     * 视图名称
     */
    private String viewName;
    /**
     * 视图逻辑对应的表
     */
    private serverModuleTypeEnum serverModule;
    /**
     * 0:personal 1: system
     */
    private String viewType;
    /**
     * 视图所属用户
     */
    private Integer userId;
}
