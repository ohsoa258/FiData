package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 *
 * 计算数据源实体类
 */
@Data
@TableName("tb_area_datasource")
@EqualsAndHashCode(callSuper = true)
public class DataSourceAreaPO extends BasePO {

    public String datasourceName;

    public String datasourceDes;

    public String databaseName;

    public String datasourceAddress;

    public String datasourceAccount;

    public String datasourcePwd;
}
