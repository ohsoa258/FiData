package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.ConfigureDTO;
import com.fisk.dataservice.dto.UserApiDTO;
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
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.fisk.dataservice.utils.paging.PagingUtils.startPage;
import static java.util.stream.Collectors.toList;

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
    public Page<UserDTO> listData(Page<ConfigureUserPO> page, String downSystemName) {
        QueryWrapper<ConfigureUserPO> query = new QueryWrapper<>();
        query.lambda()
                .orderByDesc(ConfigureUserPO::getCreateTime);

        if (StringUtils.isNotBlank(downSystemName)) {
            query.lambda()
                    .like(ConfigureUserPO::getDownSystemName, downSystemName);
            return ConfigureUserMap.INSTANCES.poToDtoPage(configureUserMapper.selectPage(page, query));
        }

        return ConfigureUserMap.INSTANCES.poToDtoPage(configureUserMapper.selectPage(page, query));
    }

    @Override
    public ResultEnum saveUserConfigure(UserConfigureDTO dto) {
        if (dto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        // 用户不存在
        ConfigureUserPO configureUser = configureUserMapper.selectById(dto.id);
        if (configureUser == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        QueryWrapper<MiddleConfigurePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(MiddleConfigurePO::getUserId, dto.id);
        middleMapper.delete(queryWrapper);

        // 判断添加的api服务不为空
        if (CollectionUtils.isEmpty(dto.apiIds)){
            return null;
        }

        // 用户存在，添加Api服务
        for (Integer apiId : dto.apiIds) {
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
        if (dto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        QueryWrapper<ConfigureUserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ConfigureUserPO::getDownSystemName, dto.getDownSystemName())
                .eq(ConfigureUserPO::getUserName, dto.getUserName())
                .last("limit 1");
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

        if (userExistent(id) == null) {
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

        if (userExistent(Integer.parseInt(String.valueOf(dto.id))) == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 判断添加的api服务不为空
        if (CollectionUtils.isEmpty(dto.apiIds)){
            return null;
        }

        for (Integer configureId : dto.apiIds) {
            QueryWrapper<MiddleConfigurePO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(MiddleConfigurePO::getUserId, dto.id)
                    .eq(MiddleConfigurePO::getConfigureId, configureId);
            if (middleMapper.delete(queryWrapper) <= 0) {
                return ResultEnum.DELETE_ERROR;
            }
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ConfigureUserPO byUserId(Integer id) {
        ConfigureUserPO user = configureUserMapper.selectById(id);
        if (user == null) {
            return null;
        } else {
            return user;
        }
    }

    @Override
    public Page<ConfigureDTO> configureByUserId(Integer id, Integer currentPage, Integer pageSize) {
        if (id == null) {
            return null;
        }

        // 用户下的服务id
        QueryWrapper<MiddleConfigurePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(MiddleConfigurePO::getUserId, id)
                .select(MiddleConfigurePO::getId, MiddleConfigurePO::getUserId, MiddleConfigurePO::getConfigureId);
        List<MiddleConfigurePO> userConfigureList = middleMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(userConfigureList)) {
            List<ConfigureDTO> dtoList = ConfigureUserMap.INSTANCES.poToDto(apiConfigureMapper.selectList(null))
                    .stream().map(e -> {
                        e.setCheck(0);
                        return e;
                    }).collect(toList());
            return this.paginating(dtoList,currentPage, pageSize);
        }

        // 获取api服务Id集合
        List<Integer> userApiConfigureIdList = userConfigureList.stream().map(e -> e.getConfigureId()).collect(toList());
        if (CollectionUtils.isEmpty(userApiConfigureIdList)) {
            return null;
        }

        // 不是用户下的服务api
        QueryWrapper<ApiConfigurePO> query = new QueryWrapper<>();
        query.lambda()
                .notIn(ApiConfigurePO::getId, userApiConfigureIdList);
        List<ApiConfigurePO> configureList = apiConfigureMapper.selectList(query);

        return this.mergeList(userConfigureList, configureList, currentPage, pageSize);
    }

    @Override
    public List<UserApiDTO> configureByUserId(Integer id) {
        if (id == null) {
            return null;
        }

        QueryWrapper<MiddleConfigurePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(MiddleConfigurePO::getUserId, id)
                .select(MiddleConfigurePO::getUserId, MiddleConfigurePO::getConfigureId);
        List<MiddleConfigurePO> configureList = middleMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(configureList)) {
            return null;
        }

        return configureList.stream()
                .map(e -> {
                    UserApiDTO dto = new UserApiDTO();
                    dto.setUserId(e.getUserId());
                    dto.setConfigureId(e.getConfigureId());
                    return dto;
                }).collect(toList());
    }

    /**
     * 查询出来的Api集合进行合并
     *
     * @param userConfigureList 用户Api集合
     * @param configureList     非用户Api集合
     * @return
     */
    public Page<ConfigureDTO> mergeList(List<MiddleConfigurePO> userConfigureList, List<ApiConfigurePO> configureList,
                                        Integer currentPage, Integer pageSize) {

        List<Integer> userConfigureIdList = userConfigureList.stream().map(e -> e.getConfigureId()).collect(toList());

        List<ConfigureDTO> userConfigureApiList = ConfigureUserMap.INSTANCES.poToDto(this.queryApi(userConfigureIdList))
                .stream().map(e -> {
                    e.setCheck(1);
                    return e;
                }).collect(toList());

        List<ConfigureDTO> apiConfigureList = ConfigureUserMap.INSTANCES.poToDto(configureList)
                .stream().map(e -> {
                    e.setCheck(0);
                    return e;
                }).collect(toList());

        List<ConfigureDTO> dtoList = new ArrayList<>();
        dtoList.addAll(userConfigureApiList);
        dtoList.addAll(apiConfigureList);

        return paginating(dtoList, currentPage, pageSize);
    }

    /**
     * 分页
     *
     * @param dtoList
     * @param currentPage
     * @param pageSize
     * @return
     */
    public Page<ConfigureDTO> paginating(List<ConfigureDTO> dtoList,
                                         Integer currentPage,
                                         Integer pageSize) {
        // 总条数
        int total = dtoList.size();

        Page<ConfigureDTO> configureDTOPage = new Page<>();
        configureDTOPage.setRecords(startPage(dtoList, currentPage, pageSize));
        configureDTOPage.setCurrent(currentPage);
        configureDTOPage.setSize(pageSize);
        configureDTOPage.setTotal(total);
        return configureDTOPage;
    }

    /**
     * 根据id查询对应的Api服务
     *
     * @param ConfigureIdList
     * @return
     */
    public List<ApiConfigurePO> queryApi(List<Integer> ConfigureIdList) {
        QueryWrapper<ApiConfigurePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(ApiConfigurePO::getId, ConfigureIdList)
                .orderByDesc(ApiConfigurePO::getCreateTime);
        return apiConfigureMapper.selectList(queryWrapper);
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
     *
     * @param userId 用户id
     */
    public ConfigureUserPO userExistent(Integer userId) {
        return configureUserMapper.selectById(userId);
    }
}
