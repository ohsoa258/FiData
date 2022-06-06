package com.fisk.datagovernance.dto.dataquality.notice;

import com.fisk.datagovernance.enums.dataquality.ModuleTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 通知扩展DTO
 * @date 2022/3/24 14:31
 */
public class NoticeExtendDTO {
    /**
     * 通知id
     */
    @ApiModelProperty(value = "通知id")
    public int noticeId;

    /**
     * 模块类型
     * 100、数据校验 200、业务清洗
     * 300、生命周期
     */
    @ApiModelProperty(value = "模块类型")
    public ModuleTypeEnum moduleType;

    /**
     * 规则id
     */
    @ApiModelProperty(value = "规则id")
    public int ruleId;
}
