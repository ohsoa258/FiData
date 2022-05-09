package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.stgbatch.StgBatchDTO;
import com.fisk.mdm.entity.StgBatchPO;
import com.fisk.mdm.map.StgBatchMap;
import com.fisk.mdm.mapper.StgBatchMapper;
import com.fisk.mdm.service.IStgBatchService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
public class StgBatchServiceImpl
        extends ServiceImpl<StgBatchMapper, StgBatchPO>
        implements IStgBatchService {

    @Resource
    StgBatchMapper mapper;

    @Override
    public ResultEnum addStgBatch(StgBatchDTO dto)
    {
        return mapper.insert(StgBatchMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

}
