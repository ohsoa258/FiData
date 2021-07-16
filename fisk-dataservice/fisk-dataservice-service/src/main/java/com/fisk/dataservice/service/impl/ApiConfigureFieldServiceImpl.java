package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.entity.ApiConfigureFieldPO;
import com.fisk.dataservice.entity.ApiConfigurePO;
import com.fisk.dataservice.mapper.ApiConfigureFieldMapper;
import com.fisk.dataservice.mapper.ApiConfigureMapper;
import com.fisk.dataservice.service.ApiConfigureFieldService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

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

    // todo 登录人

    @Override
    public ResultEnum saveConfigureField(ApiConfigureFieldPO dto) {
        if (StringUtils.isEmpty(dto)) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        return configureFieldMapper.insert(dto) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 拼接ApiConfigurePO对象
     *
     * @param apiName
     * @param apiInfo
     * @return
     */
    @Override
    public ResultEnum saveApiConfigure(String apiName, String apiInfo) {
        ApiConfigurePO apiconfigurepo = new ApiConfigurePO();
        apiconfigurepo.setApiName(apiName);
        // 生成的·
        apiconfigurepo.setApiRoute("/a");
        apiconfigurepo.setApiInfo(apiInfo);

        return configureMapper.insert(apiconfigurepo) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataById(Integer id) {
        if (id == null){
            return ResultEnum.PARAMTER_NOTNULL;
        }

        ApiConfigureFieldPO apiConfigureFieldPO = configureFieldMapper.selectById(id);
        if (StringUtils.isEmpty(apiConfigureFieldPO)){
            return ResultEnum.DATA_NOTEXISTS;
        }
        return configureFieldMapper.deleteById(id) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateField(ApiConfigureFieldPO dto) {
        if (StringUtils.isEmpty(dto)){
            return ResultEnum.PARAMTER_NOTNULL;
        }

        ApiConfigureFieldPO apiConfigureFieldPO = configureFieldMapper.selectById(dto.getId());
        if (apiConfigureFieldPO == null){
            return ResultEnum.DATA_NOTEXISTS;
        }
        return configureFieldMapper.updateById(dto)> 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ApiConfigureFieldPO getDataById(Integer id) {
        if (id == null){
            return null;
        }

        return configureFieldMapper.selectById(id);
    }

    @Override
    public List<ApiConfigureFieldPO> listData(Integer currentPage, Integer pageSize) {
        if (currentPage == null || pageSize == null){
            return null;
        }

        IPage<ApiConfigureFieldPO> page = new Page<>(currentPage, pageSize);
        List<ApiConfigureFieldPO> apiConfigureFieldPOList = configureFieldMapper.selectPage(page, null).getRecords();
        return apiConfigureFieldPOList;
    }
}
