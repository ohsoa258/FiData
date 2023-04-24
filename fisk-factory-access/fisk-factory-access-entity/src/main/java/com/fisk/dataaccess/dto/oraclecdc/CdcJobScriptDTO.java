package com.fisk.dataaccess.dto.oraclecdc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class CdcJobScriptDTO {
    /**
     * oracle-cdc任务脚本
     */
    @ApiModelProperty(value = "oracle-cdc任务脚本")
    public String jobScript;

    //public Integer savepointHistoryId = 0;

}
