package com.fisk.dataaccess.dto.dataops;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableQueryDTO {

    public String odsTableName;

    public String stgTableName;

    public Integer id;

    public Integer appId;

}
