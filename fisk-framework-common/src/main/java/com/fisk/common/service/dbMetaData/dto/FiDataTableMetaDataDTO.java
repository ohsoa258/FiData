package com.fisk.common.service.dbMetaData.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description FiData 表元数据查询
 * @date 2022/6/16 17:18
 */
@Data
public class FiDataTableMetaDataDTO {
    /**
     * 表ID/字段ID
     */
    @ApiModelProperty(value = "表ID/字段ID")
    public String id;

    /**
     * 表名称/字段名称
     */
    @ApiModelProperty(value = "表名称/字段名称")
    public String name;

    /**
     * 表字段集合
     */
    public List<FiDataTableMetaDataDTO> fieldList;
}
