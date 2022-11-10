package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.savepointhistory.SavepointHistoryDTO;
import com.fisk.dataaccess.entity.SavepointHistoryPO;
import com.fisk.dataaccess.map.SavepointHistoryMap;
import com.fisk.dataaccess.mapper.SavepointHistoryMapper;
import com.fisk.dataaccess.service.ISavepointHistory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

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

    /**
     * 获取检查点详情
     *
     * @param tableAccessId
     * @param savepointHistoryId
     * @return
     */
    public SavepointHistoryDTO getSavepointHistoryDetails(long tableAccessId, long savepointHistoryId) {
        List<SavepointHistoryDTO> savepointHistory = this.getSavepointHistory(tableAccessId);
        if (CollectionUtils.isEmpty(savepointHistory)) {
            return null;
        }
        //从最新检查点开始
        if (savepointHistoryId == 0) {
            return savepointHistory.get(0);
        }
        Optional<SavepointHistoryDTO> first = savepointHistory.stream().filter(e -> e.id.equals(savepointHistoryId)).findFirst();
        if (!first.isPresent()) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return first.get();
    }

}
