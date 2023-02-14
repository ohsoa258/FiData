package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.category.CategoryDetailsDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryAnchorDTO;
import com.fisk.datamanagement.dto.term.TermAssignedEntities;
import com.fisk.datamanagement.dto.term.TermDTO;
import com.fisk.datamanagement.entity.GlossaryLibraryPO;
import com.fisk.datamanagement.entity.GlossaryPO;
import com.fisk.datamanagement.entity.MetaDataGlossaryMapPO;
import com.fisk.datamanagement.mapper.GlossaryLibraryMapper;
import com.fisk.datamanagement.mapper.GlossaryMapper;
import com.fisk.datamanagement.mapper.MetaDataGlossaryMapMapper;
import com.fisk.datamanagement.service.ITerm;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
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

    @Resource
    MetaDataGlossaryMapMapper metaDataGlossaryMapMapper;

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

        String categoryId = dto.categories.get(0).categoryGuid;
        // 校验是否在术语库下创建术语
        if (!StringUtils.isEmpty(dto.getAnchor().getGlossaryGuid()) && StringUtils.isEmpty(categoryId)){
            throw new FkException(ResultEnum.ERROR, "请在术语类别下创建术语");
        }

        // 校验术语库，术语类别是否存在
        if (StringUtils.isEmpty(categoryId)){
            throw new FkException(ResultEnum.ERROR, "所属术语类别id不能为空");
        }

        // 查询术语所在术语类别是否存在
        QueryWrapper<GlossaryLibraryPO> qw = new QueryWrapper<>();
        qw.eq("id", categoryId).eq("del_flag", 1);
        GlossaryLibraryPO glossaryLibraryPO = glossaryLibraryMapper.selectOne(qw);
        if (glossaryLibraryPO == null){
            throw new FkException(ResultEnum.ERROR, "所属术语类别不存在");
        }

        // 查询术语是否重复
        QueryWrapper<GlossaryPO> gQw = new QueryWrapper<>();
        gQw.eq("glossary_library_id", categoryId).eq("name", dto.name);
        GlossaryPO preDto = glossaryMapper.selectOne(gQw);
        if (preDto != null){
            throw new FkException(ResultEnum.ERROR, "当前术语类别下已存在该术语名称");
        }

        // 存储术语
        GlossaryPO model = new GlossaryPO();
        model.setName(dto.name);
        model.setShortDescription(dto.shortDescription);
        model.setLongDescription(dto.longDescription);
        model.setGlossaryLibraryId(Integer.parseInt(categoryId));
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
        QueryWrapper<GlossaryPO> qw = new QueryWrapper<>();
        qw.eq("id", guid);
        GlossaryPO model = glossaryMapper.selectOne(qw);
        if (model == null){
            throw new FkException(ResultEnum.ERROR, "数据不存在");
        }

        // 创建对象
        TermDTO dto = new TermDTO();

        // 查询所有术语库及类别
        List<GlossaryLibraryPO> allData = glossaryLibraryMapper.selectList(new QueryWrapper<>());

        // 设置限定名、术语库id依赖
        GlossaryLibraryPO recursionData = recursionData(allData, guid);
        GlossaryAnchorDTO gDto = new GlossaryAnchorDTO();
        dto.setGuid(String.valueOf(model.getId()));
        dto.setShortDescription(model.shortDescription);
        dto.setLongDescription(model.longDescription);
        if (recursionData != null){
            gDto.setGlossaryGuid(String.valueOf(recursionData.getId()));
            // todo relation暂时不明
            // gDto.setRelationGuid();
            dto.setAnchor(gDto);
            dto.setQualifiedName(model.name + "@" + recursionData.name);
        }

        // 设置所属术语类别
        CategoryDetailsDTO categoryDetailsDTO = new CategoryDetailsDTO();
        categoryDetailsDTO.setCategoryGuid(String.valueOf(model.glossaryLibraryId));
        GlossaryLibraryPO libraryPO = allData.stream().filter(item -> item.id == model.glossaryLibraryId).findFirst().orElse(null);
        if (libraryPO != null){
            categoryDetailsDTO.setDisplayText(libraryPO.name);
        }
        dto.setCategories(Collections.singletonList(categoryDetailsDTO));
        return dto;
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
    public ResultEnum updateTerm(TermDTO dto)
    {
        // 校验数据
        if (StringUtils.isEmpty(dto.name)){
            throw new FkException(ResultEnum.ERROR, "术语名称不能为空");
        }

        // 查询术语分类是否存在
        String categoryId = dto.categories.get(0).categoryGuid;
        QueryWrapper<GlossaryLibraryPO> qw = new QueryWrapper<>();
        qw.eq("id", categoryId).eq("del_flag", 1);
        GlossaryLibraryPO category = glossaryLibraryMapper.selectOne(qw);
        if (category == null){
            throw new FkException(ResultEnum.ERROR, "所属术语类别不存在");
        }

        // 查询术语是否存在
        QueryWrapper<GlossaryPO> qwTerm = new QueryWrapper<>();
        qwTerm.eq("id", dto.guid);
        GlossaryPO model = glossaryMapper.selectOne(qwTerm);
        if (model == null){
            throw new FkException(ResultEnum.ERROR, "术语不存在");
        }

        // 查询术语名称是否重复
        qwTerm = new QueryWrapper<>();
        qwTerm.eq("glossary_library_id", categoryId).eq("name", dto.name);
        GlossaryPO all = glossaryMapper.selectOne(qwTerm);
        if (all != null && !String.valueOf(all.id).equals(dto.guid)){
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
        QueryWrapper<GlossaryPO> qw = new QueryWrapper<>();
        qw.eq("id", guid);
        GlossaryPO model = glossaryMapper.selectOne(qw);
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
        // 存储关联实体
        MetaDataGlossaryMapPO model = new MetaDataGlossaryMapPO();
        model.setGlossaryId(Integer.parseInt(dto.termGuid));
        model.setMetaDataEntityId(Integer.parseInt(dto.dto.get(0).guid));
        if (metaDataGlossaryMapMapper.insert(model) <= 0){
            throw new FkException(ResultEnum.ERROR, "术语关联实体失败");
        }
//        String jsonParameter= JSONArray.toJSON(dto.dto).toString();
//        ResultDataDTO<String> result = atlasClient.post(terms + "/" + dto.termGuid+"/assignedEntities",jsonParameter);
        Boolean exist = redisTemplate.hasKey("metaDataEntityData:"+dto.dto.get(0).guid);
        if (exist)
        {
            entity.setRedis(dto.dto.get(0).guid);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum termDeleteAssignedEntities(TermAssignedEntities dto)
    {
        if (metaDataGlossaryMapMapper.deleteById(dto.termGuid) <= 0){
            throw new FkException(ResultEnum.ERROR, "术语关联实体删除失败");
        }
//        String jsonParameter= JSONArray.toJSON(dto.dto).toString();
//        ResultDataDTO<String> result = atlasClient.put(terms + "/" + dto.termGuid+"/assignedEntities",jsonParameter);
//        if (result.code == AtlasResultEnum.BAD_REQUEST)
//        {
//            JSONObject msg= JSON.parseObject(result.data);
//            throw new FkException(ResultEnum.BAD_REQUEST,msg.getString("errorMessage"));
//        }
        return ResultEnum.SUCCESS;
    }

}
