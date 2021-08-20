package com.fisk.datamodel.dto.table;

import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TableNameDTO {

    public Integer id;

    public DataDoFieldTypeEnum type;

    public String tableName;

}
