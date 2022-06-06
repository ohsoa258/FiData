package com.fisk.dataaccess.dto.json;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author gy
 * @version 1.0
 * @description json模板
 * @date 2022/1/20 14:02
 */
@Data
@Builder
public class JsonSchema {
    /**
     * 表名(解析的标识)
     */
    public String name;
    /**
     * 数据类型
     */
    public TypeEnum type;
    /**
     * 子级
     */
    public List<JsonSchema> children;
    /**
     * 目标表名
     */
    public String targetTableName;

    public String guid;

    public enum TypeEnum {
        /**
         *
         */
        STRING, ARRAY, INT, DATETIME, TYPE
    }
}

