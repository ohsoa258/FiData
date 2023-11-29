package com.fisk.datamanagement.service;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoDefsDTO;
import com.fisk.datamanagement.entity.BusinessTargetinfoPO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface BusinessTargetinfoService {


    /**
     * 查询业务分类
     * @param
     * @return
     */
    List<BusinessTargetinfoPO> SelectClassification(String pid);


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
    void downLoad(Integer key, HttpServletResponse response);




}
