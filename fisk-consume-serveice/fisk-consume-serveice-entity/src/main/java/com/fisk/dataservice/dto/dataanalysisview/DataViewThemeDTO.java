package com.fisk.dataservice.dto.dataanalysisview;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
@ApiModel(value = "DataViewThemeDTO", description = "数据视图主题DTO")
public class DataViewThemeDTO extends BaseDTO{

    @ApiModelProperty(value = "视图主题id")
    private Integer id ;

    @ApiModelProperty(value = "视图主题名称", required = true)
    @NotEmpty(message = "视图主题名称不能为空")
    private String themeName ;

    @ApiModelProperty(value = "视图主题简称", required = true)
    @NotEmpty(message = "视图主题简称不能为空")
    private String themeAbbr ;

    @ApiModelProperty(value= "是否用作schema名称", required = true)
    private Boolean whetherSchema ;

    @ApiModelProperty(value = "视图主题负责人", required = true)
    @NotEmpty(message = "视图主题负责人不能为空")
    private String themePrincipal ;

    @ApiModelProperty(value = "视图主题负责人邮箱", required = true)
    @NotEmpty(message = "视图主题负责人邮箱不能为空")
    @Email(message = "邮箱格式错误")
    private String themePrincipalEmail ;

    @ApiModelProperty(value = "视图存储在目标库的id", required = true)
    @NotNull(message = "视图存储在目标库的id不能为空")
    private Integer targetDbId ;

    @ApiModelProperty(value = "视图主题关联账号名称集合")
    private List<DataViewAccountDTO> relAccountList;
}
