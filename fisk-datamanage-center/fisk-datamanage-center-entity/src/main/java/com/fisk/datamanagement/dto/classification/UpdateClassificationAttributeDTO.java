package com.fisk.datamanagement.dto.classification;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Data
public class UpdateClassificationAttributeDTO {

    @NotEmpty(message = "属性id不能为空")
    public String guid;

    @NotEmpty(message = "属性名称不能为空")
    public String name;

    @NotEmpty(message = "属性类别名称不能为空")
    public String typeName;
}
