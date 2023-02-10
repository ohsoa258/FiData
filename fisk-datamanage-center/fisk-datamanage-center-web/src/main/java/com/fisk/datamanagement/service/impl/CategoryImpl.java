package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.category.CategoryDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryLibraryDTO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.mapper.GlossaryLibraryMapper;
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
        QueryWrapper<GlossaryLibraryDTO> qw = new QueryWrapper<>();
        qw.eq("name", dto.name).eq("del_flag", 1).isNotNull("pid");
        GlossaryLibraryDTO preCategory = glossaryLibraryMapper.selectOne(qw);
        if (preCategory != null){
            throw new FkException(ResultEnum.ERROR, "术语分类名称不能重复");
        }

        // 校验所属术语库是否存在
        qw = new QueryWrapper<>();
        qw.eq("id", dto.anchor.glossaryGuid).eq("del_flag", 1).isNull("pid");
        GlossaryLibraryDTO preLibrary = glossaryLibraryMapper.selectOne(qw);
        if (preLibrary == null){
            throw new FkException(ResultEnum.ERROR, "所属术语库不存在");
        }

        // 校验父级术语分类是否存在
        if(!StringUtils.isEmpty(dto.parentCategory.categoryGuid)){
            qw = new QueryWrapper<>();
            qw.eq("id", dto.parentCategory.categoryGuid).eq("del_flag", 1).isNotNull("pid");
            GlossaryLibraryDTO preParentCategory = glossaryLibraryMapper.selectOne(qw);
            if (preParentCategory == null){
                throw new FkException(ResultEnum.ERROR, "所属术语父级分类不存在");
            }
        }

        // 新增术语分类
        GlossaryLibraryDTO model = new GlossaryLibraryDTO();
        model.setName(dto.name);
        if (!StringUtils.isEmpty(dto.parentCategory.categoryGuid)){
            model.setPid(dto.parentCategory.categoryGuid);
        }else{
            model.setPid(preLibrary.id);
        }
        model.setShortDescription(dto.shortDescription);
        model.setLongDescription(dto.longDescription);
        model.setCreateTime(LocalDateTime.now());
        model.setCreateUser(userHelper.getLoginUserInfo().id.toString());
        return glossaryLibraryMapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.ERROR;
    }

    @Override
    public ResultEnum deleteCategory(String guid)
    {
        ResultDataDTO<String> result = atlasClient.delete(category + "/" + guid);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public CategoryDTO getCategory(String guid)
    {
        ResultDataDTO<String> result = atlasClient.get(category + "/" + guid);
        return JSONObject.parseObject(result.data,CategoryDTO.class);
    }

    @Override
    public ResultEnum updateCategory(CategoryDTO dto)
    {
        String jsonParameter = JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.put(category + "/" + dto.guid, jsonParameter);
        return atlasClient.newResultEnum(result);
    }



}
