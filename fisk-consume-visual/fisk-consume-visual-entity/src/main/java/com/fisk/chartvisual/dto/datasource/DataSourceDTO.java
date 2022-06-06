package com.fisk.chartvisual.dto.datasource;

import com.fisk.chartvisual.enums.StorageEngineTypeEnum;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/1/17 16:31
 */
@Data
public class DataSourceDTO {

    private Integer id;
    private StorageEngineTypeEnum type;
}
