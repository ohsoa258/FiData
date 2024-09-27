package com.fisk.datamanagement.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.category.CategoryQueryDTO;
import com.fisk.datamanagement.dto.category.IndexForAssetCatalogDTO;
import com.fisk.datamanagement.dto.classification.*;
import com.fisk.datamanagement.entity.BusinessTargetinfoPO;
import com.fisk.datamodel.dto.businessprocess.BusinessQueryDataParamDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface BusinessTargetinfoService {


    List<BusinessTargetinfoMenuDTO> getBusinessMetaDataDetailMenuList(String pid);
    /**
     * 查询业务分类
     * @param
     * @return
     */
    JSONObject SelectClassification(String id);


    JSONArray SelectClassifications(Integer fieldMetadataId);


    /**
     * 查询类型分类
     * @param
     * @return
     */
    JSONObject SelecttypeClassification();

    /**
     * 添加指标主题明细数据
     * @param dto
     * @return
     */
    ResultEnum addTargetinfo(BusinessTargetinfoDefsDTO dto);

    /**
     * 添加历史指标主题明细数据
     * @param dto
     */
    ResultEnum addHistoryBusinessMetaDataDetail(BusinessTargetinfoDefsDTO dto);

    /**
     * 删除指标主题明细数据
     * @param Id
     * @return
     */
    ResultEnum deleteTargetinfo(long Id);

    /**
     * 更改指标主题明细数据
     *
     * @param dto
     * @return
     */
    ResultEnum updateTargetinfo(BusinessTargetinfoDefsDTO dto);

    /**
     * 下载数据
     *
     * @param type      type
     * @param ids
     * @param response http请求响应
     */
    void downLoad(String type, List<String> ids, HttpServletResponse response);

    /**
     *  获取树状指标数据列表
     *
     * @return
     */
    List<BusinessTargetinfoPO> getDimensionList(String name);

    JSONObject getTargetinfoHistory(String historyId);

    /**
     * 数仓建模获取所有业务指标 只获取id 名称
     * @return
     */
    List<BusinessTargetinfoDTO> modelGetBusinessTargetInfoList();

    /**
     * 获取数仓字段和指标所属表里所有关联关系 只获取字段id 和指标id
     * @return
     */
    List<FacttreeListDTO> modelGetFactTreeList(Integer tblId);

    /**
     * 获取数仓字段和指标粒度表里所有关联关系 只获取字段id 和指标id
     * @return
     */
    List<BusinessExtendedfieldsDTO> modelGetMetricMapList();


    List<BusinessTargetinfoMenuDTO> getBusinessMetaDataNameList(String key);

    /**
     * 数据资产 - 资产目录 按指标标准分类
     *
     * @return
     */
    List<IndexForAssetCatalogDTO> getIndexForAssetCatalog();

    Integer getBusinessTargetinfoTotal();

    /**
     * 筛选器
     *
     * @param query 查询条件
     * @return 筛选结果
     */
    List<BusinessTargetinfoMenuDTO> pageFilter(CategoryQueryDTO query);

    /**
     * 获取指标数据查询参数
     * @param fieldId
     * @return
     */
    BusinessQueryDataParamDTO getBusinessQueryDataParam(Integer fieldId);
}
