package com.fisk.chartvisual.dto;

import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
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
