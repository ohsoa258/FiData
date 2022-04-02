package com.fisk.chartvisual.dto.datasource;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author gy
 */
@Data
public class TestConnectionDTO {

    @NotNull()
    public String conStr;

    @NotNull
    public DataSourceTypeEnum conType;

    @NotNull()
    public String conAccount;

    @NotNull()
    public String conPassword;
}
