package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.UserConfigureDTO;
import com.fisk.dataservice.dto.UserDTO;
import com.fisk.dataservice.map.ConfigureUserMap;
import com.fisk.dataservice.entity.ApiConfigurePO;
import com.fisk.dataservice.entity.ConfigureUserPO;
import com.fisk.dataservice.entity.MiddleConfigurePO;
import com.fisk.dataservice.mapper.ApiConfigureMapper;
import com.fisk.dataservice.mapper.ConfigureUserMapper;
import com.fisk.dataservice.mapper.MiddleConfigureMapper;
import com.fisk.dataservice.service.ConfigureUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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

    @Resource
    private MiddleConfigureMapper middleMapper;

    @Override
    public List<ConfigureUserPO> listData(Page<ConfigureUserPO> page) {
        return configureUserMapper.selectPage(page, null).getRecords();
    }

    @Override
    public ResultEnum saveUserConfigure(UserConfigureDTO dto) {
        if (StringUtils.isEmpty(dto)) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // 用户不存在，先添加用户
        ConfigureUserPO configureUser = configureUserMapper.selectById(dto.id);
        if (configureUser == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 用户存在，添加Api服务
        List<Integer> apiIds = new ArrayList<>();
        List<String> apiName = dto.getApiName();
        for (String name : apiName) {
            apiIds.add(obtainId(name));
        }

        for (Integer apiId : apiIds) {
            MiddleConfigurePO middleConfigure = new MiddleConfigurePO();
            middleConfigure.setUserId(Integer.parseInt(String.valueOf(dto.id)));
            middleConfigure.setConfigureId(apiId);
            if (middleMapper.insert(middleConfigure) <= 0) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum saveUser(ConfigureUserPO dto) {
        if (StringUtils.isEmpty(dto)) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        QueryWrapper<ConfigureUserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ConfigureUserPO::getDownSystemName, dto.getDownSystemName()).last("limit 1");
        ConfigureUserPO configureUser = configureUserMapper.selectOne(queryWrapper);
        if (configureUser != null) {
            return ResultEnum.DATA_EXISTS;
        }
        return configureUserMapper.insert(dto) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateUser(UserDTO dto) {
        ConfigureUserPO configureUser = configureUserMapper.selectById(dto.id);
        if (configureUser == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        ConfigureUserPO user = ConfigureUserMap.INSTANCES.dtoToPo(dto);
        return configureUserMapper.updateById(user) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteUserById(Integer id) {
        if (id == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        if (StringUtils.isEmpty(userExistent(id))) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        QueryWrapper<MiddleConfigurePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(MiddleConfigurePO::getUserId, id)
                .select(MiddleConfigurePO::getConfigureId);
        List<MiddleConfigurePO> configureList = middleMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(configureList)) {
            // 该用户下没有Api服务，可以直接删除
            if (configureUserMapper.deleteById(id) <= 0) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        } else {
            // 用户下有Api服务,必须先删除服务
            return ResultEnum.API_DELETE_ERROR;
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteUserApiById(UserConfigureDTO dto) {
        if (dto.id == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        if (StringUtils.isEmpty(userExistent(Integer.parseInt(String.valueOf(dto.id))))) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 获取Api对应的Id
        List<Integer> configureIds = new ArrayList<>();
        for (String apiName : dto.getApiName()) {
            configureIds.add(obtainId(apiName));
        }

        for (Integer configureId : configureIds) {
            QueryWrapper<MiddleConfigurePO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(MiddleConfigurePO::getUserId, dto.id)
                    .eq(MiddleConfigurePO::getConfigureId, configureId);
            if (middleMapper.delete(queryWrapper) <= 0){
                return ResultEnum.DELETE_ERROR;
            }
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ConfigureUserPO byUserId(Integer id) {
        ConfigureUserPO user = configureUserMapper.selectById(id);
        if (StringUtils.isEmpty(user)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        } else {
            return user;
        }
    }

    @Override
    public List<ApiConfigurePO> configureByUserId(Integer id) {
        // 查询用户id下所有服务的id
        QueryWrapper<MiddleConfigurePO> query = new QueryWrapper<>();
        query.lambda()
                .eq(MiddleConfigurePO::getUserId, id)
                .select(MiddleConfigurePO::getConfigureId);
        List<MiddleConfigurePO> configureList = middleMapper.selectList(query);
        if (CollectionUtils.isEmpty(configureList)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 根据服务id查询所有服务
        List<ApiConfigurePO> apiConfigureList = new ArrayList<>();
        for (MiddleConfigurePO middleConfigurePO : configureList) {
            apiConfigureList.add(apiConfigureMapper.selectById(middleConfigurePO.getConfigureId()));
        }
        return apiConfigureList;
    }

    /**
     * 根据apiName获取对应ID
     *
     * @param apiName
     * @return
     */
    public Integer obtainId(String apiName) {
        QueryWrapper<ApiConfigurePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ApiConfigurePO::getApiName, apiName)
                .select(ApiConfigurePO::getId);
        ApiConfigurePO apiConfigure = apiConfigureMapper.selectOne(queryWrapper);
        return Integer.parseInt(String.valueOf(apiConfigure.getId()));
    }

    /**
     * 判断用户是否存在
     * @param userId 用户id
     */
    public ConfigureUserPO userExistent(Integer userId){
        return configureUserMapper.selectById(userId);
    }
}
