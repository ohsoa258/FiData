package com.fisk.datamanagement.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.classification.BusinessExtendedfieldsDTO;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoDTO;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoDefsDTO;
import com.fisk.datamanagement.dto.classification.FacttreeListDTO;
import com.fisk.datamanagement.entity.BusinessTargetinfoPO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface BusinessTargetinfoService {


    /**
     * 查询业务分类
     * @param
     * @return
     */
    JSONArray SelectClassification(String pid);


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
     * @param key redis key
     * @param response http请求响应
     */
    void downLoad(String key,String indicatorname, HttpServletResponse response);

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
}
