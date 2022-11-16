package com.fisk.datamodel.dto.dataops;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DataModelQueryDTO {

    public String odsTableName;

    public String stgTableName;

    public Integer id;

    public Integer businessAreaId;

    public Integer tableType;

}
