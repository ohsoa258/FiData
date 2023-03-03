package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "DataViewThemePO", description = "数据视图主题PO")
@TableName(value = "tb_view_theme")
public class DataViewThemePO extends BasePO {

    @ApiModelProperty(value = "视图主题名称", required = true)
    @NotEmpty(message = "视图主题名称不能为空")
    private String themeName ;

    @ApiModelProperty(value = "视图主题简称", required = true)
    @NotEmpty(message = "视图主题简称不能为空")
    private String themeAbbr ;

    @ApiModelProperty(value = "视图主题描述")
    private String themeDesc ;

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
}
