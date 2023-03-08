package com.fisk.datamanagement.dto.classification;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BasePO;
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

    @NotEmpty(message = "属性类型不能为空")
    public String name;
}
