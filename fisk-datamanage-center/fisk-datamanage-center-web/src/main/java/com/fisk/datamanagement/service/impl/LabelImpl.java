package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.label.LabelDTO;
import com.fisk.datamanagement.dto.label.LabelDataDTO;
import com.fisk.datamanagement.dto.label.LabelQueryDTO;
import com.fisk.datamanagement.entity.CategoryPO;
import com.fisk.datamanagement.entity.LabelPO;
import com.fisk.datamanagement.map.LabelMap;
import com.fisk.datamanagement.mapper.LabelCategoryMapper;
import com.fisk.datamanagement.mapper.LabelMapper;
import com.fisk.datamanagement.service.ILabel;
import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class LabelImpl implements ILabel {


    @Resource
    LabelMapper mapper;
    @Resource
    LabelCategoryImpl category;
    @Resource
    LabelCategoryMapper categoryMapper;

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
        CategoryPO categoryPo=categoryMapper.selectById(po.categoryId);
        dto.categoryName=categoryPo==null?"":categoryPo.categoryCnName;
        return dto;
    }

    @Override
    public Page<LabelDataDTO> getLabelPageList(LabelQueryDTO dto)
    {
        String categoryIds="";
        List<Integer> ids=category.getCategoryIds(dto.categoryId);
        if (ids !=null && ids.size()!=0)
        {
            categoryIds="'";
            categoryIds+= StringUtils.join(ids, ",")+"'";
        }
        return mapper.queryPageList(dto.dto,categoryIds);
    }

    @Override
    public List<String> atlasGetLabel()
    {
        QueryWrapper<LabelPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("label_cn_name").orderByDesc("create_time");
        List<String> list=(List)mapper.selectObjs(queryWrapper);
        return list;
    }

}
