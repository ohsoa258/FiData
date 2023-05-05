package com.fisk.datamanagement.dto.businessmetadataconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 * @date 2022-06-28 14:28
 */
@Data
public class BusinessMetadataConfigDTO {

    /**
     * 业务元数据名称
     */
    @ApiModelProperty(value = "业务元数据名称")
    public String businessMetadataName;

    /**
     * 业务元数据中文名称
     */
    @ApiModelProperty(value = "业务元数据中文名称")
    public String businessMetadataCnName;

    /**
     * 业务元数据属性名称
     */
    @ApiModelProperty(value = "业务元数据属性名称")
    public String attributeName;

    /**
     * 业务元数据属性中文名称
     */
    @ApiModelProperty(value = "业务元数据属性中文名称")
    public String attributeCnName;

    /**
     * 适用元数据类型:rdbms_instance、rdbms_db、rdbms_table、rdbms_column
     */
    @ApiModelProperty(value = "适用元数据类型")
    public String suitableType;

    /**
     * 属性是否支持多值
     */
    @ApiModelProperty(value = "属性是否支持多值")
    public Boolean multipleValued;

    /**
     * 属性类型:string、int、boolean
     */
    @ApiModelProperty(value = "属性类型")
    public String attributeType;

}
