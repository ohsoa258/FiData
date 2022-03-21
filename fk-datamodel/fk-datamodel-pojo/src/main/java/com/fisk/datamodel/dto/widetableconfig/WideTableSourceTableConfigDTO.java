package com.fisk.datamodel.dto.widetableconfig;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableSourceTableConfigDTO {

    public int tableId;

    public String tableName;
    /**
     * 0:维度表、1:事实表
     */
    public int tableType;

    public List<WideTableSourceFieldConfigDTO> columnConfig;

}
