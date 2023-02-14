package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.category.CategoryDTO;
import com.fisk.datamanagement.dto.category.ChildrenCategoryDetailsDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryAnchorDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryAttributeDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryLibraryDTO;
import com.fisk.datamanagement.dto.term.TermDetailsDTO;
import com.fisk.datamanagement.entity.GlossaryLibraryPO;
import com.fisk.datamanagement.entity.GlossaryPO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.mapper.GlossaryLibraryMapper;
import com.fisk.datamanagement.mapper.GlossaryMapper;
import com.fisk.datamanagement.service.ICategory;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class CategoryImpl implements ICategory {

    @Resource
    GlossaryLibraryMapper glossaryLibraryMapper;
    @Resource
    UserHelper userHelper;
    @Resource
    GlossaryMapper glossaryMapper;

    @Resource
    AtlasClient atlasClient;

    @Value("${atlas.glossary.category}")
    public String category;

    @Override
    public ResultEnum addCategory(CategoryDTO dto)
    {
        // 校验术语分类名称
        if (StringUtils.isEmpty(dto.name)){
            throw new FkException(ResultEnum.ERROR, "术语分类名称不能为空");
        }

        // 校验术语库是否为空
        if (StringUtils.isEmpty(dto.anchor.getGlossaryGuid())){
            throw new FkException(ResultEnum.ERROR, "所属术语库id不能为空");
        }

        // 校验术语分类名称是否重复,所有术语下均不能重复
        QueryWrapper<GlossaryLibraryPO> qw = new QueryWrapper<>();
        qw.eq("name", dto.name).eq("del_flag", 1).isNotNull("pid");
        GlossaryLibraryPO preCategory = glossaryLibraryMapper.selectOne(qw);
        if (preCategory != null){
            throw new FkException(ResultEnum.ERROR, "术语分类名称不能重复");
        }

        // 校验所属术语库是否存在
        qw = new QueryWrapper<>();
        qw.eq("id", dto.anchor.glossaryGuid).eq("del_flag", 1).isNull("pid");
        GlossaryLibraryPO preLibrary = glossaryLibraryMapper.selectOne(qw);
        if (preLibrary == null){
            throw new FkException(ResultEnum.ERROR, "所属术语库不存在");
        }

        // 校验父级术语分类是否存在
        if(!StringUtils.isEmpty(dto.parentCategory.categoryGuid)){
            qw = new QueryWrapper<>();
            qw.eq("id", dto.parentCategory.categoryGuid).eq("del_flag", 1).isNotNull("pid");
            GlossaryLibraryPO preParentCategory = glossaryLibraryMapper.selectOne(qw);
            if (preParentCategory == null){
                throw new FkException(ResultEnum.ERROR, "所属术语父级分类不存在");
            }
        }

        // 新增术语分类
        GlossaryLibraryPO model = new GlossaryLibraryPO();
        model.setName(dto.name);
        if (!StringUtils.isEmpty(dto.parentCategory.categoryGuid)){
            model.setPid(Integer.parseInt(dto.parentCategory.categoryGuid));
        }else{
            model.setPid(Integer.parseInt(String.valueOf(preLibrary.getId())));
        }
        model.setShortDescription(dto.shortDescription);
        model.setLongDescription(dto.longDescription);
        model.setCreateUser(userHelper.getLoginUserInfo().id.toString());
        if (glossaryLibraryMapper.insert(model) > 0){
            return ResultEnum.SUCCESS;
        }else {
            throw new FkException(ResultEnum.ERROR, "术语分类添加失败");
        }
    }

    @Override
    public ResultEnum deleteCategory(String guid)
    {
        // 查询是否存在
        QueryWrapper<GlossaryLibraryPO> qw = new QueryWrapper<>();
        qw.eq("id", guid).isNotNull("pid");
        GlossaryLibraryPO model = glossaryLibraryMapper.selectOne(qw);
        if (model == null){
            throw new FkException(ResultEnum.ERROR, "术语类别不存在");
        }

        // 查询所有术语
        qw = new QueryWrapper<>();
        qw.isNotNull("pid");
        List<GlossaryLibraryPO> allData = glossaryLibraryMapper.selectList(qw);
        // 递归获取id
        List<String> idList  = recursionCategoryId(allData, guid, new ArrayList<>());
        idList.add(guid);

        // 批量删除
        if (glossaryLibraryMapper.deleteBatchIds(idList) > 0){
            return ResultEnum.SUCCESS;
        }else{
            throw new FkException(ResultEnum.ERROR, "术语类别删除失败");
        }
    }

    private List<String> recursionCategoryId(List<GlossaryLibraryPO> allData, String id, List<String> idList){
        for (GlossaryLibraryPO item : allData){
            if (String.valueOf(item.pid).equals(id)){
                idList.add(String.valueOf(item.id));
                recursionCategoryId(allData, String.valueOf(item.id), idList);
            }
        }
        return idList;
    }

    @Override
    public CategoryDTO getCategory(String guid)
    {
        // 查询当前术语类别是否存在
        QueryWrapper<GlossaryLibraryPO> qw = new QueryWrapper<>();
        qw.eq("id", guid);
        GlossaryLibraryPO libraryDto = glossaryLibraryMapper.selectOne(qw);
        if (libraryDto == null){
            throw new FkException(ResultEnum.ERROR, "术语类别不存在");
        }

        // 设置已查询的数据
        CategoryDTO data = new CategoryDTO();
        data.setGuid(String.valueOf(libraryDto.pid));
        data.setName(libraryDto.name);
        data.setShortDescription(libraryDto.shortDescription);
        data.setLongDescription(libraryDto.longDescription);

        // 设置全限定名
        qw = new QueryWrapper<>();
        qw.eq("del_flag", 1);
        List<GlossaryLibraryPO> allData = glossaryLibraryMapper.selectList(qw);

        GlossaryLibraryPO glPO = recursionData(allData, guid);
        if (glPO != null){
            data.setQualifiedName(libraryDto.name + "@" + glPO.name);
        }
        // 设置术语库id
        GlossaryAnchorDTO parent = new GlossaryAnchorDTO();
        parent.setGlossaryGuid(String.valueOf(glPO.getId()));
        data.setAnchor(parent);

        // 设置一级子集术语类别
        List<ChildrenCategoryDetailsDTO> children = new ArrayList<>();
        for (GlossaryLibraryPO item : allData){
            if (!StringUtils.isEmpty(item.pid) && String.valueOf(item.pid).equals(guid)){
                ChildrenCategoryDetailsDTO detailsDTO = new ChildrenCategoryDetailsDTO();
                detailsDTO.categoryGuid = String.valueOf(item.id);
                detailsDTO.displayText = item.name;
                detailsDTO.parentCategoryGuid = String.valueOf(glPO.id);
                children.add(detailsDTO);
            }
        }
        data.setChildrenCategories(children);

        // 查询术语类别下的术语
        QueryWrapper<GlossaryPO> gQw = new QueryWrapper<>();
        gQw.eq("glossary_library_id", guid);
        List<GlossaryPO> termPoList = glossaryMapper.selectList(gQw);
        if (!CollectionUtils.isEmpty(termPoList)){
            List<TermDetailsDTO> termList = termPoList.stream().map(item -> {
                TermDetailsDTO dto = new TermDetailsDTO();
                dto.setDisplayText(item.name);
                dto.setTermGuid(String.valueOf(item.id));
                return dto;
            }).collect(Collectors.toList());
            data.setTerms(termList);
        }

        return data;
    }

    private GlossaryLibraryPO recursionData(List<GlossaryLibraryPO> allData, String guid){
        for (GlossaryLibraryPO item : allData){
            if (guid.equals(String.valueOf(item.id)) && item.pid != null){
                // pid不为空则递归
                recursionData(allData, String.valueOf(item.pid));
            }
            return item;
        }
        return null;
    }

    @Override
    public ResultEnum updateCategory(CategoryDTO dto)
    {
        // 校验名称是否为空
        if (StringUtils.isEmpty(dto.name)){
            throw new FkException(ResultEnum.ERROR, "术语类别名称不能为空");
        }

        // 查询名称是否重复
        QueryWrapper<GlossaryLibraryPO> qw = new QueryWrapper<>();
        qw.isNotNull("pid").eq("name", dto.name);
        GlossaryLibraryPO model = glossaryLibraryMapper.selectOne(qw);
        if (model != null && !String.valueOf(model.getId()).equals(dto.getGuid())){
            throw new FkException(ResultEnum.ERROR, "术语类别名称已存在");
        }

        // 查询就数据
        qw = new QueryWrapper<>();
        qw.eq("id", dto.guid);
        GlossaryLibraryPO currModel = glossaryLibraryMapper.selectOne(qw);
        currModel.setName(dto.name);
        currModel.setShortDescription(dto.shortDescription);
        currModel.setLongDescription(dto.longDescription);
        currModel.setUpdateUser(userHelper.getLoginUserInfo().id.toString());
        if (glossaryLibraryMapper.updateById(currModel) > 0){
            return ResultEnum.SUCCESS;
        }else{
            throw new FkException(ResultEnum.ERROR, "术语分类修改失败");
        }
    }



}
