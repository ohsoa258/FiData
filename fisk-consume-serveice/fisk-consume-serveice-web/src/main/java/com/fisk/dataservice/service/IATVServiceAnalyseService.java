package com.fisk.dataservice.service;

import com.fisk.dataservice.dto.serviceanalyse.ATVServiceAnalyseDTO;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-22 11:34
 * @description
 */

public interface IATVServiceAnalyseService {
    /**
     * 获取服务数据分析
     * @return
     */
    ATVServiceAnalyseDTO getServiceAnalyse();
}
