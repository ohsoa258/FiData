package com.fisk.dataaccess.vo;

import lombok.Data;

import java.util.Objects;

/**
 * @author Lock
 */
@Data
public class TableNameVO {
    public long id;
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
        return id == tableNameVO.id &&
                Objects.equals(tableName, tableNameVO.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tableName);
    }
}
