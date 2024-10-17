package com.fisk.dataaccess.dto.access;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ExportCdcConfigDTO {

    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    private Integer appId;

    /**
     * 表id集合
     */
    @ApiModelProperty(value = "表id集合")
    private List<Integer> tblIds;

    /**
     * 要导出的列
     */
    @ApiModelProperty(value = "要导出的列")
    private List<String> includeColumnFieldNames;

}
