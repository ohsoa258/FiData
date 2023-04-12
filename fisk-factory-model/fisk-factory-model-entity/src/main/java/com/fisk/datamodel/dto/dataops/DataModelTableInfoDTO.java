package com.fisk.datamodel.dto.dataops;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DataModelTableInfoDTO {

    public Integer tableId;

    public Integer businessAreaId;

    public String tableName;

    public Integer olapTable;

}
