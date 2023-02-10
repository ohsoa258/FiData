package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.category.CategoryDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryAttributeDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryLibraryDTO;
import com.fisk.datamanagement.dto.term.TermDTO;
import com.fisk.datamanagement.dto.term.TermDetailsDTO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.mapper.GlossaryLibraryMapper;
import com.fisk.datamanagement.service.IGlossary;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class GlossaryImpl implements IGlossary {

    @Resource
    UserHelper userHelper;

    @Resource
    GlossaryLibraryMapper glossaryLibraryMapper;

    @Resource
    AtlasClient atlasClient;

    @Value("${atlas.glossary.url}")
    private String glossary;
    @Resource
    TermImpl termImpl;
    @Resource
    CategoryImpl categoryImpl;

    @Override
    public List<GlossaryAttributeDTO> getGlossaryList() {
        List<GlossaryAttributeDTO> list;
        try {
            ResultDataDTO<String> result = atlasClient.get(glossary);
            if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
                throw new FkException(ResultEnum.BAD_REQUEST);
            }
            list=JSONObject.parseArray(result.data, GlossaryAttributeDTO.class);
        }
        catch (Exception e)
        {
            log.error("getGlossaryList ex:"+e);
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }
        return list;
    }

    @Override
    public ResultEnum addGlossary(GlossaryDTO dto)
    {
        if (StringUtils.isEmpty(dto.getName())){
            throw new FkException(ResultEnum.ERROR, "术语库名称不能为空");
        }
        // 查询是否存在
        QueryWrapper<GlossaryLibraryDTO> qw = new QueryWrapper<>();
        qw.eq("name", dto.getName()).eq("del_flag", 1).isNull("pid");
        GlossaryLibraryDTO preModel = glossaryLibraryMapper.selectOne(qw);
        if (preModel != null){
            throw new FkException(ResultEnum.ERROR, "术语库名称不能重复");
        }
        // 新增术语库
        GlossaryLibraryDTO model = new GlossaryLibraryDTO();
        model.setName(dto.name);
        model.setShortDescription(dto.shortDescription);
        model.setLongDescription(dto.longDescription);
        model.setCreateTime(LocalDateTime.now());
        model.setCreateUser(userHelper.getLoginUserInfo().id.toString());
        return glossaryLibraryMapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteGlossary(String guid)
    {
        // 查询是否存在
        QueryWrapper<GlossaryLibraryDTO> qw = new QueryWrapper<>();
        qw.eq("id", guid).isNull("pid");
        GlossaryLibraryDTO model = glossaryLibraryMapper.selectOne(qw);
        if (model == null){
            throw new FkException(ResultEnum.ERROR, "术语库不存在");
        }
        // TODO 删除术语分类及分类下的术语

        if (glossaryLibraryMapper.deleteById(guid) > 0){
            return ResultEnum.SUCCESS;
        }else{
            throw new FkException(ResultEnum.ERROR, "删除失败");
        }
    }

    @Override
    public ResultEnum updateGlossary(GlossaryDTO dto) {
        // 校验数据
        if (StringUtils.isEmpty(dto.name)){
            throw new FkException(ResultEnum.ERROR, "术语库名称不能为空");
        }

        // 查询是否存在
        QueryWrapper<GlossaryLibraryDTO> qw = new QueryWrapper<>();
        qw.eq("id", dto.guid).eq("del_flag", 1).isNull("pid");
        GlossaryLibraryDTO model = glossaryLibraryMapper.selectOne(qw);
        if (model == null){
            throw new FkException(ResultEnum.ERROR, "术语库不存在");
        }

        // 查询修改后的名称是否重复
        qw = new QueryWrapper<>();
        qw.eq("name", dto.name).eq("del_flag", 1).isNull("pid");
        GlossaryLibraryDTO preModel = glossaryLibraryMapper.selectOne(qw);
        if (preModel != null && !String.valueOf(preModel.getId()).equals(dto.getGuid())){
            throw new FkException(ResultEnum.ERROR, "术语库名称不能重复");
        }

        // 修改术语库
        model.setName(dto.name);
        model.setShortDescription(dto.shortDescription);
        model.setLongDescription(dto.longDescription);
        model.setUpdateTime(LocalDateTime.now());
        model.setUpdateUser(userHelper.getLoginUserInfo().id.toString());
        qw = new QueryWrapper<>();
        qw.eq("id", dto.guid);
        return glossaryLibraryMapper.update(model, qw) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<TermDTO> getTermList(String guid, Boolean parent) {
        List<TermDTO> list = new ArrayList<>();
        //是否为术语库
        if (parent) {
            /*List<GlossaryAttributeDTO> glossaryList = getGlossaryList();
            if (CollectionUtils.isEmpty(glossaryList)) {
                return list;
            }
            Optional<GlossaryAttributeDTO> first = glossaryList.stream().filter(e -> e.guid.equals(guid)).findFirst();
            if (!first.isPresent()) {
                return list;
            }
            for (GlossaryTermAttributeDTO term : first.get().terms) {
                list.add(termImpl.getTerm(term.termGuid));
            }
            return list;*/
            return new ArrayList<>();
        }
        //查询类别关联下的术语
        CategoryDTO category = categoryImpl.getCategory(guid);
        if (category == null || CollectionUtils.isEmpty(category.terms)) {
            return list;
        }
        for (TermDetailsDTO term : category.terms) {
            list.add(termImpl.getTerm(term.termGuid));
        }
        return list;
    }

}