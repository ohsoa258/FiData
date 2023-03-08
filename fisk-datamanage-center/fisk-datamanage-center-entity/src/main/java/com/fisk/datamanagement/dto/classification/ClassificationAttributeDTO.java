package com.fisk.datamanagement.dto.classification;

import com.fisk.common.core.baseObject.dto.BaseDTO;
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

    @NotEmpty(message = "业务分类id不能为空")
    public String guid;

    @NotEmpty(message = "属性名称不能为空")
    public String name;

    @NotEmpty(message = "属性类别名称不能为空")
    public String typeName;
}
