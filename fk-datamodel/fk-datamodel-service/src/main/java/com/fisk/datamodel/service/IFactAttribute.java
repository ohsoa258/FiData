package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeListDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeUpdateDTO;

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

    /**
     *事实字段表添加
     * @param factId
     * @param dto
     * @return
     */
    ResultEnum addFactAttribute(int factId, List<FactAttributeDTO> dto);

    /**
     * 事实字段批量删除
     * @param ids
     * @return
     */
    ResultEnum deleteFactAttribute(List<Integer> ids);

    /**
     * 事实字段数据更改
     * @param dto
     * @return
     */
    ResultEnum updateFactAttribute(FactAttributeUpdateDTO dto);

}
