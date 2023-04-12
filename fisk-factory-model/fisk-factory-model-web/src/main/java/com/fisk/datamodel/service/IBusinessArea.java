package com.fisk.datamodel.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.dbMetaData.dto.*;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.datafactory.dto.dataaccess.DispatchRedirectDTO;
import com.fisk.datamodel.dto.atomicindicator.IndicatorQueryDTO;
import com.fisk.datamodel.dto.businessarea.*;
import com.fisk.datamodel.dto.webindex.WebIndexDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;

import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
public interface IBusinessArea extends IService<BusinessAreaPO> {


    /**
     * 添加业务域
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(BusinessAreaDTO dto);

    /**
     * 回显数据: 根据id查询
     *
     * @param id id
     * @return 查询结果
     */
    BusinessAreaDTO getData(long id);

    /**
     * 修改业务域
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum updateBusinessArea(BusinessAreaDTO dto);

    /**
     * 根据id删除业务域
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteBusinessArea(long id);

    /**
     * 分页
     *
     * @param key  key
     * @param page page
     * @param rows rows
     * @return 查询结果
     */
    Page<Map<String, Object>> queryByPage(String key, Integer page, Integer rows);

    /**
     * 获取tb_area_business表全部字段
     *
     * @return 查询结果
     */
    List<FilterFieldDTO> getBusinessAreaColumn();

    /**
     * 分页
     *
     * @param query query
     * @return 查询结果
     */
    Page<BusinessPageResultDTO> getDataList(BusinessQueryDTO query);

    /**
     * Doris发布
     *
     * @param dto
     * @return
     */
    ResultEnum getBusinessAreaPublicData(IndicatorQueryDTO dto);

    /**
     * 获取业务域数量
     *
     * @return
     */
    WebIndexDTO getBusinessArea();

    /**
     * 获取业务域下已发布维度/事实表
     *
     * @param dto
     * @return
     */
    Page<PipelineTableLogVO> getBusinessAreaTable(PipelineTableQueryDTO dto);

    /**
     * 根据业务id、表类型、表id,获取表详情
     *
     * @param dto
     * @return
     */
    BusinessAreaTableDetailDTO getBusinessAreaTableDetail(BusinessAreaQueryTableDTO dto);

    /**
     * 跳转页面: 查询出当前表具体在哪个管道中使用,并给跳转页面提供数据
     *
     * @param dto dto
     * @return list
     */
    List<DispatchRedirectDTO> redirect(ModelRedirectDTO dto);

    /**
     * 获取数据建模结构
     *
     * @param dto dto
     * @return list
     */
    List<FiDataMetaDataDTO> getDataModelStructure(FiDataMetaDataReqDTO dto);

    /**
     * 获取数据建模表结构
     *
     * @param dto dto
     * @return list
     */
    List<FiDataMetaDataTreeDTO> getDataModelTableStructure(FiDataMetaDataReqDTO dto);

    /**
     * 刷新数据建模结构
     *
     * @param dto dto
     * @return list
     */
    boolean setDataModelStructure(FiDataMetaDataReqDTO dto);

    /**
     * 根据表信息/字段ID,获取表/字段基本信息
     *
     * @param dto dto
     * @return 查询结果
     */
    List<FiDataTableMetaDataDTO> getFiDataTableMetaData(FiDataTableMetaDataReqDTO dto);

    /**
     * 获取所有业务域
     *
     * @return
     */
    List<AppBusinessInfoDTO> getBusinessAreaList();

    /**
     * 获取当前业务域下所有发布成功表
     *
     * @param businessId
     * @return
     */
    List<TableNameDTO> getPublishSuccessTab(Integer businessId);

    /**
     * 获取数据建模元数据
     *
     * @return
     */
    List<MetaDataInstanceAttributeDTO> getDataModelMetaData();

    /**
     * 构建维度key脚本
     *
     * @param dto
     * @return
     */
    Object buildDimensionKeyScript(List<TableSourceRelationsDTO> dto);

    /**
     * 获取数据类型
     *
     * @param businessId
     * @return
     */
    JSONObject dataTypeList(Integer businessId);

    /**
     * 建模覆盖方式代码预览
     *
     * @param dto
     * @return
     */
    Object overlayCodePreview(OverlayCodePreviewDTO dto);

    /**
     * 建模覆盖方式代码预览
     * @param dto
     * @return
     */
    Object overlayCodePreviewTest(OverlayCodePreviewDTO dto);
}
