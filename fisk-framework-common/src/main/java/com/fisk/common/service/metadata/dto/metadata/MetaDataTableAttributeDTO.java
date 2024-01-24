package com.fisk.common.service.metadata.dto.metadata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-07-01 14:33
 */
@Data
public class MetaDataTableAttributeDTO extends MetaDataBaseAttributeDTO {
    @ApiModelProperty(value = "字段集合")
    public List<MetaDataColumnAttributeDTO> columnList;

    /**
     *  应用名称（数据接入、数据建模、数据消费) 模型名(主数据)
     */
    public String AppName;

    /**
     *  应用ID（数据接入、数据建模、数据消费) 模型ID(主数据)
     */
    public Integer AppId;

    /**
     *  应用类型(数据接入)
     */

    public Integer AppType;
}
