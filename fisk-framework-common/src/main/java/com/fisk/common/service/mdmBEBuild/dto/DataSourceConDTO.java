package com.fisk.common.service.mdmBEBuild.dto;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/5/7 15:31
 * @Version 1.0
 */
@Data
public class DataSourceConDTO {

    public String name;

    public String conStr;

    public DataSourceTypeEnum conType;

    public String conIp;

    public int conPort;

    public String conDbname;

    public String conAccount;

    public String conPassword;

    public String conCube;
}
