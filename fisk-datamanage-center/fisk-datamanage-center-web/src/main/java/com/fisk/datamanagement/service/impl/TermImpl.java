package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.category.CategoryDetailsDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryAnchorDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryLibraryDTO;
import com.fisk.datamanagement.dto.glossary.NewGlossaryDTO;
import com.fisk.datamanagement.dto.term.TermAssignedEntities;
import com.fisk.datamanagement.dto.term.TermDTO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.mapper.GlossaryLibraryMapper;
import com.fisk.datamanagement.mapper.GlossaryMapper;
import com.fisk.datamanagement.service.ITerm;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author JianWenYang
 */
@Service
public class TermImpl implements ITerm {

    @Resource
    GlossaryLibraryMapper glossaryLibraryMapper;

    @Resource
    UserHelper userHelper;

    @Resource
    GlossaryMapper glossaryMapper;

    @Value("${atlas.glossary.term}")
    public String term;
    @Value("${atlas.glossary.terms}")
    public String terms;
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    EntityImpl entity;
    @Resource
    AtlasClient atlasClient;

    @Override
    public ResultEnum addTerm(TermDTO dto)
    {
        // 校验数据
        if (StringUtils.isEmpty(dto.name)){
            throw new FkException(ResultEnum.ERROR, "术语名称不能为空");
        }

        // 校验术语库，术语类别是否存在
        String categoryId = dto.categories.get(0).categoryGuid;
        if (StringUtils.isEmpty(categoryId)){
            throw new FkException(ResultEnum.ERROR, "所属术语类别id不能为空");
        }

        // 查询是否重复
        QueryWrapper<GlossaryLibraryDTO> qw = new QueryWrapper<>();
        qw.eq("id", categoryId).eq("del_flag", 1);
        GlossaryLibraryDTO glossaryLibraryDTO = glossaryLibraryMapper.selectOne(qw);
        if (glossaryLibraryDTO == null){
            throw new FkException(ResultEnum.ERROR, "所属术语类别不存");
        }

        // 存储术语
        NewGlossaryDTO model = new NewGlossaryDTO();
        model.setName(dto.name);
        model.setShortDescription(dto.shortDescription);
        model.setLongDescription(dto.longDescription);
        model.setGlossaryLibraryId(categoryId);
        model.setCreateTime(LocalDateTime.now());
        model.setCreateUser(userHelper.getLoginUserInfo().id.toString());

        if (glossaryMapper.insert(model) > 0){
            return ResultEnum.SUCCESS;
        }else {
            throw new FkException(ResultEnum.ERROR, "新增术语失败");
        }
    }

    @Override
    public TermDTO getTerm(String guid)
    {
        // 查询术语
        QueryWrapper<NewGlossaryDTO> qw = new QueryWrapper<>();
        qw.eq("id", guid);
        NewGlossaryDTO model = glossaryMapper.selectOne(qw);
        if (model == null){
            throw new FkException(ResultEnum.ERROR, "数据不存在");
        }

        // 创建对象
        TermDTO dto = new TermDTO();

        // 查询所有术语库及类别
        List<GlossaryLibraryDTO> allData = glossaryLibraryMapper.selectList(new QueryWrapper<>());

        // 设置限定名、术语库id依赖
        GlossaryLibraryDTO recursionData = recursionData(allData, guid);
        GlossaryAnchorDTO gDto = new GlossaryAnchorDTO();
        dto.setGuid(model.id);
        dto.setShortDescription(model.shortDescription);
        dto.setLongDescription(model.longDescription);
        if (recursionData != null){
            gDto.setGlossaryGuid(recursionData.id);
            // todo relation暂时不明
            // gDto.setRelationGuid();
            dto.setAnchor(gDto);
            dto.setQualifiedName(model.name + "@" + recursionData.name);
        }

        // 设置所属术语类别
        CategoryDetailsDTO categoryDetailsDTO = new CategoryDetailsDTO();
        categoryDetailsDTO.setCategoryGuid(model.glossaryLibraryId);
        GlossaryLibraryDTO libraryDTO = allData.stream().filter(item -> item.id.equals(model.glossaryLibraryId)).findFirst().orElse(null);
        if (libraryDTO != null){
            categoryDetailsDTO.setDisplayText(libraryDTO.name);
        }
        dto.setCategories(Collections.singletonList(categoryDetailsDTO));
        return dto;
    }

    private GlossaryLibraryDTO recursionData(List<GlossaryLibraryDTO> allData, String guid){
        for (GlossaryLibraryDTO item : allData){
            if (guid.equals(item.id) && !StringUtils.isEmpty(item.pid)){
                // pid不为空则递归
                recursionData(allData, item.pid);
            }
            return item;
        }
        return null;
    }

    @Override
    public ResultEnum updateTerm(TermDTO dto)
    {
        // 校验数据
        if (StringUtils.isEmpty(dto.name)){
            throw new FkException(ResultEnum.ERROR, "术语名称不能为空");
        }

        // 查询术语分类是否存在
        String categoryId = dto.categories.get(0).categoryGuid;
        QueryWrapper<GlossaryLibraryDTO> qw = new QueryWrapper<>();
        qw.eq("id", categoryId).eq("del_flag", 1);
        GlossaryLibraryDTO category = glossaryLibraryMapper.selectOne(qw);
        if (category == null){
            throw new FkException(ResultEnum.ERROR, "所属术语类别不存在");
        }

        // 查询术语是否存在
        QueryWrapper<NewGlossaryDTO> qwTerm = new QueryWrapper<>();
        qwTerm.eq("id", dto.guid);
        NewGlossaryDTO model = glossaryMapper.selectOne(qwTerm);
        if (model == null){
            throw new FkException(ResultEnum.ERROR, "术语不存在");
        }

        // 查询术语名称是否重复
        qwTerm = new QueryWrapper<>();
        qwTerm.eq("glossary_library_id", categoryId).eq("name", dto.name);
        NewGlossaryDTO all = glossaryMapper.selectOne(qwTerm);
        if (all != null && !all.id.equals(dto.guid)){
            throw new FkException(ResultEnum.ERROR, "当前术语类别下术语名称已经存在");
        }

        model.setName(dto.name);
        model.setShortDescription(dto.shortDescription);
        model.setLongDescription(dto.longDescription);
        model.setUpdateTime(LocalDateTime.now());
        model.setUpdateUser(userHelper.getLoginUserInfo().id.toString());
        // 修改
        if (glossaryMapper.updateById(model) > 0){
            return ResultEnum.SUCCESS;
        }else{
            throw new FkException(ResultEnum.ERROR, "术语修改失败");
        }
    }

    @Override
    public ResultEnum deleteTerm(String guid)
    {
        // 查询是否存在
        QueryWrapper<NewGlossaryDTO> qw = new QueryWrapper<>();
        qw.eq("id", guid);
        NewGlossaryDTO model = glossaryMapper.selectOne(qw);
        if (model == null){
            throw new FkException(ResultEnum.ERROR, "术语不存在");
        }
        if (glossaryMapper.deleteById(guid) > 0){
            return ResultEnum.SUCCESS;
        }else{
            throw new FkException(ResultEnum.ERROR, "删除失败");
        }
    }

    @Override
    public ResultEnum termAssignedEntities(TermAssignedEntities dto)
    {
        String jsonParameter= JSONArray.toJSON(dto.dto).toString();
        ResultDataDTO<String> result = atlasClient.post(terms + "/" + dto.termGuid+"/assignedEntities",jsonParameter);
        Boolean exist = redisTemplate.hasKey("metaDataEntityData:"+dto.dto.get(0).guid);
        if (exist)
        {
            entity.setRedis(dto.dto.get(0).guid);
        }
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum termDeleteAssignedEntities(TermAssignedEntities dto)
    {
        String jsonParameter= JSONArray.toJSON(dto.dto).toString();
        ResultDataDTO<String> result = atlasClient.put(terms + "/" + dto.termGuid+"/assignedEntities",jsonParameter);
        if (result.code == AtlasResultEnum.BAD_REQUEST)
        {
            JSONObject msg= JSON.parseObject(result.data);
            throw new FkException(ResultEnum.BAD_REQUEST,msg.getString("errorMessage"));
        }
        return atlasClient.newResultEnum(result);
    }

}
