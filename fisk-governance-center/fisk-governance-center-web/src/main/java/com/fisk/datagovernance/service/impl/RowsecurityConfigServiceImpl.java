package com.fisk.datagovernance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.RowsecurityConfigDTO;
import com.fisk.datagovernance.entity.datasecurity.RowsecurityConfigPO;
import com.fisk.datagovernance.mapper.RowsecurityConfigMapper;
import com.fisk.datagovernance.service.RowsecurityConfigService;
import org.springframework.stereotype.Service;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Service
public class RowsecurityConfigServiceImpl extends ServiceImpl<RowsecurityConfigMapper, RowsecurityConfigPO> implements RowsecurityConfigService {


    @Override
    public RowsecurityConfigDTO getData(long id) {
        return null;
    }

    @Override
    public ResultEnum addData(RowsecurityConfigDTO dto) {
        return null;
    }

    @Override
    public ResultEnum editData(RowsecurityConfigDTO dto) {
        return null;
    }

    @Override
    public ResultEnum deleteData(long id) {
        return null;
    }
}