package com.fisk.datagovern.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovern.dto.category.CategoryDTO;
import com.fisk.datagovern.entity.CategoryPO;
import com.fisk.datagovern.map.CategoryMap;
import com.fisk.datagovern.mapper.CategoryMapper;
import com.fisk.datagovern.service.ICategory;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
public class CategoryImpl implements ICategory {

    @Resource
    CategoryMapper mapper;

    @Override
    public ResultEnum addCategory(CategoryDTO dto)
    {
        QueryWrapper<CategoryPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(CategoryPO::getCategoryCnName,dto.categoryCnName);
        CategoryPO po=mapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        return mapper.insert(CategoryMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum delCategory(int id)
    {
        CategoryPO po=mapper.selectById(id);
        if (po==null)
        {
            return  ResultEnum.DATA_NOTEXISTS;
        }
        return  mapper.deleteByIdWithFill(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateCategory(CategoryDTO dto)
    {
        CategoryPO po=mapper.selectById(dto.id);
        if (po==null)
        {
            return  ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<CategoryPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(CategoryPO::getCategoryCnName,dto.categoryCnName);
        CategoryPO model=mapper.selectOne(queryWrapper);
        if (model !=null && model.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        po=CategoryMap.INSTANCES.dtoToPo(dto);
        return mapper.updateById(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public CategoryDTO getCategoryDetail(int id)
    {
        CategoryPO po=mapper.selectById(id);
        if (po==null)
        {
            return null;
        }
        return CategoryMap.INSTANCES.poToDto(po);
    }

}
