package com.fisk.datagovern.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovern.dto.category.CategoryDTO;
import com.fisk.datagovern.dto.category.CategoryDataDTO;
import com.fisk.datagovern.entity.CategoryPO;
import com.fisk.datagovern.map.CategoryMap;
import com.fisk.datagovern.mapper.CategoryMapper;
import com.fisk.datagovern.service.ICategory;
import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        dto.categoryCode= UUID.randomUUID().toString();
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

    @Override
    public List<CategoryDataDTO> getCategoryList(String queryName)
    {
        List<CategoryDataDTO> dataList=new ArrayList<>();
        QueryWrapper<CategoryPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(CategoryPO::getCategoryParentCode,"1");
        if (StringUtils.isNotEmpty(queryName))
        {
            queryWrapper.lambda().like(CategoryPO::getCategoryCnName,queryName);
        }
        //获取父节点
        List<CategoryPO> list=mapper.selectList(queryWrapper);
        if (list!=null && list.size()>0)
        {
            dataList=CategoryMap.INSTANCES.dataListPoToDto(list);
        }
        //获取子节点
        QueryWrapper<CategoryPO> categoryPOQueryWrapper=new QueryWrapper<>();
        categoryPOQueryWrapper.lambda().ne(CategoryPO::getCategoryParentCode,"1");
        List<CategoryPO> childrenList=mapper.selectList(categoryPOQueryWrapper);
        List<CategoryPO> childrenAllList=mapper.selectList(categoryPOQueryWrapper);
        if (childrenList==null || childrenList.size()==0)
        {
            return dataList;
        }
        //如果没有第一级，则找出子级中的第一级所有数据
        if (dataList ==null || dataList.size()==0)
        {
            if (StringUtils.isNotEmpty(queryName))
            {
                childrenList=childrenList.stream().filter(e->e.categoryCnName.contains(queryName)).collect(Collectors.toList());
            }
            dataList=CategoryMap.INSTANCES.dataListPoToDto(getParentCode(childrenList));
        }
        //递归获取树形结构
        for (CategoryDataDTO item :dataList)
        {
            item=buildChildTree(item,childrenAllList);
        }
        return dataList;
    }

    /**
     * 递归，建立子树形结构
     * @param pNode
     * @return
     */
     public CategoryDataDTO buildChildTree(CategoryDataDTO pNode,List<CategoryPO> poList) {
         List<CategoryDataDTO> list=new ArrayList<>();
         for (CategoryPO item:poList)
         {
             if (item.getCategoryParentCode().equals(pNode.getCategoryCode()))
             {
                 list.add(buildChildTree(CategoryMap.INSTANCES.dataPoToDto(item),poList));
             }
         }
         pNode.childrenDto=list;
         return pNode;
     }

    /**
     * 筛选父级CategoryPO
     * @param listPo
     * @return
     */
     public List<CategoryPO> getParentCode(List<CategoryPO> listPo)
     {
         List<CategoryPO> data=new ArrayList<>();
        for (CategoryPO item:listPo)
        {
            List<CategoryPO> pos=listPo.stream().filter(e->item.categoryParentCode.equals(e.categoryCode)).collect(Collectors.toList());
            if (pos !=null && pos.size()>0)
            {
                continue;
            }
            data.add(item);
        }
        return data;
     }

     public List<Integer> getCategoryIds(int categoryId)
     {
         List<Integer> ids=new ArrayList<>();
         //获取选中类目下所有标签的id
         QueryWrapper<CategoryPO> queryWrapper=new QueryWrapper<>();
         List<CategoryPO> list=mapper.selectList(queryWrapper);
         CategoryPO po=mapper.selectById(categoryId);
         if (list==null || list.size()==0 || po==null)
         {
             return ids;
         }
         ids.add(categoryId);
         ids=getCategory(po,list,ids);
         return ids;
     }

     public List<Integer> getCategory(CategoryPO po,List<CategoryPO> list,List<Integer> ids){
         for (CategoryPO item:list)
         {
             if (item.getCategoryParentCode().equals(po.getCategoryCode()))
             {
                 ids.add(Integer.parseInt(String.valueOf(item.id)));
                 getCategory(item,list,ids);
             }
         }
         return ids;
     }


}
