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
     * tb_app_registration表id
     */
    public Long appId;

    /**
     * 添加数据时atlas生成
     */
    public String atlasTableId;

    /**
     * 物理表名
     */
    public String tableName;

    /**
     * 物理表描述
     */
    public String tableDes;

    /**
     * 如果是实时物理表，需要提供数据同步地址
     */
    public String syncSrc;

    /**
     * 0是实时物理表，1是非实时物理表
     */
    public Integer isRealtime;
    /**
     * 0: 未发布  1: 发布成功  2: 发布失败
     */
    public Integer publish;

    /**
     * SQL脚本
     */
    public String sqlScript;
}
