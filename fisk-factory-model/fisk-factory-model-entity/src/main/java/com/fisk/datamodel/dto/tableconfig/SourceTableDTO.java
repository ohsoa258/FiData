package com.fisk.datamodel.dto.tableconfig;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class SourceTableDTO {

    public long id;

    public String tableName;

    public int type;

    public String tableDes;

    public String sqlScript;

    public List<SourceFieldDTO> fieldList;
}
