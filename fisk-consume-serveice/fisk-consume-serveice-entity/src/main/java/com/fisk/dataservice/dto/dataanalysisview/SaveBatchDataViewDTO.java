package com.fisk.dataservice.dto.dataanalysisview;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fisk.common.core.baseObject.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class SaveBatchDataViewDTO extends BaseDTO {

    @ApiModelProperty(value= "所属视图主题id")
    @NotNull(message = "视图主题id不能为空'")
    @Positive(message = "视图主题id必须大于0")
    private Integer viewThemeId ;

    @ApiModelProperty(value = "表名称集合")
    @NotEmpty(message = "表名称集合不能为空'")
    private List<String> tableNameList ;
}
