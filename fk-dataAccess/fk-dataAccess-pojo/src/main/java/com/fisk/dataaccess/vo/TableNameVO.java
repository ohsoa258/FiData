package com.fisk.dataaccess.vo;

import lombok.Data;

import java.util.Objects;

/**
 * @author Lock
 */
@Data
public class TableNameVO {

    /**
     * 应用注册id
     */
    public long appid;
    /**
     * 表名
     */
    public String tableName;
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TableNameVO tableNameVO = (TableNameVO) o;
        return appid == tableNameVO.appid &&
                Objects.equals(tableName, tableNameVO.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appid, tableName);
    }
}
