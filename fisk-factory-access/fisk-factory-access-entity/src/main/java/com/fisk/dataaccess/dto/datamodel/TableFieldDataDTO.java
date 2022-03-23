package com.fisk.dataaccess.dto.datamodel;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableFieldDataDTO {
    public long id;
    public String fieldName;
    public int type;
}
