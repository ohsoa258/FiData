package com.fisk.datamodel.service;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.table.FieldNameDTO;
import com.fisk.datamodel.dto.dimension.DimensionSelectDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.fact.FactAttributeDetailDTO;
import com.fisk.datamodel.dto.factattribute.*;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableQueryPageDTO;
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
     * @return
     */
    List<FactAttributeDropDTO> getFactAttributeData(FactAttributeDropQueryDTO dto);

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

    /**
     * 根据事实字段id获取事实字段配置详情
     *
     * @param id id
     * @return dto
     */
    FactAttributeDTO getConfigDetailsByFactAttributeId(int id);

    /**
     * 添加单个事实字段
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addFactField(FactAttributeDTO dto);

    /**
     * 根据业务域id,获取业务域下的维度表详情和共享维度表详情(表名+字段)
     *
     * @param id 业务域id
     * @return 查询结果
     */
    List<DimensionSelectDTO> getDimensionDetailByBusinessId(int id);

    /**
     * 查询事实表关联数据
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    WideTableQueryPageDTO executeFactTableSql(WideTableFieldConfigDTO dto);

    /**
     * 修改单个事实字段
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editFactField(FactAttributeDTO dto);
}
