package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.apioutputparameter.ApiOutputParameterDTO;
import com.fisk.dataaccess.entity.ApiOutputParameterPO;
import com.fisk.dataaccess.map.ApiOutputParameterMap;
import com.fisk.dataaccess.mapper.ApiOutputParameterMapper;
import com.fisk.dataaccess.service.IApiOutputParameter;
import com.fisk.dataaccess.vo.output.apioutputparameter.ApiOutputParameterVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class ApiOutputParameterImpl
        extends ServiceImpl<ApiOutputParameterMapper, ApiOutputParameterPO>
        implements IApiOutputParameter {

    @Resource
    ApiOutputParameterMapper mapper;

    @Override
    public ResultEnum addApiOutputParameter(Long dataTargetId, List<ApiOutputParameterDTO> dtoList) {
        //先删除之前配置
        if (delApiOutputParameter(dataTargetId) != ResultEnum.SUCCESS) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        //重新添加配置
        dtoList.stream().map(e -> e.dataTargetId = dataTargetId).collect(Collectors.toList());
        List<ApiOutputParameterPO> data = ApiOutputParameterMap.INSTANCES.dtoListToPoList(dtoList);
        boolean result = this.saveBatch(data);
        if (!result) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<ApiOutputParameterVO> getApiOutputParameter(Long dataTargetId) {
        QueryWrapper<ApiOutputParameterPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApiOutputParameterPO::getDataTargetId, dataTargetId);
        List<ApiOutputParameterPO> list = mapper.selectList(queryWrapper);
        return ApiOutputParameterMap.INSTANCES.poListToVoList(list);
    }

    public ResultEnum delApiOutputParameter(Long dataTargetId) {
        QueryWrapper<ApiOutputParameterPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApiOutputParameterPO::getDataTargetId, dataTargetId);
        List<ApiOutputParameterPO> list = mapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(list)) {
            if (!this.remove(queryWrapper)) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }
        return ResultEnum.SUCCESS;
    }

}
