package com.fisk.datagovern.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovern.dto.label.LabelDTO;
import com.fisk.datagovern.entity.LabelPO;
import com.fisk.datagovern.map.LabelMap;
import com.fisk.datagovern.mapper.LabelMapper;
import com.fisk.datagovern.service.ILabel;
import com.google.common.base.Joiner;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author JianWenYang
 */
@Service
public class LabelImpl implements ILabel {


    @Resource
    LabelMapper mapper;

    @Override
    public ResultEnum addLabel(LabelDTO dto)
    {
        QueryWrapper<LabelPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(LabelPO::getLabelCnName,dto.labelCnName);
        LabelPO po=mapper.selectOne(queryWrapper);
        if (po!=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        dto.applicationModule = Joiner.on(",").join(dto.moduleIds);
        return mapper.insert(LabelMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum delLabel(int id)
    {
        LabelPO po=mapper.selectById(id);
        if (po==null)
        {
            return  ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.deleteByIdWithFill(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateLabel(LabelDTO dto)
    {
        LabelPO po=mapper.selectById(dto.id);
        if (po==null)
        {
            return  ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<LabelPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(LabelPO::getLabelCnName,dto.labelCnName);
        LabelPO model=mapper.selectOne(queryWrapper);
        if (model !=null && model.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        dto.applicationModule = Joiner.on(",").join(dto.moduleIds);
        return mapper.updateById(LabelMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public LabelDTO getLabelDetail(int id)
    {
        LabelPO po = mapper.selectById(id);
        if (po == null) {
            return null;
        }
        LabelDTO dto = LabelMap.INSTANCES.poToDto(po);
        dto.moduleIds = Arrays.asList(dto.applicationModule.split(","));
        return dto;
    }

}
