package com.fisk.datamodel.dto.fact;

import com.fisk.datamodel.dto.factattribute.FactAttributeDropDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class FactDropDTO {
    public int id;
    public String factTableEnName;
    /**
     * 事实表字段列表
     */
    public List<FactAttributeDropDTO> list;
}
