package com.fisk.chartvisual.dto.datasource;

import com.fisk.chartvisual.enums.StorageEngineTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/1/17 16:31
 */
@Data
public class DataSourceDTO {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "类型")
    private StorageEngineTypeEnum type;
}
