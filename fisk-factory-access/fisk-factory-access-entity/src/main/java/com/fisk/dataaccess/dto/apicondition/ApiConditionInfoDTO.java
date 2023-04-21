package com.fisk.dataaccess.dto.apicondition;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ApiConditionInfoDTO {

    @ApiModelProperty(value = "类型名称")
    public String typeName;

    @ApiModelProperty(value = "父类型名称")
    public String parentTypeName;

    @ApiModelProperty(value = "数据")
    public List<ApiConditionDetailDTO> data;

    @ApiModelProperty(value = "子类")
    public List<ApiConditionInfoDTO> child;

}
