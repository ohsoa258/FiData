package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.UsergroupInfoDTO;
import com.fisk.datagovernance.entity.datasecurity.UsergroupInfoPO;
import com.fisk.datagovernance.mapper.datasecurity.UsergroupInfoMapper;
import com.fisk.datagovernance.service.datasecurity.UsergroupInfoService;
import org.springframework.stereotype.Service;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Service
public class UsergroupInfoServiceImpl extends ServiceImpl<UsergroupInfoMapper, UsergroupInfoPO> implements UsergroupInfoService {


    @Override
    public UsergroupInfoDTO getData(long id) {
        return null;
    }

    @Override
    public ResultEnum addData(UsergroupInfoDTO dto) {
        return null;
    }

    @Override
    public ResultEnum editData(UsergroupInfoDTO dto) {
        return null;
    }

    @Override
    public ResultEnum deleteData(long id) {
        return null;
    }
}