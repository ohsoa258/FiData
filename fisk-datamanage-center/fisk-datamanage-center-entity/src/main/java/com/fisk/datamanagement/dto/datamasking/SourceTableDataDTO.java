package com.fisk.datamanagement.dto.datamasking;

import com.fisk.datamanagement.enums.TableTypeEnum;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class SourceTableDataDTO {

    public TableTypeEnum tableTypeEnum;
    public long tableId;
    public String tableName;
    public String tableGuid;

}
