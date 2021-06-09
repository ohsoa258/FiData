package com.fisk.chartvisual.dto;

import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

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
