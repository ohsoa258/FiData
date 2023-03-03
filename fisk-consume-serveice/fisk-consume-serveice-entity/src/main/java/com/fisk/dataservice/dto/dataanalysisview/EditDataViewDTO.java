package com.fisk.dataservice.dto.dataanalysisview;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.*;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "EditDataViewDTO", description = "编辑数据视图DTO")
public class EditDataViewDTO extends BaseDTO {

    @ApiModelProperty(value= "视图id")
    @NotNull(message = "视图id不能为空'")
    @Positive(message = "视图id必须大于0")
    private Integer viewId ;

    @ApiModelProperty(value = "视图名称")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9]*(?:_[A-Za-z0-9]+)*$", message = "视图名称只能包含字母和下划线，且必须以字母开始/字母或数字结尾")
    @Length(min = 1, max = 50, message = "视图名称不能为空且长度最大50")
    private String name ;

    @ApiModelProperty(value = "视图显示名称")
    @NotEmpty(message = "视图显示名称不能为空'")
    private String displayName ;

    @ApiModelProperty(value = "视图描述")
    private String viewDesc ;
}
