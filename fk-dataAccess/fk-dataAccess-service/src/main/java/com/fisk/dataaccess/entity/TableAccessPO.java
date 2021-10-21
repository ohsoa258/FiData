package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_table_access")
public class TableAccessPO extends BasePO {

    /**
     *  tb_app_registration表id
     */
    public long appId;

    /**
     * 添加数据时atlas生成
     */
    public String atlasTableId;

    /**
     * 组件id
     */
//    public String componentId;

    /**
     * 调度组件id
     */
//    public String schedulerComponentId;

    /**
     * nifi sql
     */
//    public String dorisSelectSqlStr;

    /**
     *  物理表名
     */
    public String tableName;

    /**
     *  物理表描述
     */
    public String tableDes;

    /**
     *  如果是实时物理表，需要提供数据同步地址
     */
    public String syncSrc;

    /**
     *  0是实时物理表，1是非实时物理表
     */
    public int isRealtime;

}
