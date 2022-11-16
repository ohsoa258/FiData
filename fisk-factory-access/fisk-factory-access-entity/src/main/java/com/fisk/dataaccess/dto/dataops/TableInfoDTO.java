package com.fisk.dataaccess.dto.dataops;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableInfoDTO {

    public Integer tableAccessId;

    public Integer appId;

    public String tableName;

    public Integer olapTable;


}
