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
    public String name;
    public TypeEnum type;
    public List<JsonSchema> children;
    public String targetTableName;

    public enum TypeEnum {
        /**
         *
         */
        STRING, ARRAY, INT, DATETIME
    }
}

