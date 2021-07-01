package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: Lock
 *
 * 计算数据源实体类
 */
@Data
@TableName("tb_area_datasource") // 表名
@EqualsAndHashCode(callSuper = true)
public class DataSourceAreaPO extends BaseEntity {

    @TableId
    public long id;

    public String datasourceName;

    public String datasourceDes;

    public String databaseName;

    public String datasourceAddress;

    public String datasourceAccount;

    public String datasourcePwd;

    public String createUser;

    public String updateUser;

    public int delFlag;

}
