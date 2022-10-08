package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.savepointhistory.SavepointHistoryDTO;
import com.fisk.dataaccess.entity.SavepointHistoryPO;
import com.fisk.dataaccess.map.SavepointHistoryMap;
import com.fisk.dataaccess.mapper.SavepointHistoryMapper;
import com.fisk.dataaccess.service.ISavepointHistory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class SavepointHistoryImpl implements ISavepointHistory {

    @Resource
    SavepointHistoryMapper mapper;

    @Override
    public ResultEnum addSavepointHistory(SavepointHistoryDTO dto) {
        return mapper.insert(SavepointHistoryMap.INSTANCES.dtoToPo(dto)) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<SavepointHistoryDTO> getSavepointHistory(long tableAccessId) {
        QueryWrapper<SavepointHistoryPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("savepoint_date")
                .lambda()
                .eq(SavepointHistoryPO::getTableAccessId, tableAccessId);
        List<SavepointHistoryPO> poList = mapper.selectList(queryWrapper);
        return SavepointHistoryMap.INSTANCES.poListToDtoList(poList);
    }

}
