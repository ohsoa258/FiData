package com.fisk.dataaccess.dto.oraclecdc;

import com.fisk.dataaccess.enums.ScanStartupModeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class CdcHeadConfigDTO {

    @ApiModelProperty(value = "数据接入表id", required = true)
    public Long dataAccessId;

    @ApiModelProperty(value = "oracle-cdc管道名称", required = true)
    public String pipelineName;

    @ApiModelProperty(value = "oracle-cdc检查点时间", required = true)
    public Integer checkPointInterval;

    @ApiModelProperty(value = "oracle-cdc检查点时间单位", required = true)
    public String checkPointUnit;

    @ApiModelProperty(value = "0:从最开始读 1:从最新的读", required = true)
    public ScanStartupModeEnum scanStartupMode;

}
