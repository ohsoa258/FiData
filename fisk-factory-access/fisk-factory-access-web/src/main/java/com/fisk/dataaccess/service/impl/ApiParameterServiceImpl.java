package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.api.ApiParameterDTO;
import com.fisk.dataaccess.entity.ApiParameterPO;
import com.fisk.dataaccess.map.ApiParameterMap;
import com.fisk.dataaccess.mapper.ApiParameterMapper;
import com.fisk.dataaccess.service.IApiParameter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-04-26 11:07:14
 */
@Service
public class ApiParameterServiceImpl extends ServiceImpl<ApiParameterMapper, ApiParameterPO> implements IApiParameter {

    @Override
    public List<ApiParameterDTO> getListByApiId(long apiId) {

        // list: po -> dto
        return ApiParameterMap.INSTANCES.listPoToDto(this.query().eq("api_id", apiId).list());
    }

    @Override
    public ResultEnum addData(List<ApiParameterDTO> dtoList) {

        // 参数校验
        if (CollectionUtils.isEmpty(dtoList)) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        // dto -> po
        List<ApiParameterPO> poList = ApiParameterMap.INSTANCES.listDtoToPo(dtoList);


        //保存
        return this.saveBatch(poList) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum editData(List<ApiParameterDTO> dtoList) {

        // 参数校验
        if (CollectionUtils.isEmpty(dtoList)) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // dto -> po
        // 执行修改
        return this.updateBatchById(ApiParameterMap.INSTANCES.listDtoToPo(dtoList)) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteData(long id) {
        // 参数校验
        ApiParameterPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 执行删除
        return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

}