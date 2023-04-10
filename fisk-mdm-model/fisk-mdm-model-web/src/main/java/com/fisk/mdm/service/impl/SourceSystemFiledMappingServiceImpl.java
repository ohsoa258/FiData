package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.mathingrules.AddSourceSystemFiledMappingDto;
import com.fisk.mdm.entity.SourceSystemFiledMappingPO;
import com.fisk.mdm.map.SourceSystemFiledMappingMap;
import com.fisk.mdm.mapper.SourceSystemFiledMappingMapper;
import com.fisk.mdm.service.ISourceSystemFiledMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JinXingWang
 */
@Service
@Slf4j
public class SourceSystemFiledMappingServiceImpl
        extends ServiceImpl<SourceSystemFiledMappingMapper, SourceSystemFiledMappingPO>
        implements ISourceSystemFiledMappingService {

    @Resource
    SourceSystemFiledMappingMapper mapper;

    @Override
    public ResultEnum save(List<AddSourceSystemFiledMappingDto> dtoList) {
        List<Long> sourceSystemMappingId=new ArrayList<>();
        List<SourceSystemFiledMappingPO> sourceSystemFiledMappingPOList=new ArrayList<>();
        dtoList.stream().forEach(e->{
            sourceSystemMappingId.add(e.sourceSystemMappingId);
            List<SourceSystemFiledMappingPO> sourceSystemFiledPOListNoId= SourceSystemFiledMappingMap.INSTANCES.dtoToPoList(e.sourceSystemFiledMappingDtoList);
            sourceSystemFiledPOListNoId.stream().forEach(s->{
                s.setSourceSystemMappingId(e.sourceSystemMappingId);
            });
            sourceSystemFiledMappingPOList.addAll(sourceSystemFiledPOListNoId);

        });
        delete(sourceSystemMappingId);
        boolean sucess= this.saveBatch(sourceSystemFiledMappingPOList);
        return sucess?ResultEnum.SUCCESS:ResultEnum.ERROR;
    }

    public ResultEnum delete(List<Long> sourceSystemMappingId){
        LambdaQueryWrapper<SourceSystemFiledMappingPO> queryWrapper= new  LambdaQueryWrapper<>();
        queryWrapper
                .in(SourceSystemFiledMappingPO::getSourceSystemMappingId,sourceSystemMappingId);
        Integer deleteRow= mapper.delete(queryWrapper);
        return ResultEnum.SUCCESS;
    }
}
