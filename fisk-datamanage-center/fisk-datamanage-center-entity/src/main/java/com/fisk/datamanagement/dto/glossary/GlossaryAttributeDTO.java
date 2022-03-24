package com.fisk.datamanagement.dto.glossary;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class GlossaryAttributeDTO extends GlossaryDTO {

    public List<GlossaryTermAttributeDTO> terms;

    public List<GlossaryCategoryAttributeDTO> categories;

}
