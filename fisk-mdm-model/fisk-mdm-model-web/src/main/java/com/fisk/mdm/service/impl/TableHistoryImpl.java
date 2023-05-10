package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.access.TableHistoryDTO;
import com.fisk.mdm.entity.TableHistoryPO;
import com.fisk.mdm.map.TableHistoryMap;
import com.fisk.mdm.mapper.TableHistoryMapper;
import com.fisk.mdm.service.ITableHistory;
import com.fisk.task.client.PublishTaskClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author Lock
 */
@Service
public class TableHistoryImpl extends ServiceImpl<TableHistoryMapper, TableHistoryPO> implements ITableHistory {

    @Resource
    TableHistoryMapper mapper;
    @Resource
    PublishTaskClient publishTaskClient;

    @Override
    public long addTableHistory(List<TableHistoryDTO> dto) {
        dto.stream().filter(Objects::nonNull)
                .forEach(e -> {
                    if (e.openTransmission) {
                        e.remark = e.remark + " --> 已同步";
                    } else {
                        e.remark = e.remark + " --> 未同步";
                    }
                });
        if (!CollectionUtils.isEmpty(dto)) {
            //虽然用的list,实际上一张表发布仅有一条日志
            TableHistoryPO tableHistory = TableHistoryMap.INSTANCES.dtoToPo(dto.get(0));
            this.save(tableHistory);
            return tableHistory.id;
        } else {
            throw new FkException(ResultEnum.ERROR, "添加发布历史失败");
        }
    }

    @Override
    public List<TableHistoryDTO> getTableHistoryList(TableHistoryDTO dto) {
        QueryWrapper<TableHistoryPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TableHistoryPO::getTableId, dto.tableId)
                .eq(TableHistoryPO::getTableType, dto.tableType);
        List<TableHistoryDTO> list = TableHistoryMap.INSTANCES.poListToDtoList(mapper.selectList(queryWrapper));
        list.forEach(
                e -> {
                    ResultEntity<List<String>> resultEntity = publishTaskClient.getPipelStates(e.subRunId);
                    if (Objects.equals(resultEntity.code, ResultEnum.SUCCESS.getCode())) {
                        e.msg = resultEntity.data;
                    }
                }
        );
        return list;
    }

}
