package com.fisk.common.service.sqlparser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author gy
 * @version 1.0
 * @description 字段元数据对象
 * @date 2022/12/6 17:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FieldMetaDataObject {
    public String id;
    public String name;
    public String alias;
    public String owner;
}
