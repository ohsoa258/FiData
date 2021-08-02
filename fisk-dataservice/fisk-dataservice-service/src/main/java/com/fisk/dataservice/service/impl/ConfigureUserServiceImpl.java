package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.dto.UserDTO;
import com.fisk.dataservice.entity.ApiConfigurePO;
import com.fisk.dataservice.entity.ConfigureUserPO;
import com.fisk.dataservice.map.ApiConfigureFieldMap;
import com.fisk.dataservice.mapper.ApiConfigureMapper;
import com.fisk.dataservice.mapper.ConfigureUserMapper;
import com.fisk.dataservice.service.ConfigureUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/7/30 14:18
 */
@Service
public class ConfigureUserServiceImpl implements ConfigureUserService {

    @Resource
    private ConfigureUserMapper configureUserMapper;

    @Resource
    private ApiConfigureMapper apiConfigureMapper;

    @Override
    public List<UserDTO> listData(Page<ConfigureUserPO> page) {
        List<ConfigureUserPO> userList = configureUserMapper.selectPage(page, null).getRecords();

        List<UserDTO> userDTOList = new ArrayList<>();
        for (ConfigureUserPO configureUser : userList) {
            UserDTO user = ApiConfigureFieldMap.INSTANCES.poToDto(configureUser);
            QueryWrapper<ApiConfigurePO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(ApiConfigurePO::getId, configureUser.getConfigureId())
                    .select(ApiConfigurePO::getApiName);
            ApiConfigurePO apiConfigure = apiConfigureMapper.selectOne(queryWrapper);
            user.setConfigureName(apiConfigure.getApiName());
            userDTOList.add(user);
        }
        return userDTOList;
    }
}
