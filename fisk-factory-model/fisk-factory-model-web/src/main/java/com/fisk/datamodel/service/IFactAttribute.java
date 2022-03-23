package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.FieldNameDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.fact.FactAttributeDetailDTO;
import com.fisk.datamodel.dto.factattribute.*;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;

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
     * @param dto
     * @return
     */
    ResultEnum addFactAttribute(FactAttributeAddDTO dto);

    /**
     * 事实字段批量删除
     * @param ids
     * @return
     */
    ResultEnum deleteFactAttribute(List<Integer> ids);

    /**
     * 根据事实表字段id,获取字段详情
     * @param factAttributeId
     * @return
     */
    FactAttributeUpdateDTO getFactAttributeDetail(int factAttributeId);

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
     * @param dto
     */
    List<FactAttributeDropDTO> GetFactAttributeData(FactAttributeDropQueryDTO dto);

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

    /**
     *根据事实id,获取事实表字段列表与关联详情
     * @param factId
     * @return
     */
    ResultEntity<List<ModelPublishFieldDTO>> selectAttributeList(Integer factId);

    /**
     * 根据事实id,获取事实字段(宽表)
     * @param factId
     * @return
     */
    List<FactAttributeUpdateDTO> getFactAttribute(int factId);

}
