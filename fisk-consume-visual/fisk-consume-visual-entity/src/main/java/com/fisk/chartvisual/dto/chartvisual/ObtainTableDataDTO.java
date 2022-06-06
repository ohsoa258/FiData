package com.fisk.chartvisual.dto.chartvisual;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author WangYan
 * @date 2022/3/8 11:26
 */
@Data
public class ObtainTableDataDTO {

    @NotNull
    @ApiModelProperty(value = "数据源id",required = true)
    private Integer id;

    @NotNull
    @ApiModelProperty(value = "表名" ,required = true)
    private String tableName;

    @ApiModelProperty(value = "分页条数" ,required = true)
    private Integer total;
}
