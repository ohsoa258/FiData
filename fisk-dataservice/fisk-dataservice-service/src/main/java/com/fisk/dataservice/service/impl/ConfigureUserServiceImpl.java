package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.UserDTO;
import com.fisk.dataservice.map.ConfigureUserMap;
import com.fisk.dataservice.vo.UserVO;
import com.fisk.dataservice.entity.ApiConfigurePO;
import com.fisk.dataservice.entity.ConfigureUserPO;
import com.fisk.dataservice.entity.MiddleConfigurePO;
import com.fisk.dataservice.map.ApiConfigureFieldMap;
import com.fisk.dataservice.mapper.ApiConfigureMapper;
import com.fisk.dataservice.mapper.ConfigureUserMapper;
import com.fisk.dataservice.mapper.MiddleConfigureMapper;
import com.fisk.dataservice.service.ConfigureUserService;
import org.springframework.stereotype.Service;
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
    public List<UserVO> listData(Page<ConfigureUserPO> page) {
        List<ConfigureUserPO> userList = configureUserMapper.selectPage(page, null).getRecords();

        List<UserVO> userVOList = new ArrayList<>();
        for (ConfigureUserPO configureUser : userList) {
            UserVO user = ApiConfigureFieldMap.INSTANCES.poToDto(configureUser);

            // 查询出用户对应服务表ID
            QueryWrapper<MiddleConfigurePO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(MiddleConfigurePO::getUserId, configureUser.getId());
            List<MiddleConfigurePO> configureList = middleMapper.selectList(queryWrapper);
            for (MiddleConfigurePO middleConfigure : configureList) {
                QueryWrapper<ApiConfigurePO> query = new QueryWrapper<>();
                query.lambda()
                        .eq(ApiConfigurePO::getId , middleConfigure.getConfigureId())
                        .select(ApiConfigurePO::getApiName);
                ApiConfigurePO apiConfigure = apiConfigureMapper.selectOne(query);
                user.setConfigureName(apiConfigure.getApiName());
                userVOList.add(user);
            }
        }
        return userVOList;
    }

    @Override
    public ResultEnum saveUser(ConfigureUserPO po,String apiName) {
        if (StringUtils.isEmpty(po)) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        QueryWrapper<ConfigureUserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ConfigureUserPO::getUserName, po.getUserName())
                .eq(ConfigureUserPO::getPassword, po.getPassword()).last("limit 1");
        ConfigureUserPO selectOne = configureUserMapper.selectOne(queryWrapper);
        if (selectOne != null){
            return ResultEnum.DATA_EXISTS;
        }

        if (configureUserMapper.insert(po) <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        MiddleConfigurePO middleConfigure = new MiddleConfigurePO();
        middleConfigure.setUserId(Integer.parseInt(String.valueOf(po.getId())));
        middleConfigure.setConfigureId(obtainId(apiName));
        return middleMapper.insert(middleConfigure) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateUser(UserDTO dto) {
        ConfigureUserPO configureUser = configureUserMapper.selectById(dto.id);
        if (configureUser == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        ConfigureUserPO user = ConfigureUserMap.INSTANCES.dtoToPo(dto);
        return configureUserMapper.updateById(user)> 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteUserById(Integer id) {
        if (id == null){
            return ResultEnum.PARAMTER_NOTNULL;
        }

        ConfigureUserPO configureUser = configureUserMapper.selectById(id);
        if (StringUtils.isEmpty(configureUser)){
            return ResultEnum.DATA_NOTEXISTS;
        }
        if (configureUserMapper.deleteById(id) <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }
        return middleMapper.deleteById(id) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 根据apiName获取对应ID
     * @param apiName
     * @return
     */
    public Integer obtainId(String apiName){
        QueryWrapper<ApiConfigurePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ApiConfigurePO::getApiName, apiName)
                .select(ApiConfigurePO::getId);
        ApiConfigurePO apiConfigure = apiConfigureMapper.selectOne(queryWrapper);
        return Integer.parseInt(String.valueOf(apiConfigure.getId()));
    }
}
