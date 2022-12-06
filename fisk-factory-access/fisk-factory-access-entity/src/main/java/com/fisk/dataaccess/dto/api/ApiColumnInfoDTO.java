package com.fisk.dataaccess.dto.api;

import com.fisk.dataaccess.dto.table.FieldNameDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ApiColumnInfoDTO {

    public String tableName;

    public List<FieldNameDTO> fieldNameDTOList;

}
