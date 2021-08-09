package com.fisk.dataaccess.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Lock
 * <p>
 * 分页对象
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TableAccessVO {

    public long id;

    public long appId;

    /**
     * 应用名称
     */
//    public String appName;

    /**
     * 物理表名
     */
    public String tableName;

    /**
     * 物理表描述
     */
    public String tableDes;

    /**
     * 更新时间
     */
    public LocalDateTime updateTime;

    /**
     * 时间戳字段(增量字段)
     */
    public String syncField;
}
