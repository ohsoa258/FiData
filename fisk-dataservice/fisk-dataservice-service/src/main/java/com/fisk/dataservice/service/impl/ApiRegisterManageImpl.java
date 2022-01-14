package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.api.ApiRegisterDTO;
import com.fisk.dataservice.dto.api.ApiRegisterEditDTO;
import com.fisk.dataservice.dto.api.ApiRegisterQueryDTO;
import com.fisk.dataservice.dto.api.FieldConfigEditDTO;
import com.fisk.dataservice.entity.ApiConfigPO;
import com.fisk.dataservice.entity.FieldConfigPO;
import com.fisk.dataservice.map.ApiFieldMap;
import com.fisk.dataservice.mapper.ApiFieldMapper;
import com.fisk.dataservice.mapper.ApiRegisterMapper;
import com.fisk.dataservice.service.IApiRegisterManageService;
import com.fisk.dataservice.vo.api.ApiRegisterDetailVO;
import com.fisk.dataservice.vo.api.ApiRegisterVO;
import com.fisk.dataservice.vo.api.FieldConfigVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * api接口实现类
 *
 * @author dick
 */
@Service
public class ApiRegisterManageImpl extends ServiceImpl<ApiRegisterMapper, ApiConfigPO> implements IApiRegisterManageService {

    @Resource
    private ApiFieldMapper apiFieldMapper;

    @Resource
    private ApiFieldManageImpl apiFieldManageImpl;

    @Override
    public Page<ApiRegisterVO> getAll(ApiRegisterQueryDTO query) {
        return baseMapper.getAll(query.page, query);
    }

    @Override
    public ResultEnum addData(ApiRegisterDTO dto) {
        return null;
    }

    @Override
    public ResultEnum editData(ApiRegisterEditDTO dto) {
        return null;
    }

    @Override
    public ResultEnum deleteData(Integer apiId) {
        ApiConfigPO model = baseMapper.selectById(apiId);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        model.setDelFlag(0);
        return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ApiRegisterDetailVO detail(Integer apiId) {
        return null;
    }

    @Override
    public List<FieldConfigVO> getFieldAll(Integer apiId) {
        List<FieldConfigVO> fieldList = new ArrayList<>();
        QueryWrapper<FieldConfigPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(FieldConfigPO::getApiId, apiId)
                .eq(FieldConfigPO::getDelFlag, 1);
        List<FieldConfigPO> selectList = apiFieldMapper.selectList(query);
        if (CollectionUtils.isNotEmpty(selectList)) {
            fieldList = ApiFieldMap.INSTANCES.listPoToVo(selectList);
        }
        return fieldList;
    }

    @Override
    public ResultEnum setField(List<FieldConfigEditDTO> dto)
    {
        if (CollectionUtils.isEmpty(dto)) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        List<FieldConfigPO> fieldList = ApiFieldMap.INSTANCES.listDtoToPo(dto);
        return apiFieldManageImpl.updateBatchById(fieldList) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEntity<Object> preview(Integer appId) {
        return null;
    }
}
