package com.fisk.datamanagement.dto.entity;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class EntityAssociatedMetaDataDTO {

    @ApiModelProperty(value = "guid")
    public String guid;

    @ApiModelProperty(value = "业务元数据属性")
    public JSONObject businessMetaDataAttribute;

}
