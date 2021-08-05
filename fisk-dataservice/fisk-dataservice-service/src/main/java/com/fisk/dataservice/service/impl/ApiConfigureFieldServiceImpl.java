package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.entity.GbfPO;
import com.fisk.dataservice.vo.ApiFieldDataVO;
import com.fisk.dataservice.dto.ApiConfigureFieldEditDTO;
import com.fisk.dataservice.entity.ApiConfigureFieldPO;
import com.fisk.dataservice.entity.ApiConfigurePO;
import com.fisk.dataservice.map.ApiConfigureFieldMap;
import com.fisk.dataservice.mapper.ApiConfigureFieldMapper;
import com.fisk.dataservice.mapper.ApiConfigureMapper;
import com.fisk.dataservice.service.ApiConfigureFieldService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.fisk.dataservice.utils.TransformationUtils.toFirstChar;

/**
 * @author WangYan
 * @date 2021/7/8 9:58
 */
@Service
public class ApiConfigureFieldServiceImpl implements ApiConfigureFieldService {

    @Resource
    private ApiConfigureFieldMapper configureFieldMapper;

    @Resource
    private ApiConfigureMapper configureMapper;

    @Override
    public ResultEnum saveConfigure(ApiFieldDataVO dto) {
        if (StringUtils.isEmpty(dto)) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        ApiConfigurePO apiconfigurepo = new ApiConfigurePO();
        apiconfigurepo.setApiName(dto.getApiName());
        apiconfigurepo.setApiInfo(dto.getApiInfo());
        apiconfigurepo.setTableName(dto.getTableName());
        // 生成的·
        String apiRoute = toFirstChar(dto.getApiName()).toLowerCase();
        QueryWrapper<ApiConfigurePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApiConfigurePO::getApiRoute, apiRoute);
        ApiConfigurePO apbConfigurer = configureMapper.selectOne(queryWrapper);
        if (apbConfigurer == null){
            apiconfigurepo.setApiRoute(apiRoute);
        }else {
            // 证明已经存在
            apiconfigurepo.setApiRoute(apiRoute+1);
        }

        if (configureMapper.insert(apiconfigurepo) < 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        List<ApiConfigureFieldPO> apiConfigureFieldList = ApiConfigureFieldMap.INSTANCES.dtoConfigureFieldListPo(dto.getApiConfigureFieldList());
        return this.splicingApiConfigureField(apiConfigureFieldList,apiconfigurepo.getId());
    }

    /**
     * 保存 ApiConfigureFieldPO 对象
     * @param dto
     * @param id
     * @return
     */
    public ResultEnum splicingApiConfigureField(List<ApiConfigureFieldPO> dto,long id) {
        // 把字段表配置信息先保存到字段表
        for (ApiConfigureFieldPO configurable : dto) {
            configurable.setConfigureId((int) id);
            if (configureFieldMapper.insert(configurable) <= 0){
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteDataById(Integer id) {
        if (id == null){
            return ResultEnum.PARAMTER_NOTNULL;
        }

        ApiConfigureFieldPO apiConfigureField = configureFieldMapper.selectById(id);
        if (StringUtils.isEmpty(apiConfigureField)){
            return ResultEnum.DATA_NOTEXISTS;
        }
        return configureFieldMapper.deleteById(id) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateField(ApiConfigureFieldEditDTO dto) {
        ApiConfigureFieldPO configureField = configureFieldMapper.selectById(dto.id);
        if (configureField == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        ApiConfigureFieldMap.INSTANCES.editDtoToPo(dto,configureField);
        return configureFieldMapper.updateById(configureField)> 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<ApiConfigureFieldPO> getDataById(Integer configureId) {
        if (configureId == null){
            return null;
        }

        QueryWrapper<ApiConfigureFieldPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApiConfigureFieldPO::getConfigureId, configureId);
        return configureFieldMapper.selectList(queryWrapper);
    }

    @Override
    public ApiConfigureFieldPO getById(Integer id) {
        if (id == null){
            throw new FkException(ResultEnum.PARAMTER_NOTNULL);
        }

        ApiConfigureFieldPO apiConfigureField = configureFieldMapper.selectById(id);
        if (apiConfigureField == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }else {
            return apiConfigureField;
        }
    }

    @Override
    public Object getAllField() {
        List<Object> objectList = new ArrayList<>();
        GbfPO gbf = new GbfPO();
        gbf.setTableName("gbf");
        gbf.setId("id");
        gbf.setStudentNo("studentNo");
        gbf.setName("name");
        gbf.setAge("age");
        gbf.setHeight("height");
        gbf.setWeight("weight");
        gbf.setGender("gender");
        objectList.add(gbf);

        return objectList;
    }
}
