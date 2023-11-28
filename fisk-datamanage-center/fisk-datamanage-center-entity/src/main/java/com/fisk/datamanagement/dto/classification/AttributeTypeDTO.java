package com.fisk.datamanagement.dto.classification;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class AttributeTypeDTO extends BaseDTO {

    @ApiModelProperty(value = "名称")
    @NotEmpty(message = "属性类型不能为空")
    public String name;
}
