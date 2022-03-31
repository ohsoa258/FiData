package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.datasecurity.RowfilterConfigDTO;
import com.fisk.datagovernance.entity.datasecurity.RowfilterConfigPO;
import com.fisk.datagovernance.mapper.datasecurity.RowfilterConfigMapper;
import com.fisk.datagovernance.service.datasecurity.RowfilterConfigService;
import org.springframework.stereotype.Service;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-30 15:29:16
 */
@Service
public class RowfilterConfigServiceImpl extends ServiceImpl<RowfilterConfigMapper, RowfilterConfigPO> implements RowfilterConfigService {


    @Override
    public RowfilterConfigDTO getData(long id) {
        return null;
    }

    @Override
    public ResultEnum addData(RowfilterConfigDTO dto) {
        return null;
    }

    @Override
    public ResultEnum editData(RowfilterConfigDTO dto) {
        return null;
    }

    @Override
    public ResultEnum deleteData(long id) {
        return null;
    }
}