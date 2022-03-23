package com.fisk.dataaccess.vo;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "应用id", required = true)
    public long appId;
    /**
     * 表名
     */
    @ApiModelProperty(value = "物理表名称", required = true)
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
        return appId == tableNameVO.appId &&
                Objects.equals(tableName, tableNameVO.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, tableName);
    }
}
