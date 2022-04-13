package com.fisk.datagovernance.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datagovernance.enums.dataquality.DataQualityRequestDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量服务接口
 * @date 2022/4/12 11:31
 */
@FeignClient("dataquality-service")
public interface DataQualityClient {

    /**
     * 消费字段强规则模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费字段强规则模板规则")
    @PostMapping("/dataQualityClient/buildFieldStrongRule")
    ResultEntity<Object> buildFieldStrongRule(@RequestBody DataQualityRequestDTO requestDTO);

    /**
     * 消费字段聚合波动阈值模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费字段聚合波动阈值模板规则")
    @PostMapping("/dataQualityClient/buildFieldAggregateRule")
    ResultEntity<Object> buildFieldAggregateRule(@RequestBody DataQualityRequestDTO requestDTO);

    /**
     * 消费表行数波动阈值模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费表行数波动阈值模板规则")
    @PostMapping("/dataQualityClient/buildTableRowThresholdRule")
    ResultEntity<Object> buildTableRowThresholdRule(@RequestBody DataQualityRequestDTO requestDTO);

    /**
     * 消费空表校验模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费空表校验模板规则")
    @PostMapping("/dataQualityClient/buildEmptyTableCheckRule")
    ResultEntity<Object> buildEmptyTableCheckRule(@RequestBody DataQualityRequestDTO requestDTO);

    /**
     * 消费表更新校验模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费表更新校验模板规则")
    @PostMapping("/dataQualityClient/buildUpdateTableRule")
    ResultEntity<Object> buildUpdateTableRule(@RequestBody DataQualityRequestDTO requestDTO);

    /**
     * 消费表血缘断裂校验模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费表血缘断裂校验模板规则")
    @PostMapping("/dataQualityClient/buildTableBloodKinshipRule")
    ResultEntity<Object> buildTableBloodKinshipRule(@RequestBody DataQualityRequestDTO requestDTO);

    /**
     * 消费业务验证模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费业务验证模板规则")
    @PostMapping("/dataQualityClient/buildBusinessCheckRule")
    ResultEntity<Object> buildBusinessCheckRule(@RequestBody DataQualityRequestDTO requestDTO);

    /**
     * 消费相似度模板消费规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费相似度模板消费规则")
    @PostMapping("/dataQualityClient/buildSimilarityRule")
    ResultEntity<Object> buildSimilarityRule(@RequestBody DataQualityRequestDTO requestDTO);

    /**
     * 消费业务清洗模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费业务清洗模板规则")
    @PostMapping("/dataQualityClient/buildBusinessFilterRule")
    ResultEntity<Object> buildBusinessFilterRule(@RequestBody DataQualityRequestDTO requestDTO);

    /**
     * 消费指定时间回收模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费指定时间回收模板规则")
    @PostMapping("/dataQualityClient/buildSpecifyTimeRecyclingRule")
    ResultEntity<Object> buildSpecifyTimeRecyclingRule(@RequestBody DataQualityRequestDTO requestDTO);

    /**
     * 消费空表回收模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费空表回收模板规则")
    @PostMapping("/dataQualityClient/buildEmptyTableRecoveryRule")
    ResultEntity<Object> buildEmptyTableRecoveryRule(@RequestBody DataQualityRequestDTO requestDTO);

    /**
     * 消费数据无刷新回收模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费数据无刷新回收模板规则")
    @PostMapping("/dataQualityClient/buildNoRefreshDataRecoveryRule")
    ResultEntity<Object> buildNoRefreshDataRecoveryRule(@RequestBody DataQualityRequestDTO requestDTO);

    /**
     * 消费数据血缘断裂回收模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费数据血缘断裂回收模板规则")
    @PostMapping("/dataQualityClient/buildDataBloodKinshipRecoveryRule")
    ResultEntity<Object> buildDataBloodKinshipRecoveryRule(@RequestBody DataQualityRequestDTO requestDTO);

}
