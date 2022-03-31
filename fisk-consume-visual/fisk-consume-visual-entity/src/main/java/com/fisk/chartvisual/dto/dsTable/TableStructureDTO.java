package com.fisk.chartvisual.dto.dsTable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author WangYan
 * @date 2022/3/9 10:34
 */
@Data
public class TableStructureDTO {


    @NotNull
    @ApiModelProperty(value = "数据源id")
    private Integer id;

    @NotNull
    @ApiModelProperty(value = "表名")
    private List<String> tableName;
}
