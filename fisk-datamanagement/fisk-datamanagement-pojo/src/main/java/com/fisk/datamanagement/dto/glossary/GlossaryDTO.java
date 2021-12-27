package com.fisk.datamanagement.dto.glossary;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class GlossaryDTO {
    public String guid;
    public String qualifiedName;
    public String name;
    public String shortDescription;
    public String longDescription;
}
