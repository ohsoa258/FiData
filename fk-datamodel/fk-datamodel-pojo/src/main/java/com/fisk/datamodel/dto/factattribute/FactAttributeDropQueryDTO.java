package com.fisk.datamodel.dto.factattribute;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeDropQueryDTO {
    public int id;
    /**
     * 查询事实字段类型
     */
    public List<Integer> type;
}
