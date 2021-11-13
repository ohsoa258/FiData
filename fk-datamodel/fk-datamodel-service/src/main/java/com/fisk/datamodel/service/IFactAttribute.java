package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.FieldNameDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.fact.FactAttributeDetailDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDropDTO;
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
    ResultEnum addFactAttribute(int factId,boolean isPublish, List<FactAttributeDTO> dto);

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

    /**
     * 根据维度id获取事实字段详情
     * @param id
     * @return
     */
    ModelMetaDataDTO getFactMetaData(int id);

    /**
     * 根据事实id获取事实下字段
     * @param id
     */
    List<FactAttributeDropDTO> GetFactAttributeData(int id);

    /**
     * 根据事实表id获取来源表下未添加字段
     * @param id
     * @return
     */
    List<FieldNameDTO> getFactAttributeSourceId(int id);

    /**
     *根据事实id,获取事实表字段列表
     * @param factId
     * @return
     */
    FactAttributeDetailDTO getFactAttributeDataList(int factId);

}
