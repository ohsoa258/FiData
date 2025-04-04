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
@ApiModel(value = "SaveDataViewDTO", description = "保存数据视图DTO")
public class SaveDataViewDTO extends BaseDTO {

    @ApiModelProperty(value= "所属视图主题id")
    @NotNull(message = "视图主题id不能为空'")
    @Positive(message = "视图主题id必须大于0")
    private Integer viewThemeId ;

    @ApiModelProperty(value = "视图名称")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9]*(?:_[A-Za-z0-9]+)*$", message = "视图名称只能包含字母和下划线，且必须以字母开始/字母或数字结尾")
    @Length(min = 1, max = 50, message = "视图名称不能为空且长度最大50")
    private String name ;

    @ApiModelProperty(value = "视图显示名称")
    @NotEmpty(message = "视图显示名称不能为空'")
    private String displayName ;

    @ApiModelProperty(value = "视图描述")
    private String viewDesc ;

    @ApiModelProperty(value = "视图脚本语句")
    @NotEmpty(message = "视图执行sql脚本不能为空'")
    private String viewScript ;

    @ApiModelProperty(value = "目标数据源id")
    @NotNull(message = "目标数据源id不能为空'")
    private Integer targetDbId ;

}
