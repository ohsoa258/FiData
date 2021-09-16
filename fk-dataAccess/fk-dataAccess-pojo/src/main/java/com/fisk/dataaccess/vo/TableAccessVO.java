package com.fisk.dataaccess.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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
     * 物理表名
     */
    public String tableName;

    /**
     * 物理表描述
     */
    public String tableDes;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * 更新时间
     */
    public LocalDateTime updateTime;

    /**
     * 时间戳字段(增量字段)
     */
    public String syncField;

    /**
     * 1：全量、2：时间戳增量、3：业务时间覆盖、4：自定义覆盖；
     */
    public int syncMode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    /**
     * 上次数据更新点
     */
    public LocalDateTime incrementalObjectivescoreEnd;
}
