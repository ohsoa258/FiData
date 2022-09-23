package com.fisk.dataaccess.dto.oraclecdc;

import com.fisk.dataaccess.dto.table.FieldNameDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class CdcJobParameterDTO {

    public Integer dataSourceId;
    /**
     * 目标表id
     */
    public Integer tableAccessId;

    /**
     * 目标表名称
     */
    public String targetTable;

    /**
     * 表字段
     */
    public List<FieldNameDTO> fieldNameDTOList;

}
