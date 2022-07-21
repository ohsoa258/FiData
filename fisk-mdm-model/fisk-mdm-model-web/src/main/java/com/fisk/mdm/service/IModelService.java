package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.mdm.dto.model.ModelUpdateDTO;
import com.fisk.mdm.entity.ModelPO;
import com.fisk.mdm.dto.model.ModelDTO;
import com.fisk.mdm.dto.model.ModelQueryDTO;
import com.fisk.mdm.vo.model.ModelInfoVO;
import com.fisk.mdm.vo.model.ModelVO;

import java.util.List;

/**
 * @author ChenYa
 */
public interface IModelService extends IService<ModelPO> {
    /**
     * 根据id查询
     * @param id
     * @return
     */
    ResultEntity<ModelVO> getById(Integer id);

    /**
     * 添加模型
     * @param model
     * @return
     */
    ResultEnum addData(ModelDTO model);

    /**
     * 编辑
     * @param modelUpdateDTO
     * @return
     */
    ResultEnum editData(ModelUpdateDTO modelUpdateDTO);

    /**
     * 删除
     * @param id
     * @return
     */
    ResultEnum deleteDataById(Integer id);

    /**
     * 分页查询所有模型
     * @param query
     * @return
     */
    Page<ModelVO> getAll(ModelQueryDTO query);

    /**
     * 通过模型id获取实体
     *
     * @param modelId 模型id
     * @return {@link ModelInfoVO}
     */
    ModelInfoVO getEntityById(Integer modelId,String name);

    /**
     * 获取主数据结构
     * @return
     */
    List<FiDataMetaDataDTO> getDataStructure(FiDataMetaDataReqDTO reqDto);

    /**
     * 刷新主数据结构
     * @return
     */
    boolean setDataStructure(FiDataMetaDataReqDTO reqDto);
}
