package com.fisk.dataaccess.dto.datamodel;

import lombok.Data;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class TableAccessDataDTO {

    public long id;
    public String tableName;
    public int type;
    public List<TableFieldDataDTO> fieldDtoList;
}
