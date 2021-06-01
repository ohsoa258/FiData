package com.fisk.chartvisual.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 数据源配置
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_datasource_con")
public class DataSourceConPO extends BasePO {

    public String conStr;

    public int conType;

    public String conDbname;

    public String conAccount;

    public String conPassword;

}
