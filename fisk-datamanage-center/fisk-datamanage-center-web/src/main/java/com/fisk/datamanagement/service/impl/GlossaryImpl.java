package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.category.CategoryDTO;
import com.fisk.datamanagement.dto.category.CategoryDetailsDTO;
import com.fisk.datamanagement.dto.glossary.*;
import com.fisk.datamanagement.dto.term.TermDTO;
import com.fisk.datamanagement.dto.term.TermDetailsDTO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.mapper.GlossaryLibraryMapper;
import com.fisk.datamanagement.mapper.GlossaryMapper;
import com.fisk.datamanagement.service.IGlossary;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    GlossaryMapper glossaryMapper;

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
        // 查询术语术语库数据
//        List<GlossaryLibraryDTO> gAllData = glossaryLibraryMapper.selectList(new QueryWrapper<>());
//        if (CollectionUtils.isEmpty(gAllData)){
//            return null;
//        }
//
//        // 数据转换
//        List<GlossaryAttributeDTO> data = gAllData.stream().map(item -> {
//            GlossaryAttributeDTO dto = new GlossaryAttributeDTO();
//            dto.setGuid(String.valueOf(item.id));
//            if (item.pid == null){
//                dto.setPid(null);
//            }else{
//                dto.setPid(item.pid.toString());
//            }
//            dto.setQualifiedName(item.name);
//            dto.setName(item.name);
//            dto.setShortDescription(item.shortDescription);
//            dto.setLongDescription(item.longDescription);
//            return dto;
//        }).collect(Collectors.toList());
//
//        // 获取父级NewGlossaryDTO
//        List<GlossaryAttributeDTO> parent = data.stream().filter(item -> StringUtils.isEmpty(item.pid)).collect(Collectors.toList());
//
//        // 设置所有子集、术语列表
//        for (GlossaryAttributeDTO item : parent){
//            List<GlossaryCategoryAttributeDTO> c = new ArrayList<>();
//            item.setCategories(recursionChildren(data, item.getGuid(), c));
//        }
//
//        // 设置所有术语
//        List<NewGlossaryDTO> termData = glossaryMapper.selectList(new QueryWrapper<>());
//        if (CollectionUtils.isEmpty(termData)){
//            return parent;
//        }
//        for (GlossaryAttributeDTO item : parent){
//            List<String> idList = item.getCategories().stream().map(GlossaryCategoryAttributeDTO::getCategoryGuid).collect(Collectors.toList());
//
//            List<GlossaryTermAttributeDTO> tList = new ArrayList<>();
//            termData.stream().filter(t -> {
//                if (idList.contains(t.getGlossaryLibraryId().toString())){
//                    GlossaryTermAttributeDTO dto = new GlossaryTermAttributeDTO();
//                    dto.setTermGuid(String.valueOf(t.id));
//                    dto.setDisplayText(t.name);
//                    tList.add(dto);
//                }
//                return false;
//            }).collect(Collectors.toList());
//            item.setTerms(tList);
//        }

//        return parent;
        return null;
    }

    private List<GlossaryCategoryAttributeDTO> recursionChildren(List<GlossaryAttributeDTO> allData, String pid, List<GlossaryCategoryAttributeDTO> data){
        for (GlossaryAttributeDTO item : allData){
            if (!StringUtils.isEmpty(item.pid) && item.getPid().equals(pid)){
                GlossaryCategoryAttributeDTO dto = new GlossaryCategoryAttributeDTO();
                dto.setDisplayText(item.name);
                dto.setCategoryGuid(item.guid);
                dto.setParentCategoryGuid(item.pid);
                data.add(dto);
                recursionChildren(allData, item.guid, data);
            }
        }
        return data;
    }

    @Override
    public ResultEnum addGlossary(GlossaryDTO dto)
    {
//        if (StringUtils.isEmpty(dto.getName())){
//            throw new FkException(ResultEnum.ERROR, "术语库名称不能为空");
//        }
//        // 查询是否存在
//        QueryWrapper<GlossaryLibraryDTO> qw = new QueryWrapper<>();
//        qw.eq("name", dto.getName()).eq("del_flag", 1).isNull("pid");
//        GlossaryLibraryDTO preModel = glossaryLibraryMapper.selectOne(qw);
//        if (preModel != null){
//            throw new FkException(ResultEnum.ERROR, "术语库名称不能重复");
//        }
//        // 新增术语库
//        GlossaryLibraryDTO model = new GlossaryLibraryDTO();
//        model.setName(dto.name);
//        model.setShortDescription(dto.shortDescription);
//        model.setLongDescription(dto.longDescription);
//        model.setCreateTime(LocalDateTime.now());
//        model.setCreateUser(userHelper.getLoginUserInfo().id.toString());
//        return glossaryLibraryMapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        return null;
    }

    @Override
    public ResultEnum deleteGlossary(String guid)
    {
        // 查询是否存在
//        QueryWrapper<GlossaryLibraryDTO> qw = new QueryWrapper<>();
//        qw.eq("id", guid).isNull("pid");
//        GlossaryLibraryDTO model = glossaryLibraryMapper.selectOne(qw);
//        if (model == null){
//            throw new FkException(ResultEnum.ERROR, "术语库不存在");
//        }
//        // TODO 删除术语分类及分类下的术语
//
//        if (glossaryLibraryMapper.deleteById(guid) > 0){
            return ResultEnum.SUCCESS;
//        }else{
//            throw new FkException(ResultEnum.ERROR, "删除失败");
//        }
    }

    @Override
    public ResultEnum updateGlossary(GlossaryDTO dto) {
        // 校验数据
//        if (StringUtils.isEmpty(dto.name)){
//            throw new FkException(ResultEnum.ERROR, "术语库名称不能为空");
//        }
//
//        // 查询是否存在
//        QueryWrapper<GlossaryLibraryDTO> qw = new QueryWrapper<>();
//        qw.eq("id", dto.guid).eq("del_flag", 1).isNull("pid");
//        GlossaryLibraryDTO model = glossaryLibraryMapper.selectOne(qw);
//        if (model == null){
//            throw new FkException(ResultEnum.ERROR, "术语库不存在");
//        }
//
//        // 查询修改后的名称是否重复
//        qw = new QueryWrapper<>();
//        qw.eq("name", dto.name).eq("del_flag", 1).isNull("pid");
//        GlossaryLibraryDTO preModel = glossaryLibraryMapper.selectOne(qw);
//        if (preModel != null && !String.valueOf(preModel.getId()).equals(dto.getGuid())){
//            throw new FkException(ResultEnum.ERROR, "术语库名称不能重复");
//        }
//
//        // 修改术语库
//        model.setName(dto.name);
//        model.setShortDescription(dto.shortDescription);
//        model.setLongDescription(dto.longDescription);
//        model.setUpdateTime(LocalDateTime.now());
//        model.setUpdateUser(userHelper.getLoginUserInfo().id.toString());
//        qw = new QueryWrapper<>();
//        qw.eq("id", dto.guid);
//        return glossaryLibraryMapper.update(model, qw) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        return null;
    }

    @Override
    public List<TermDTO> getTermList(String guid, Boolean parent) {
        List<TermDTO> list = new ArrayList<>();
        //是否为术语库
//        if (parent) {
//            return new ArrayList<>();
//        }
//        // 查询术语信息
//        NewGlossaryDTO model = glossaryMapper.selectById(guid);
//        if (model == null){
//            return new ArrayList<>();
//        }
//
//        TermDTO dto = new TermDTO();
//        dto.setGuid(String.valueOf(model.id));
//        dto.setName(model.name);
//        dto.setShortDescription(model.shortDescription);
//        dto.setLongDescription(model.longDescription);
//
//        // 加载所有数据
//        List<GlossaryLibraryDTO> allData = glossaryLibraryMapper.selectList(new QueryWrapper<>());
//
//        // 查询术语所在术语库中的术语类别
//        GlossaryLibraryDTO category = allData.stream().filter(item -> item.id == model.glossaryLibraryId).findFirst().orElse(null);
//        if (category != null) {
//            CategoryDetailsDTO cdDto = new CategoryDetailsDTO();
//            cdDto.setDisplayText(category.name);
//            cdDto.setCategoryGuid(String.valueOf(category.id));
//            dto.setCategories(Collections.singletonList(cdDto));
//            // 查询所在术语库
//            GlossaryLibraryDTO libraryDTO = recursionData(allData, category.getPid().toString());
//            if (libraryDTO != null){
//                // 设置全限定名
//                dto.setQualifiedName(model.name + "@" + libraryDTO.name);
//                GlossaryAnchorDTO gaDto = new GlossaryAnchorDTO();
//                gaDto.setGlossaryGuid(String.valueOf(libraryDTO.id));
//                dto.setAnchor(gaDto);
//            }
//        }
//
//        return Collections.singletonList(dto);
        return null;
    }

//    private GlossaryLibraryDTO recursionData(List<GlossaryLibraryDTO> allData, String pid){
//        for (GlossaryLibraryDTO item : allData){
//            if (!StringUtils.isEmpty(item.pid) && !String.valueOf(item.id).equals(pid)){
//                recursionData(allData, String.valueOf(item.pid));
//            }
//            return item;
//        }
//        return null;
//    }

}