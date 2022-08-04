package com.fisk.datagovernance.vo.dataquality.notice;

import com.fisk.datagovernance.enums.dataquality.ModuleTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/8/4 13:57
 */
@Data
public class NoticeModuleVO {

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

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * 选中状态 1：选中 0：不选中
     */
    @ApiModelProperty(value = "选中状态 1：选中 0：不选中")
    public int checkd;
}
