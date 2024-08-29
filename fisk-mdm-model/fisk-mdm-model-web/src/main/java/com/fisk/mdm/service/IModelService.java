package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.service.accessAndModel.AccessAndModelAppDTO;
import com.fisk.common.service.dbMetaData.dto.ColumnQueryDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.dataaccess.dto.taskschedule.ComponentIdDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.datamanagement.dto.standards.SearchColumnDTO;
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
     * 获取主数据结构
     * @return
     */
    List<FiDataMetaDataDTO> dataQualityGetMdmFolderTableTree(FiDataMetaDataReqDTO reqDto);


    List<FiDataMetaDataTreeDTO> getFieldDataTree(String entityId);
    /**
     * 刷新主数据结构
     * @return
     */
    boolean setDataStructure(FiDataMetaDataReqDTO reqDto);

    /**
     * 获取主数据表结构(数据标准用)
     * @param reqDto
     * @return
     */
    List<TableNameDTO> getTableDataStructure(FiDataMetaDataReqDTO reqDto);
    /**
     * 获取主数据字段结构(数据标准用)
     * @param reqDto
     * @return
     */
    List<TableColumnDTO> getFieldDataStructure(ColumnQueryDTO reqDto);

    /**
     * 获取模型名称和实体名称
     * @param dto
     * @return
     */
    ResultEntity<ComponentIdDTO> getModelNameAndEntityName(DataAccessIdsDTO dto);

    /**
     * 获取主数据模型(元数据业务分类)
     * @return
     */
    List<AppBusinessInfoDTO>  getMasterDataModel();


    /**
     * 获取所有模型和实体
     * @return
     */
    List<AccessAndModelAppDTO> getAllModelAndEntitys();

    /**
     * 获取主数据所有模型数量
     * @return
     */
    Integer getModelTotal();

    /**
     * 搜索主数据数据元关联字段
     * @param key
     * @return
     */
    List<SearchColumnDTO> searchStandardBeCitedField(String key);
}
