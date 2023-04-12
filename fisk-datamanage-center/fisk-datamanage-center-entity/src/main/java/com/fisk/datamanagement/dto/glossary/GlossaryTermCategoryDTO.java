package com.fisk.datamanagement.dto.glossary;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GlossaryTermCategoryDTO extends GlossaryDTO {

    public String relationGuid;

    public String displayText;

}
