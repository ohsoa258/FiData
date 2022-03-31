package com.fisk.chartvisual.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据源配置
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_datasource_con")
public class DataSourceConPO extends BasePO {

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
