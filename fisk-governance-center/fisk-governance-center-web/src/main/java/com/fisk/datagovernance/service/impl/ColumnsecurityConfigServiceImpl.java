package com.fisk.datagovernance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.ColumnsecurityConfigDTO;
import com.fisk.datagovernance.entity.datasecurity.ColumnsecurityConfigPO;
import com.fisk.datagovernance.mapper.ColumnsecurityConfigMapper;
import com.fisk.datagovernance.service.ColumnsecurityConfigService;
import org.springframework.stereotype.Service;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Service
public class ColumnsecurityConfigServiceImpl extends ServiceImpl<ColumnsecurityConfigMapper, ColumnsecurityConfigPO> implements ColumnsecurityConfigService {


    @Override
    public ColumnsecurityConfigDTO getData(long id) {
        return null;
    }

    @Override
    public ResultEnum addData(ColumnsecurityConfigDTO dto) {
        return null;
    }

    @Override
    public ResultEnum editData(ColumnsecurityConfigDTO dto) {
        return null;
    }

    @Override
    public ResultEnum deleteData(long id) {
        return null;
    }
}