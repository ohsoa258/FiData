package com.fisk.datagovernance.service.impl.dataquality;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datagovernance.dto.dataquality.DataQualityRequestDTO;
import com.fisk.datagovernance.service.dataquality.IDataQualityClientManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量服务接口实现类
 * @date 2022/4/12 13:47
 */
@Service
@Slf4j
public class DataQualityClientManageImpl implements IDataQualityClientManageService {

    @Override
    public ResultEntity<Object> buildFieldRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildFieldAggregateRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildTableRowThresholdRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildEmptyTableCheckRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildUpdateTableRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildTableBloodKinshipRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildBusinessCheckRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildSimilarityRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildBusinessFilterRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildSpecifyTimeRecyclingRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildEmptyTableRecoveryRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildNoRefreshDataRecoveryRule(DataQualityRequestDTO requestDTO) {
        return null;
    }

    @Override
    public ResultEntity<Object> buildDataBloodKinshipRecoveryRule(DataQualityRequestDTO requestDTO) {
        return null;
    }
}
