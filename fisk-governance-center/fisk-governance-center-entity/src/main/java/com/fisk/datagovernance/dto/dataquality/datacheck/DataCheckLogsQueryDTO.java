package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckLogsVO;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version 1.0
 * @description 数据检验规则日志查询DTO
 * @date 2023/7/25 14:26
 */
public class DataCheckLogsQueryDTO {
    /**
     * 日志类型：
     * 1 接口同步数据校验日志（同步前）
     * 2 nifi同步数据校验日志（同步中）
     * 3 订阅报告数据校验日志（同步后）
     */
    @ApiModelProperty(value = "日志类型：1 接口同步数据校验日志（同步前）、2 nifi同步数据校验日志（同步中）、3 订阅报告数据校验日志（同步后）")
    public int logType;

    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    public int templateId;

    /**
     * 规则id
     */
    @ApiModelProperty(value = "规则id")
    public int ruleId;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<DataCheckLogsVO> page;
}
