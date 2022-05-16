package com.fisk.datagovernance.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.dataquality.DataQualityRequestDTO;
import com.fisk.datagovernance.service.dataquality.IDataQualityClientManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量服务接口API
 * @date 2022/3/22 16:17
 */
@Api(tags = {SwaggerConfig.DATA_QUALITY_CLIENT_CONTROLLER})
@RestController
@RequestMapping("/dataQualityClient")
public class DataQualityClientController {

    @Resource
    IDataQualityClientManageService service;

    /**
     * 消费字段规则模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费字段规则模板规则")
    @PostMapping("/dataQualityClient/buildFieldRule")
    ResultEntity<Object> buildFieldRule(@RequestBody DataQualityRequestDTO requestDTO) {
        return service.buildFieldRule(requestDTO);
    }

    /**
     * 消费字段聚合波动阈值模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费字段聚合波动阈值模板规则")
    @PostMapping("/dataQualityClient/buildFieldAggregateRule")
    ResultEntity<Object> buildFieldAggregateRule(@RequestBody DataQualityRequestDTO requestDTO) {
        return service.buildFieldAggregateRule(requestDTO);
    }

    /**
     * 消费表行数波动阈值模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费表行数波动阈值模板规则")
    @PostMapping("/dataQualityClient/buildTableRowThresholdRule")
    ResultEntity<Object> buildTableRowThresholdRule(@RequestBody DataQualityRequestDTO requestDTO) {
        return service.buildTableRowThresholdRule(requestDTO);
    }

    /**
     * 消费空表校验模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费空表校验模板规则")
    @PostMapping("/dataQualityClient/buildEmptyTableCheckRule")
    ResultEntity<Object> buildEmptyTableCheckRule(@RequestBody DataQualityRequestDTO requestDTO) {
        return service.buildEmptyTableCheckRule(requestDTO);
    }

    /**
     * 消费表更新校验模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费表更新校验模板规则")
    @PostMapping("/dataQualityClient/buildUpdateTableRule")
    ResultEntity<Object> buildUpdateTableRule(@RequestBody DataQualityRequestDTO requestDTO) {
        return service.buildUpdateTableRule(requestDTO);
    }

    /**
     * 消费表血缘断裂校验模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费表血缘断裂校验模板规则")
    @PostMapping("/dataQualityClient/buildTableBloodKinshipRule")
    ResultEntity<Object> buildTableBloodKinshipRule(@RequestBody DataQualityRequestDTO requestDTO) {
        return service.buildTableBloodKinshipRule(requestDTO);
    }

    /**
     * 消费业务验证模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费业务验证模板规则")
    @PostMapping("/dataQualityClient/buildBusinessCheckRule")
    ResultEntity<Object> buildBusinessCheckRule(@RequestBody DataQualityRequestDTO requestDTO) {
        return service.buildBusinessCheckRule(requestDTO);
    }

    /**
     * 消费相似度模板消费规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费相似度模板消费规则")
    @PostMapping("/dataQualityClient/buildSimilarityRule")
    ResultEntity<Object> buildSimilarityRule(@RequestBody DataQualityRequestDTO requestDTO) {
        return service.buildSimilarityRule(requestDTO);
    }

    /**
     * 消费业务清洗模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费业务清洗模板规则")
    @PostMapping("/dataQualityClient/buildBusinessFilterRule")
    ResultEntity<Object> buildBusinessFilterRule(@RequestBody DataQualityRequestDTO requestDTO) {
        return service.buildBusinessFilterRule(requestDTO);
    }

    /**
     * 消费指定时间回收模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费指定时间回收模板规则")
    @PostMapping("/dataQualityClient/buildSpecifyTimeRecyclingRule")
    ResultEntity<Object> buildSpecifyTimeRecyclingRule(@RequestBody DataQualityRequestDTO requestDTO) {
        return service.buildSpecifyTimeRecyclingRule(requestDTO);
    }

    /**
     * 消费空表回收模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费空表回收模板规则")
    @PostMapping("/dataQualityClient/buildEmptyTableRecoveryRule")
    ResultEntity<Object> buildEmptyTableRecoveryRule(@RequestBody DataQualityRequestDTO requestDTO) {
        return service.buildEmptyTableRecoveryRule(requestDTO);
    }

    /**
     * 消费数据无刷新回收模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费数据无刷新回收模板规则")
    @PostMapping("/dataQualityClient/buildNoRefreshDataRecoveryRule")
    ResultEntity<Object> buildNoRefreshDataRecoveryRule(@RequestBody DataQualityRequestDTO requestDTO) {
        return service.buildNoRefreshDataRecoveryRule(requestDTO);
    }

    /**
     * 消费数据血缘断裂回收模板规则
     *
     * @param requestDTO 请求DTO
     * @return 执行结果
     */
    @ApiOperation("消费数据血缘断裂回收模板规则")
    @PostMapping("/dataQualityClient/buildDataBloodKinshipRecoveryRule")
    ResultEntity<Object> buildDataBloodKinshipRecoveryRule(@RequestBody DataQualityRequestDTO requestDTO) {
        return service.buildDataBloodKinshipRecoveryRule(requestDTO);
    }
}
