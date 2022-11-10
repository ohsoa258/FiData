package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.apiresultconfig.ApiResultConfigDTO;
import com.fisk.dataaccess.entity.ApiResultConfigPO;
import com.fisk.dataaccess.map.ApiResultConfigMap;
import com.fisk.dataaccess.mapper.ApiResultConfigMapper;
import com.fisk.dataaccess.service.IApiResultConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class ApiResultConfigImpl
        extends ServiceImpl<ApiResultConfigMapper, ApiResultConfigPO>
        implements IApiResultConfig {

    @Resource
    ApiResultConfigMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum apiResultConfig(long appDatasourceId, List<ApiResultConfigDTO> dto) {
        if (delApiResultConfig(appDatasourceId) != ResultEnum.SUCCESS) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return addApiResultConfig(appDatasourceId, dto);
    }

    public ResultEnum delApiResultConfig(long appDatasourceId) {
        QueryWrapper<ApiResultConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApiResultConfigPO::getAppDatasourceId, appDatasourceId);
        List<ApiResultConfigPO> poList = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(poList)) {
            return ResultEnum.SUCCESS;
        }
        if (!this.remove(queryWrapper)) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    public ResultEnum addApiResultConfig(long appDatasourceId, List<ApiResultConfigDTO> dto) {
        if (CollectionUtils.isEmpty(dto)) {
            return ResultEnum.SUCCESS;
        }
        List<ApiResultConfigPO> poList = ApiResultConfigMap.INSTANCES.dtoListToPoList(dto);
        poList.stream().map(e -> e.appDatasourceId = appDatasourceId).collect(Collectors.toList());
        boolean flat = this.saveBatch(poList);
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    public List<ApiResultConfigDTO> getApiResultConfig(long appDatasourceId) {
        QueryWrapper<ApiResultConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApiResultConfigPO::getAppDatasourceId, appDatasourceId);
        List<ApiResultConfigPO> poList = mapper.selectList(queryWrapper);
        return ApiResultConfigMap.INSTANCES.poListToDtoList(poList);
    }

}
