package com.fisk.dataservice.dto.dataanalysisview;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Data
public class SelSqlResultDTO {

    @ApiModelProperty(value = "视图主题id", required = true)
    @NotNull(message = "视图主题id不能为空")
    @Positive(message = "视图主题id必须大于0'")
    public Integer viewThemeId;

    @ApiModelProperty(value = "当前页数", required = true)
    @NotNull(message = "当前页数")
    @Positive(message = "当前页数必须大于0'")
    public Integer pageNum;

    @ApiModelProperty(value = "每页条数", required = true)
    @NotNull(message = "每页条数")
    @Range(min = 1, max = 100, message = "每页条数错误")
    public Integer pageSize;

    @ApiModelProperty(value = "查询sql脚本语句", required = true)
    @NotEmpty(message = "查询sql不能为空")
    public String querySql;

    @ApiModelProperty(value = "视图名称", required = true)
    @NotEmpty(message = "视图名称不能为空")
    public String viewName;

    @ApiModelProperty(value = "数据源驱动类型", required = true)
    @NotEmpty(message = "数据源驱动类型不能为空")
    public String dataSourceTypeEnum;

    @ApiModelProperty(value = "目标数据源id", required = true)
    @NotNull(message = "目标数据源id不能为空")
    @Positive(message = "目标数据源id必须大于0'")
    public Integer targetDbId;
}
