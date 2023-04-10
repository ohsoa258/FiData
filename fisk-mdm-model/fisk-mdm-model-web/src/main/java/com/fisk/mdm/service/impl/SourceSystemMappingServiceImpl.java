package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.mathingrules.AddSourceSystemFiledMappingDto;
import com.fisk.mdm.dto.mathingrules.SourceSystemMappingDto;
import com.fisk.mdm.entity.SourceSystemMappingPO;
import com.fisk.mdm.map.SourceSystemMappingMap;
import com.fisk.mdm.mapper.SourceSystemMappingMapper;
import com.fisk.mdm.service.ISourceSystemFiledMappingService;
import com.fisk.mdm.service.ISourceSystemMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JinXingWang
 */
@Service
@Slf4j
public class SourceSystemMappingServiceImpl
        extends ServiceImpl<SourceSystemMappingMapper,SourceSystemMappingPO>
        implements ISourceSystemMappingService {

    @Resource
    SourceSystemMappingMapper mapper;

    @Resource
    ISourceSystemFiledMappingService sourceSystemFiledMappingService;


    @Override
    public ResultEnum save(long matchingRulesId, List<SourceSystemMappingDto> dtoList) {
        //删除源系统
        ResultEnum deleteResult= delete(matchingRulesId);
        //添加源系统
        List<SourceSystemMappingPO> sourceSystemMappingPOList= SourceSystemMappingMap.INSTANCES.dtoToPoList(dtoList);
        sourceSystemMappingPOList.stream().forEach(e->{
            e.setMatchingRulesId(matchingRulesId);
        });
        boolean success= this.saveBatch(sourceSystemMappingPOList);
        //添加源系统的字段映射
        List<AddSourceSystemFiledMappingDto> addSourceSystemFiledMappingDtoList=new ArrayList<>();
        sourceSystemMappingPOList.forEach(e->{
            SourceSystemMappingDto sourceSystemMappingDto=  dtoList.stream().filter(c->{
               return c.getSourceSystemId().equals(e.getSourceSystemId());
            }).collect(Collectors.toList()).get(0);
            AddSourceSystemFiledMappingDto  addSourceSystemFiledMappingDto=new  AddSourceSystemFiledMappingDto();
            addSourceSystemFiledMappingDto.setSourceSystemMappingId(e.getId());
            addSourceSystemFiledMappingDto.setSourceSystemFiledMappingDtoList(sourceSystemMappingDto.getSourceSystemFiledMappingDtoList());
            addSourceSystemFiledMappingDtoList.add(addSourceSystemFiledMappingDto);
        });
        ResultEnum  sourceSystemFiledSuccess= sourceSystemFiledMappingService.save(addSourceSystemFiledMappingDtoList);
        return (deleteResult==ResultEnum.SUCCESS&&success&&sourceSystemFiledSuccess==ResultEnum.SUCCESS)?ResultEnum.SUCCESS:ResultEnum.ERROR;
    }

    @Override
    public ResultEnum delete(long matchingRulesId) {
        LambdaQueryWrapper<SourceSystemMappingPO> queryWrapper= new  LambdaQueryWrapper<>();
        queryWrapper
                .eq(SourceSystemMappingPO::getMatchingRulesId,matchingRulesId);
        Integer deleteRow= mapper.delete(queryWrapper);
        return ResultEnum.SUCCESS;
    }
}
