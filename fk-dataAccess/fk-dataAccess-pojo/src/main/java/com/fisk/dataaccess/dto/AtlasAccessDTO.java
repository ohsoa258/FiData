package com.fisk.dataaccess.dto;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class AtlasAccessDTO {
    /**
     * 物理表id
     */
    public long tableId;
    /**
     * 应用注册id
     */
    public long appid;

    /**
     * 当前登陆人
     */
    public String userId;

    /**
     * atlas对接物理表生成的GUID
     */
    public String atlasTableId;

    /**
     * doris生成的表名
     */
    public String tableName;
    /**
     * 执行sql
     */
    public String dorisSelectSqlStr;
}
