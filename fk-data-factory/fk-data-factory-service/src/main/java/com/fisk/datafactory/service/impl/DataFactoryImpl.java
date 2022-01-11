package com.fisk.datafactory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.datafactory.dto.dataaccess.LoadDependDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import com.fisk.datafactory.mapper.NifiCustomWorkflowDetailMapper;
import com.fisk.datafactory.service.IDataFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/1/11 11:56
 */
@Service
public class DataFactoryImpl implements IDataFactory {

    @Resource
    NifiCustomWorkflowDetailMapper nifiCustomWorkflowDetailMapper;

    @Override
    public boolean loadDepend(LoadDependDTO dto) {

        QueryWrapper<NifiCustomWorkflowDetailPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(NifiCustomWorkflowDetailPO::getComponentType, dto.channelDataEnum.getName())
                .eq(NifiCustomWorkflowDetailPO::getTableId, String.valueOf(dto.tableId))
                .select(NifiCustomWorkflowDetailPO::getTableId);
        List<NifiCustomWorkflowDetailPO> list = nifiCustomWorkflowDetailMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return false;
        } else {
            return true;
        }
    }
}
