package com.fisk.datamodel.service;

import com.fisk.datamodel.dto.factattribute.FactAttributeListDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IFactAttribute {
    /**
     * 获取事实字段表数据
     * @param factId
     * @return
     */
    List<FactAttributeListDTO> getFactAttributeList(int factId);

}
