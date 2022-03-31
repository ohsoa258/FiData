package com.fisk.chartvisual.dto.datasource;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class DataSourceConQuery {
    public String name;
    public DataSourceTypeEnum conType;
    public String conAccount;
    public Long userId;
}
