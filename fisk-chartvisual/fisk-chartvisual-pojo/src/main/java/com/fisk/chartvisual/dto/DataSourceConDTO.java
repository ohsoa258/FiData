package com.fisk.chartvisual.dto;

import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class DataSourceConDTO {

    @Length(min = 0, max = 500, message = "长度最多500")
    @NotNull()
    public String conStr;

    @NotNull
    public DataSourceTypeEnum conType;

    @Length(min = 0, max = 50, message = "长度最多50")
    public String conAccount;

    @Length(min = 0, max = 50, message = "长度最多50")
    public String conPassword;

    @Length(min = 0, max = 50, message = "长度最多50")
    public String conDbname;
}
