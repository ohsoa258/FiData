package com.fisk.common.service.dbBEBuild.datamodel.dto;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableSourceFieldConfigDTO {

    public int fieldId;

    public String fieldName;

    public String fieldType;

    public int fieldLength;

    public String alias;

}
