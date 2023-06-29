package com.fisk.dataservice.service;

import com.fisk.dataservice.dto.atvserviceanalyse.AtvServiceMonitoringQueryDTO;
import com.fisk.dataservice.dto.serviceanalyse.ATVServiceAnalyseDTO;
import com.fisk.dataservice.vo.atvserviceanalyse.*;

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
     *
     * @return
     */
    ATVServiceAnalyseDTO getServiceAnalyse();

    /**
     * 统计数据服务API熔断情况
     *
     * @return
     */
    AtvCallApiFuSingAnalyseVO getAtvCallApiFuSingAnalyse();

    /**
     * 统计数据服务API昨天和今天调用情况
     *
     * @return
     */
    List<AtvYasCallApiAnalyseVO> getAtvYasCallApiAnalyse();

    /**
     * 统计数据服务API今天调用情况前20条
     *
     * @return
     */
    List<AtvTopCallApiAnalyseVO> getAtvTopCallApiAnalyse();

    /**
     * 扫描数据服务API是否熔断
     *
     * @return
     */
    void scanDataServiceApiIsFuSing();

    /**
     * 服务监控应用以及API下拉框数据
     *
     * @return
     */
    AtvServiceDropdownCardVO getAtvServiceDropdownCard(AtvServiceMonitoringQueryDTO dto);

    /**
     * API当天调用时长TOP20：接口调用耗时排名
     *
     * @return
     */
    List<AtvApiTimeConsumingRankingVO> getAtvApiTimeConsumingRanking(AtvServiceMonitoringQueryDTO dto);

    /**
     * API当天调用趋势：接口调用成功失败的数量排名
     *
     * @return
     */
    List<AtvApiSuccessFailureRankingVO> getAtvApiSuccessFailureRanking(AtvServiceMonitoringQueryDTO dto);

    /**
     * API申请人明细：负责人创建的应用下绑定的API
     *
     * @return
     */
    List<AtvApiPrincipalDetailAppBindApiVO> getAtvApiPrincipalDetailAppBindApi(AtvServiceMonitoringQueryDTO dto);

    /**
     * API申请次数TOP20：API所绑定的应用排名
     *
     * @return
     */
    List<AtvApiSqCountApiBindAppRankingVO> getAtvApiSqCountApiBindAppRanking(AtvServiceMonitoringQueryDTO dto);

}
