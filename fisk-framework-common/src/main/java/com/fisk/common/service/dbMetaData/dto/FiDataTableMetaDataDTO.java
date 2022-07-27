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
     * 表/字段 名称
     */
    @ApiModelProperty(value = "表/字段 名称")
    public String name;

    /**
     * 表/字段 别名
     */
    @ApiModelProperty(value = "表/字段 别名")
    public String nameAlias;

    /**
     * 表字段集合
     */
    public List<FiDataTableMetaDataDTO> fieldList;
}
