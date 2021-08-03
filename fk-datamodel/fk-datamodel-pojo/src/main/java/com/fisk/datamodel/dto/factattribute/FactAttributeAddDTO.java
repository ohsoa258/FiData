package com.fisk.datamodel.dto.factattribute;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeAddDTO {
    public int factId;
    public List<FactAttributeDTO> list;
}
