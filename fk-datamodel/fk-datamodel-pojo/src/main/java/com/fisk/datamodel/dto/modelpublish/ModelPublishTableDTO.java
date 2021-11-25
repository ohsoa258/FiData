package com.fisk.datamodel.dto.modelpublish;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ModelPublishTableDTO {
    public long tableId;
    public String tableName;
    public String sqlScript;
    public List<ModelPublishFieldDTO> fieldList;
}
