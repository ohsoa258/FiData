package com.fisk.dataservice.service.impl;

import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.entity.ApiConfigureFieldPO;
import com.fisk.dataservice.entity.ApiConfigurePO;
import com.fisk.dataservice.mapper.ApiConfigureFieldMapper;
import com.fisk.dataservice.mapper.ApiConfigureMapper;
import com.fisk.dataservice.service.ApiConfigureFieldService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

import java.util.ArrayList;
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
    public ResultEnum saveConfigureField(List<ApiConfigureFieldPO> dto, String apiName, String apiInfo,Integer distinctData) {
        if (StringUtils.isEmpty(dto)) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // 把字段表配置信息先保存到字段表
        for (ApiConfigureFieldPO configurable : dto) {
            configureFieldMapper.insert(configurable);
        }

        ApiConfigurePO apiconfigurepo = this.splicingApiConfigure(apiName, apiInfo,distinctData);
        return configureMapper.insert(apiconfigurepo) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 拼接ApiConfigurePO对象
     *
     * @param apiName
     * @param apiInfo
     * @return
     */
    public ApiConfigurePO splicingApiConfigure(String apiName, String apiInfo,Integer distinctData) {
        ApiConfigurePO apiconfigurepo = new ApiConfigurePO();
        apiconfigurepo.setApiName(apiName);
        // 生成的·
        apiconfigurepo.setApiRoute("/a");
        apiconfigurepo.setApiInfo(apiInfo);
        apiconfigurepo.setDistinctData(distinctData);

        return apiconfigurepo;
    }
}
