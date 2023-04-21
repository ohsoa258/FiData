package com.fisk.datamanagement.dto.datamasking;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 * @description 数据脱敏入参dto
 * @date 2022/4/15 14:00
 */
@Data
public class DataMaskingSourceDTO {

    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public String datasourceId;

    /**
     * 表id
     */
    @ApiModelProperty(value = "表id")
    public String tableId;

}
