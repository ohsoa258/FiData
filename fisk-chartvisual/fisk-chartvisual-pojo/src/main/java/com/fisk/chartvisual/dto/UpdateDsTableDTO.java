package com.fisk.chartvisual.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author WangYan
 * @date 2022/3/9 17:27
 */
@Data
public class UpdateDsTableDTO extends DataSourceIdDTO {

    @NotNull
    @ApiModelProperty(value = "表名")
    private String tableName;

    @ApiModelProperty(value = "字段信息")
    private List<DsTableFieldDTO> fieldList;
}
