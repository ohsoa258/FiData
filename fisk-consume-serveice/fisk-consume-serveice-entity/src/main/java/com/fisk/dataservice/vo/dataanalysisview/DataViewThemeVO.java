package com.fisk.dataservice.vo.dataanalysisview;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.baseObject.vo.BaseVO;
import com.fisk.dataservice.dto.dataanalysisview.DataViewDTO;
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
@ApiModel(value = "DataViewThemeVO", description = "数据视图主题VO")
public class DataViewThemeVO extends BaseVO {

    @ApiModelProperty(value = "视图主题id")
    private Integer id ;

    @ApiModelProperty(value = "视图主题名称")
    private String themeName ;

    @ApiModelProperty(value = "视图主题简称")
    private String themeAbbr ;

    @ApiModelProperty(value = "视图主题描述")
    private String themeDesc ;

    @ApiModelProperty(value= "是否用作schema名称")
    private Boolean whetherSchema ;

    @ApiModelProperty(value = "视图主题负责人")
    private String themePrincipal ;

    @ApiModelProperty(value = "视图主题负责人邮箱")
    private String themePrincipalEmail ;

    @ApiModelProperty(value = "视图存储在目标库的id")
    private Integer targetDbId ;

    @ApiModelProperty(value = "数据视图分页集合")
    private PageDTO<DataViewDTO> pageDto;
}
