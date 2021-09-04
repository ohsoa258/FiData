package com.fisk.dataaccess.vo.datareview;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Lock
 */
@Data
public class DataReviewVO {

    /**
     * id
     */
    public long id;
    /**
     * 应用名称
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
     * 字段长度
     */
    public String fieldLength;
    /**
     * 是否主键
     */
    public int isPrimarykey;
    /**
     * 创建时间
     */
    public LocalDateTime createTime;

}
