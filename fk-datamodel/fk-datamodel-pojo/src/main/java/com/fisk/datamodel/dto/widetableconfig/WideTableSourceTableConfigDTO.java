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

    public List<WideTableSourceFieldConfigDTO> columnConfig;

}
