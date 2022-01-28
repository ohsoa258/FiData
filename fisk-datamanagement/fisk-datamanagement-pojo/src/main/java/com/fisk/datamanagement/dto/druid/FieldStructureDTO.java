package com.fisk.datamanagement.dto.druid;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FieldStructureDTO {
    public String fieldName;
    public boolean alias;
    public String logic;
    public String source;
}
