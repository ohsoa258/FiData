package com.fisk.datamodel.dto.factattribute;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class FactAttributeAddDTO {
    public int factId;
    public boolean isPublish;
    /**
     * 发布备注
     */
    public String remark;
    public List<FactAttributeDTO> list;
}
