package com.fisk.datamanagement.dto.classification;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ClassificationAttributeDefsDTO {
    @ApiModelProperty(value = "分类属性名称,添加属性时必填")
    public String name;
    @ApiModelProperty(value = "分类属性类型名称,添加属性时必填")
    public String typeName;
    @ApiModelProperty(value = "是否可选(默认为true)")
    @JsonProperty(value = "isOptional")
    public boolean isOptional;
}
