package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.RowSecurityConfigDTO;
import com.fisk.datagovernance.entity.datasecurity.RowSecurityConfigPO;
import com.fisk.datagovernance.mapper.datasecurity.RowSecurityConfigMapper;
import com.fisk.datagovernance.service.datasecurity.RowSecurityConfigService;
import org.springframework.stereotype.Service;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Service
public class RowSecurityConfigServiceImpl extends ServiceImpl<RowSecurityConfigMapper, RowSecurityConfigPO> implements RowSecurityConfigService {


    @Override
    public RowSecurityConfigDTO getData(long id) {
        return null;
    }

    @Override
    public ResultEnum addData(RowSecurityConfigDTO dto) {



        return null;
    }

    @Override
    public ResultEnum editData(RowSecurityConfigDTO dto) {
        return null;
    }

    @Override
    public ResultEnum deleteData(long id) {
        return null;
    }

    @Override
    public ResultEnum editDefaultConfig(long defaultConfig) {

        UpdateWrapper updateWrapper = new UpdateWrapper();
        // 修改表中default_config这一列的数据
        updateWrapper.set("default_config", defaultConfig);
        return baseMapper.update(null, updateWrapper) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }
}