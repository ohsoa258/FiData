package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.dto.tableapi.TableApiParameterDTO;
import com.fisk.dataservice.entity.TableApiParameterPO;
import com.fisk.dataservice.map.TableApiParameterMap;
import com.fisk.dataservice.mapper.TableApiParameterMapper;
import com.fisk.dataservice.service.ITableApiParameterService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("tableApiParameterService")
public class TableApiParameterServiceImpl extends ServiceImpl<TableApiParameterMapper, TableApiParameterPO> implements ITableApiParameterService {


    @Override
    public ResultEnum savetTableApiParameter(List<TableApiParameterDTO> dto,long apiId) {
        LambdaQueryWrapper<TableApiParameterPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiParameterPO::getApiId,apiId);
        remove(queryWrapper);
        Map<Integer,Integer> map = new HashMap<>();
        List<TableApiParameterDTO> fristSave = dto.stream().map(i -> {
            i.setCopyId(i.getId());
            i.setCopyPid(i.getPid());
            TableApiParameterPO tableApiParameterPO = TableApiParameterMap.INSTANCES.dtoToPo(i);
            save(tableApiParameterPO);
            i.setId((int)tableApiParameterPO.getId());
            map.put(i.getCopyId(),i.getId());
            return i;
        }).collect(Collectors.toList());
        for (TableApiParameterDTO tableApiParameterDTO : fristSave) {
            if (tableApiParameterDTO.getCopyPid() != 0){
                tableApiParameterDTO.setPid(map.get(tableApiParameterDTO.getCopyPid()));
                TableApiParameterPO tableApiParameterPO = TableApiParameterMap.INSTANCES.dtoToPo(tableApiParameterDTO);
                updateById(tableApiParameterPO);
            }
        }
        return ResultEnum.SUCCESS;
    }
}
