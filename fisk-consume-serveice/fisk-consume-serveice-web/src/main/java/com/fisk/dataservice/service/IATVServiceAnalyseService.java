package com.fisk.dataservice.service;

import com.fisk.dataservice.dto.serviceanalyse.ATVServiceAnalyseDTO;
import com.fisk.dataservice.vo.atvserviceanalyse.AtvTopCallApiAnalyseVO;
import com.fisk.dataservice.vo.atvserviceanalyse.AtvYasCallApiAnalyseVO;

import java.util.List;

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

    /**
     * 统计数据服务API昨天和今天调用情况
     * @return
     */
    List<AtvYasCallApiAnalyseVO> getAtvYasCallApiAnalyse();

    /**
     * 统计数据服务API今天调用情况前20条
     * @return
     */
    List<AtvTopCallApiAnalyseVO> getAtvTopCallApiAnalyse();
}
