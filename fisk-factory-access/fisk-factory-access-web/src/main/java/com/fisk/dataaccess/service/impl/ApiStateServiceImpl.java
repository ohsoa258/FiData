package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.dataaccess.dto.apistate.ApiStateDTO;
import com.fisk.dataaccess.entity.ApiStatePO;
import com.fisk.dataaccess.map.apistate.ApiStateMap;
import com.fisk.dataaccess.service.IApiStateService;
import com.fisk.dataaccess.mapper.ApiStateMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author 56263
 * @description 针对表【tb_api_state】的数据库操作Service实现
 * @createDate 2023-08-29 10:44:39
 */
@Service
public class ApiStateServiceImpl extends ServiceImpl<ApiStateMapper, ApiStatePO>
        implements IApiStateService {

    @Resource
    private UserHelper userHelper;

    /**
     * 编辑api开启状态    save or update
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean editApiState(ApiStateDTO dto) {
        ApiStatePO apiStatePO = ApiStateMap.INSTANCES.dtoToPo(dto);
        UserInfo user = userHelper.getLoginUserInfo();
        apiStatePO.setCreateUser(String.valueOf(user.id));
        apiStatePO.setUpdateUser(String.valueOf(user.id));
        return saveOrUpdate(apiStatePO);
    }

    /**
     * 回显api是否开启状态 get
     *
     * @return
     */
    @Override
    public ApiStateDTO getApiState() {
        LambdaQueryWrapper<ApiStatePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ApiStatePO::getId)
                .last("limit 1");
        ApiStatePO one = getOne(wrapper);

        return ApiStateMap.INSTANCES.poToDto(one);
    }
}




