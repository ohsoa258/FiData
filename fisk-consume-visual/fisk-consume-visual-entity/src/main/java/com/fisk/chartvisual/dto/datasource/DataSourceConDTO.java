package com.fisk.chartvisual.dto.datasource;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author gy
 */
@Data
public class DataSourceConDTO {

    @Length(min = 0, max = 50, message = "长度最多50")
    @NotNull()
    public String name;

    @Length(min = 0, max = 500, message = "长度最多500")
    @NotNull()
    public String conStr;

    @NotNull
    public DataSourceTypeEnum conType;

    @Length(min = 0, max = 50, message = "长度最多50")
    public String conIp;

    public int conPort;

    @Length(min = 0, max = 50, message = "长度最多50")
    public String conAccount;

    @Length(min = 0, max = 50, message = "长度最多50")
    public String conPassword;

    @Length(min = 0, max = 50, message = "长度最多50")
    public String conDbname;

    @Length(min = 0, max = 50, message = "长度最多50")
    public String conCube;
}
