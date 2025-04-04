package com.fisk.dataservice.dto.dataanalysisview;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "UpdateDataViewDTO", description = "数据视图DTO")
public class UpdateDataViewDTO extends BaseDTO {

    @ApiModelProperty(value= "所属视图主题id")
    @NotNull(message = "视图主题id不能为空'")
    @Positive(message = "视图主题id必须大于0")
    private Integer viewThemeId ;

    @ApiModelProperty(value= "视图id")
    @NotNull(message = "视图id不能为空'")
    @Positive(message = "视图id必须大于0")
    private Integer viewId ;

    @ApiModelProperty(value = "视图脚本语句")
    @NotEmpty(message = "视图执行sql脚本不能为空'")
    private String viewScript ;

    @ApiModelProperty(value = "目标数据源id")
    @NotNull(message = "目标数据源id不能为空'")
    private Integer targetDbId ;
}
