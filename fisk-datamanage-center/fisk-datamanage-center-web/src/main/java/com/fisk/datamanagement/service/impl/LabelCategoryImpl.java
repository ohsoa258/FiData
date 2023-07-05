package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.labelcategory.FirstLabelCategorySummaryDto;
import com.fisk.datamanagement.dto.labelcategory.LabelCategoryDTO;
import com.fisk.datamanagement.dto.labelcategory.LabelCategoryDataDTO;
import com.fisk.datamanagement.dto.labelcategory.LabelCategoryPathDto;
import com.fisk.datamanagement.entity.BusinessClassificationPO;
import com.fisk.datamanagement.entity.LabelCategoryPO;
import com.fisk.datamanagement.entity.LabelPO;
import com.fisk.datamanagement.entity.MetadataLabelMapPO;
import com.fisk.datamanagement.map.LabelCategoryMap;
import com.fisk.datamanagement.map.LabelMap;
import com.fisk.datamanagement.map.MetadataLabelMap;
import com.fisk.datamanagement.mapper.LabelCategoryMapper;
import com.fisk.datamanagement.mapper.MetadataLabelMapper;
import com.fisk.datamanagement.service.ILabelCategory;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 属性分类
 * @author JianWenYang
 */
@Service
public class LabelCategoryImpl
        extends ServiceImpl<LabelCategoryMapper, LabelCategoryPO>
        implements ILabelCategory {

    @Resource
    LabelCategoryMapper mapper;

    @Resource
    LabelImpl label;

    @Resource
    MetadataLabelMapper metadataLabelMapper;

    @Override
    public ResultEnum addLabelCategory(LabelCategoryDTO dto)
    {
        QueryWrapper<LabelCategoryPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(LabelCategoryPO::getCategoryCnName,dto.categoryCnName);
        LabelCategoryPO po=mapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DATA_EXISTS;
        }
        dto.categoryCode= UUID.randomUUID().toString();
        return mapper.insert(LabelCategoryMap.INSTANCES.dtoToPo(dto))>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum delLabelCategory(int id)
    {
        LabelCategoryPO po=mapper.selectById(id);
        if (po==null)
        {
            return  ResultEnum.DATA_NOTEXISTS;
        }
        return  mapper.deleteByIdWithFill(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateLabelCategory(LabelCategoryDTO dto)
    {
        LabelCategoryPO po=mapper.selectById(dto.id);
        if (po==null)
        {
            return  ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<LabelCategoryPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(LabelCategoryPO::getCategoryCnName,dto.categoryCnName);
        LabelCategoryPO model=mapper.selectOne(queryWrapper);
        if (model !=null && model.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        po= LabelCategoryMap.INSTANCES.dtoToPo(dto);
        return mapper.updateById(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public LabelCategoryDTO getCategoryDetail(int id)
    {
        LabelCategoryPO po=mapper.selectById(id);
        if (po==null)
        {
            return null;
        }
        return LabelCategoryMap.INSTANCES.poToDto(po);
    }

    @Override
    public List<LabelCategoryDataDTO> getLabelCategoryList(String queryName)
    {
        List<LabelCategoryDataDTO> dataList=new ArrayList<>();
        QueryWrapper<LabelCategoryPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(LabelCategoryPO::getCategoryParentCode,"1");
        //获取父节点
        List<LabelCategoryPO> list=mapper.selectList(queryWrapper);
        if (list!=null && list.size()>0)
        {
            dataList= LabelCategoryMap.INSTANCES.dataListPoToDto(list);
        }
        //获取子节点
        QueryWrapper<LabelCategoryPO> categoryPoQueryWrapper=new QueryWrapper<>();
        categoryPoQueryWrapper.lambda().ne(LabelCategoryPO::getCategoryParentCode,"1");
        List<LabelCategoryPO> childrenList=mapper.selectList(categoryPoQueryWrapper);
        List<LabelCategoryPO> childrenAllList=mapper.selectList(categoryPoQueryWrapper);
        if (childrenList==null || childrenList.size()==0)
        {
            return dataList;
        }
        //如果没有第一级，则找出子级中的第一级所有数据
        if (dataList ==null || dataList.size()==0)
        {
            dataList= LabelCategoryMap.INSTANCES.dataListPoToDto(getParentCode(childrenList));
        }
        //递归获取树形结构
        for (LabelCategoryDataDTO item :dataList)
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
     public LabelCategoryDataDTO buildChildTree(LabelCategoryDataDTO pNode, List<LabelCategoryPO> poList) {
         List<LabelCategoryDataDTO> list=new ArrayList<>();
         for (LabelCategoryPO item:poList)
         {
             if (item.getCategoryParentCode().equals(pNode.getCategoryCode()))
             {
                 list.add(buildChildTree(LabelCategoryMap.INSTANCES.dataPoToDto(item),poList));
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
     public List<LabelCategoryPO> getParentCode(List<LabelCategoryPO> listPo)
     {
         List<LabelCategoryPO> data=new ArrayList<>();
        for (LabelCategoryPO item:listPo)
        {
            List<LabelCategoryPO> pos=listPo.stream().filter(e->item.categoryParentCode.equals(e.categoryCode)).collect(Collectors.toList());
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
         QueryWrapper<LabelCategoryPO> queryWrapper=new QueryWrapper<>();
         List<LabelCategoryPO> list=mapper.selectList(queryWrapper);
         LabelCategoryPO po=mapper.selectById(categoryId);
         if (list==null || list.size()==0 || po==null)
         {
             return ids;
         }
         ids.add(categoryId);
         ids=getCategory(po,list,ids);
         return ids;
     }

     public List<Integer> getCategory(LabelCategoryPO po, List<LabelCategoryPO> list, List<Integer> ids){
         for (LabelCategoryPO item:list)
         {
             if (item.getCategoryParentCode().equals(po.getCategoryCode()))
             {
                 ids.add(Integer.parseInt(String.valueOf(item.id)));
                 getCategory(item,list,ids);
             }
         }
         return ids;
     }

    /**
     * 模糊查询获取属性分类
      * @param keyword
     * @return
     */
    public List<LabelCategoryDTO> queryLikeLabelCategory(String keyword){
        // 创建查询条件对象
        List<LabelCategoryPO> labelCategoryPOs= this.query().eq("del_flag",1).like(StringUtils.isNotEmpty(keyword), "category_cn_name", keyword).list();

        // 执行查询
        return LabelMap.INSTANCES.categoryPosToDtos(labelCategoryPOs);
    }

    /**
     * 获取所有属性分类并且带有完整路径
     * @return
     */
    public List<LabelCategoryPathDto> getLabelCategoryPathDto(){
       return mapper.getLabelCategoryPath();
    }

    /**
     * 获取第一级属性分类下的汇总数据
     * @return
     */
    @Override
    public List<FirstLabelCategorySummaryDto> getFirstLabelCategorySummary(){
        //获取所有属性分类
        List<LabelCategoryPO> allLabelCategoryPOList =this.query().list();
        //获取所有属性
        List<LabelPO> allLabelPOList=label.query().list();
        //获取所有属性关联的元数据
        QueryWrapper<MetadataLabelMapPO> metadataLabelMapPOQueryWrapper=new QueryWrapper<>();
        List<MetadataLabelMapPO> allMetadataLabelMapPOList= metadataLabelMapper.selectList(metadataLabelMapPOQueryWrapper);
        //返回结果集
        List<FirstLabelCategorySummaryDto> firstLabelCategorySummaryDtoList=new ArrayList<>();
        //获取第一级属性分分类
        List<LabelCategoryPO> firstLabelCategoryPOList=allLabelCategoryPOList.stream().filter(e->e.getCategoryParentCode().equals("1")).collect(Collectors.toList());
        for(LabelCategoryPO allLabelCategoryPO:firstLabelCategoryPOList){
            FirstLabelCategorySummaryDto firstLabelCategorySummaryDto= LabelCategoryMap.INSTANCES.poToFirstLabelCategorySummaryDto(allLabelCategoryPO);
            /****************************递归获取第一级下所有属性分类的Id******************************************/
            List<Long> labelCategoryChildrenId=getLabelCategoryChildrenIdList(allLabelCategoryPO.getCategoryCode(),allLabelCategoryPOList);
            /**********************************获取第一级下所有属性分类的属性**************************************/
            List<Long>  labelIdList=new ArrayList<>();
            if (labelCategoryChildrenId.size()>0){
                labelIdList=allLabelPOList.stream()
                        .filter(e->labelCategoryChildrenId.contains(Long.valueOf(e.getCategoryId())))
                        .map(LabelPO::getId).collect(Collectors.toList());
            }
            firstLabelCategorySummaryDto.setLabelSummary(labelIdList.size());
            /*****************************************获取第一级下所有属性分类属性关联的元数据**********************/
            Long metadataSummary=0L;
            if (labelIdList.size()>0){
                List<Long> finalLabelIdList = labelIdList;
                metadataSummary=allMetadataLabelMapPOList.stream().filter(e-> finalLabelIdList.contains(e.getLabelId())).count();
            }
            firstLabelCategorySummaryDto.setMetaEntitySummary(metadataSummary.intValue());
            firstLabelCategorySummaryDtoList.add(firstLabelCategorySummaryDto);
        }
        return firstLabelCategorySummaryDtoList;

    };


    /**
     * 获取业务分类下所有子集的ID
     * @param pCode
     * @param allLabelCategoryPOList
     * @return
     */
    public List<Long> getLabelCategoryChildrenIdList(String pCode ,List<LabelCategoryPO> allLabelCategoryPOList){
        List<Long> idList=new ArrayList<>();
        List<LabelCategoryPO> businessClassificationPOS =allLabelCategoryPOList.stream().filter(e->e.getCategoryParentCode().equals(pCode)).collect(Collectors.toList());
        if(businessClassificationPOS.stream().count()>0){
            idList.addAll(businessClassificationPOS.stream().map(e->e.getId()).collect(Collectors.toList()));
            businessClassificationPOS.forEach(e->{
                idList.addAll(getLabelCategoryChildrenIdList(e.getCategoryCode(),allLabelCategoryPOList));
            });
        }
        return  idList;
    }
}
