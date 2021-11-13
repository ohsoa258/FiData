package com.fisk.datamodel.dto.fact;

import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeDetailDTO {
    /**
     * sql脚本
     */
    public String sqlScript;

    public List<FactAttributeDTO> attributeDTO;
}
