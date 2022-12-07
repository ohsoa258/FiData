package com.fisk.common.service.sqlparser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author gy
 * @version 1.0
 * @description 表元数据对象
 * @date 2022/12/6 17:23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableMetaDataObject {
    public String id;
    public String name;
    public String alias;
    public String details;
    public Integer hierarchy;
    public String lastNodeId;
    public TableTypeEnum tableType;
    public List<FieldMetaDataObject> fields;
    public Map<String, Object> attributes;
}
