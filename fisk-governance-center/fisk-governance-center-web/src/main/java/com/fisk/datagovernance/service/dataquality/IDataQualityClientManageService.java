package com.fisk.datagovernance.service.dataquality;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datagovernance.dto.dataquality.DataQualityRequestDTO;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量服务接口
 * @date 2022/4/12 13:41
 */
public interface IDataQualityClientManageService {
    /**
     * 消费字段规则模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    ResultEntity<Object> buildFieldRule(DataQualityRequestDTO requestDTO);

    /**
     * 消费字段聚合波动阈值模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    ResultEntity<Object> buildFieldAggregateRule(DataQualityRequestDTO requestDTO);

    /**
     * 消费表行数波动阈值模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    ResultEntity<Object> buildTableRowThresholdRule(DataQualityRequestDTO requestDTO);

    /**
     * 消费空表校验模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    ResultEntity<Object> buildEmptyTableCheckRule(DataQualityRequestDTO requestDTO);

    /**
     * 消费表更新校验模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    ResultEntity<Object> buildUpdateTableRule(DataQualityRequestDTO requestDTO);

    /**
     * 消费表血缘断裂校验模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    ResultEntity<Object> buildTableBloodKinshipRule(DataQualityRequestDTO requestDTO);

    /**
     * 消费业务验证模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    ResultEntity<Object> buildBusinessCheckRule(DataQualityRequestDTO requestDTO);

    /**
     * 消费相似度模板消费规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    ResultEntity<Object> buildSimilarityRule(DataQualityRequestDTO requestDTO);

    /**
     * 消费业务清洗模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    ResultEntity<Object> buildBusinessFilterRule(DataQualityRequestDTO requestDTO);

    /**
     * 消费指定时间回收模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    ResultEntity<Object> buildSpecifyTimeRecyclingRule(DataQualityRequestDTO requestDTO);

    /**
     * 消费空表回收模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    ResultEntity<Object> buildEmptyTableRecoveryRule(DataQualityRequestDTO requestDTO);

    /**
     * 消费数据无刷新回收模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    ResultEntity<Object> buildNoRefreshDataRecoveryRule(DataQualityRequestDTO requestDTO);

    /**
     * 消费数据血缘断裂回收模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    ResultEntity<Object> buildDataBloodKinshipRecoveryRule(DataQualityRequestDTO requestDTO);
}
