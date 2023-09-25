package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.tableapi.TableApiResultDTO;
import com.fisk.dataservice.entity.TableApiParameterPO;
import com.fisk.dataservice.entity.TableApiResultPO;
import com.fisk.dataservice.map.TableApiParameterMap;
import com.fisk.dataservice.map.TableApiResultMap;
import com.fisk.dataservice.mapper.TableApiResultMapper;
import com.fisk.dataservice.service.ITableApiResultService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("tableApiResultService")
public class TableApiResultServiceImpl extends ServiceImpl<TableApiResultMapper, TableApiResultPO> implements ITableApiResultService {


    @Override
    public ResultEnum saveApiResult(List<TableApiResultDTO> dto,long appId) {
        LambdaQueryWrapper<TableApiResultPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiResultPO::getAppId,appId);
        remove(queryWrapper);
        Map<Integer,Integer> map = new HashMap<>();
        List<TableApiResultDTO> fristSave = dto.stream().map(i -> {
            i.setAppId((int)appId);
            i.setCopyId(i.getId());
            i.setCopyPid(i.getPid());
            TableApiResultPO tableApiResultPO = TableApiResultMap.INSTANCES.dtoToPo(i);
            save(tableApiResultPO);
            i.setId((int)tableApiResultPO.getId());
            map.put(i.getCopyId(),i.getId());
            return i;
        }).collect(Collectors.toList());
        for (TableApiResultDTO tableApiResultDTO : fristSave) {
            if (tableApiResultDTO.getCopyPid() != 0){
                tableApiResultDTO.setPid(map.get(tableApiResultDTO.getCopyPid()));
                TableApiResultPO tableApiResultPO = TableApiResultMap.INSTANCES.dtoToPo(tableApiResultDTO);
                updateById(tableApiResultPO);
            }
        }
        return ResultEnum.SUCCESS;
    }
}
