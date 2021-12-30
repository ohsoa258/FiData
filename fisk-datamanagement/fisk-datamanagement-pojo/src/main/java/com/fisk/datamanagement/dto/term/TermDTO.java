package com.fisk.datamanagement.dto.term;

import com.fisk.datamanagement.dto.glossary.GlossaryAnchorDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryDTO;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TermDTO extends GlossaryDTO {
    public GlossaryAnchorDTO anchor;
}
