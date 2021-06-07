package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: Lock
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_table_fields")
public class TableFieldsPO extends BasePO {

    /**
     * table_access（id）
     */
    public long tableAccessId;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 字段描述
     */
    public String fieldDes;

    /**
     * 字段类型
     */
    public String fieldType;

    /**
     * 1是主键，0非主键
     */
    public int isPrimarykey;

    /**
     * 1是业务时间，0非业务时间
     */
    public int isBusinesstime;

    /**
     * 1是时间戳，0非时间戳
     */
    public long isRealtime;

    /**
     * 1：实时物理表的字段，0：非实时物理表的字段
     */
    public int isTimestamp;


}
