package com.fisk.dataservice.dto.tablesyncmode;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ApiTableSyncModeDTO {

    public Integer id;

    /**
     * api服务id
     */
    @ApiModelProperty(value = "api服务id", required = true)
    public Integer typeTableId;

    /**
     * 调度类型
     */
    @ApiModelProperty(value = "调度类型(触发类型为定时触发,此字段有值)", required = true)
    public String timerDriver;

    /**
     * corn表达式
     */
    @ApiModelProperty(value = "corn表达式(触发类型为定时触发,此字段有值)", required = true)
    public String cornExpression;

    /**
     * 触发类型:1定时触发 2关联触发
     */
    @ApiModelProperty(value = "触发类型:1定时触发 2关联触发", required = true)
    public Integer triggerType;

    /**
     * 关联管道
     */
    @ApiModelProperty(value = "关联管道id(触发类型为关联触发,此字段有值)", required = true)
    public Integer associatePipe;

}
