package com.fisk.mdm.entity;

import com.fisk.mdm.enums.DataTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * 实体
 *
 * @author ChenYa
 * @date 2022/04/21
 */
@Data
public class Entity {
    public String name;
    public String displayName;
    public List<Column> columns;
}

/**
 * 属性
 *
 * @author ChenYa
 * @date 2022/04/21
 */
@Data
class Column{
    public String name;
    public String displayName;
    public DataTypeEnum dataType;
}
