package com.fisk.dataaccess.dto.oraclecdc;

import com.fisk.dataaccess.dto.table.FieldNameDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class CdcJobParameterDTO {

    public Integer appId;
    /**
     * 目标表id
     */
    public Integer tableAccessId;
    /**
     * 表字段
     */
    public List<FieldNameDTO> fieldNameDTOList;

}
