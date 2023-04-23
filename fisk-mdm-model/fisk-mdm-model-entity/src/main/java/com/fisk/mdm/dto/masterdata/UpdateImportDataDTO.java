package com.fisk.mdm.dto.masterdata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * @author JianWenYang
 */
@Data
public class UpdateImportDataDTO {

    @ApiModelProperty(value = "实体Id")
    private Integer entityId;

    @ApiModelProperty(value = "数据")
    private Map<String, Object> data;

}
