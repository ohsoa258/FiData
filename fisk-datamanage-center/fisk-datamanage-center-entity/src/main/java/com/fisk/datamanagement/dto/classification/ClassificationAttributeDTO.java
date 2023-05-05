package com.fisk.datamanagement.dto.classification;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author 湖~Zero
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClassificationAttributeDTO extends BaseDTO {

    @ApiModelProperty(value = "guid")
    @NotEmpty(message = "业务分类id不能为空")
    public String guid;

    @ApiModelProperty(value = "名称")
    @NotEmpty(message = "属性名称不能为空")
    public String name;

    @ApiModelProperty(value = "类型名称")
    @NotEmpty(message = "属性类别名称不能为空")
    public String typeName;
}
