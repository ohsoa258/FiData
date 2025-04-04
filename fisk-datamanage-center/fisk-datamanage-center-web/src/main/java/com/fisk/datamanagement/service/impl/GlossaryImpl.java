package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.category.CategoryDetailsDTO;
import com.fisk.datamanagement.dto.glossary.*;
import com.fisk.datamanagement.dto.label.GlobalSearchDto;
import com.fisk.datamanagement.dto.metadataglossarymap.MetaDataGlossaryMapDTO;
import com.fisk.datamanagement.dto.term.TermDTO;
import com.fisk.datamanagement.entity.GlossaryLibraryPO;
import com.fisk.datamanagement.entity.GlossaryPO;
import com.fisk.datamanagement.entity.MetaDataGlossaryMapPO;
import com.fisk.datamanagement.map.GlossaryMap;
import com.fisk.datamanagement.mapper.GlossaryLibraryMapper;
import com.fisk.datamanagement.mapper.GlossaryMapper;
import com.fisk.datamanagement.mapper.MetaDataGlossaryMapMapper;
import com.fisk.datamanagement.service.IGlossary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class GlossaryImpl
        extends ServiceImpl<GlossaryMapper, GlossaryPO>
        implements IGlossary {

    @Resource
    UserHelper userHelper;
    @Resource
    GlossaryLibraryMapper glossaryLibraryMapper;
    @Resource
    GlossaryMapper glossaryMapper;
    @Resource
    MetaDataGlossaryMapMapper metaDataGlossaryMapMapper;

    @Override
    public List<GlossaryAttributeDTO> getGlossaryList() {
        // 查询术语术语库数据
        List<GlossaryLibraryPO> gAllData = glossaryLibraryMapper.selectList(new QueryWrapper<>());
        if (CollectionUtils.isEmpty(gAllData)) {
            return null;
        }

        // 数据转换
        List<GlossaryAttributeDTO> data = gAllData.stream().map(item -> {
            GlossaryAttributeDTO dto = new GlossaryAttributeDTO();
            dto.setGuid(String.valueOf(item.id));
            if (item.pid == null) {
                dto.setPid(null);
            } else {
                dto.setPid(item.pid.toString());
            }
            dto.setQualifiedName(item.name);
            dto.setName(item.name);
            dto.setShortDescription(item.shortDescription);
            dto.setLongDescription(item.longDescription);
            return dto;
        }).collect(Collectors.toList());

        // 获取父级NewGlossaryDTO
        List<GlossaryAttributeDTO> parent = data.stream().filter(item -> StringUtils.isEmpty(item.pid)).collect(Collectors.toList());

        // 设置所有子集、术语列表
        for (GlossaryAttributeDTO item : parent) {
            List<GlossaryCategoryAttributeDTO> c = new ArrayList<>();
            item.setCategories(recursionChildren(data, item.getGuid(), c));
        }

        // 设置所有术语
        List<GlossaryPO> termData = glossaryMapper.selectList(new QueryWrapper<>());
        if (CollectionUtils.isEmpty(termData)) {
            return parent;
        }
        for (GlossaryAttributeDTO item : parent) {
            List<String> idList = item.getCategories().stream().map(GlossaryCategoryAttributeDTO::getCategoryGuid).collect(Collectors.toList());

            List<GlossaryTermAttributeDTO> tList = new ArrayList<>();
            termData.stream().filter(t -> {
                if (idList.contains(t.getGlossaryLibraryId().toString())) {
                    GlossaryTermAttributeDTO dto = new GlossaryTermAttributeDTO();
                    dto.setTermGuid(String.valueOf(t.id));
                    dto.setDisplayText(t.name);
                    tList.add(dto);
                }
                return false;
            }).collect(Collectors.toList());
            item.setTerms(tList);
        }

        return parent;
    }

    private List<GlossaryCategoryAttributeDTO> recursionChildren(List<GlossaryAttributeDTO> allData, String pid, List<GlossaryCategoryAttributeDTO> data) {
        for (GlossaryAttributeDTO item : allData) {
            if (!StringUtils.isEmpty(item.pid) && item.getPid().equals(pid)) {
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
    public ResultEnum addGlossary(GlossaryDTO dto) {
        if (StringUtils.isEmpty(dto.getName())) {
            throw new FkException(ResultEnum.ERROR, "术语库名称不能为空");
        }
        // 查询是否存在
        QueryWrapper<GlossaryLibraryPO> qw = new QueryWrapper<>();
        qw.eq("name", dto.getName()).eq("del_flag", 1).isNull("pid");
        GlossaryLibraryPO preModel = glossaryLibraryMapper.selectOne(qw);
        if (preModel != null) {
            throw new FkException(ResultEnum.ERROR, "术语库名称不能重复");
        }
        // 新增术语库
        GlossaryLibraryPO model = new GlossaryLibraryPO();
        model.setName(dto.name);
        model.setShortDescription(dto.shortDescription);
        model.setLongDescription(dto.longDescription);
        model.setCreateUser(userHelper.getLoginUserInfo().id.toString());
        return glossaryLibraryMapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteGlossary(String guid) {
        //删除术语库时 同时删除所有术语库下的目录和目录下的术语
        //1删除术语库
        glossaryLibraryMapper.deleteById(guid);

        //2查询术语库下的目录
        List<GlossaryLibraryPO> glossaryLibraryPOS = glossaryLibraryMapper.selectList(
                new LambdaQueryWrapper<GlossaryLibraryPO>()
                        .eq(GlossaryLibraryPO::getPid, guid)
        );

        List<Long> collect = null;
        //删除该术语库下的所有目录
        if (!CollectionUtils.isEmpty(glossaryLibraryPOS)) {
            collect = glossaryLibraryPOS.stream().map(GlossaryLibraryPO::getId).collect(Collectors.toList());
            glossaryLibraryMapper.deleteBatchIds(collect);
        }

        if (!CollectionUtils.isEmpty(collect)) {
            //////////////////////////////////////////////////////////////////////////////////////////////////
            //查询该目录下的所有术语
            List<Long> collect1 = glossaryMapper.selectList(
                    new LambdaQueryWrapper<GlossaryPO>()
                            .in(GlossaryPO::getGlossaryLibraryId, collect)
            ).stream().map(GlossaryPO::getId).collect(Collectors.toList());

            //3删除术语库下的术语和元数据的关联关系
            if (!CollectionUtils.isEmpty(collect1)){
                metaDataGlossaryMapMapper.delete(
                        new LambdaQueryWrapper<MetaDataGlossaryMapPO>()
                                .in(MetaDataGlossaryMapPO::getGlossaryId, collect1)
                );
            }
            //////////////////////////////////////////////////////////////////////////////////////////////////

            //3.1删除目录下的术语
            this.remove(
                    new LambdaQueryWrapper<GlossaryPO>()
                            .in(GlossaryPO::getGlossaryLibraryId, collect)
            );
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum updateGlossary(GlossaryDTO dto) {
        // 校验数据
        if (StringUtils.isEmpty(dto.name)) {
            throw new FkException(ResultEnum.ERROR, "术语库名称不能为空");
        }

        // 查询是否存在
        QueryWrapper<GlossaryLibraryPO> qw = new QueryWrapper<>();
        qw.eq("id", dto.guid).eq("del_flag", 1).isNull("pid");
        GlossaryLibraryPO model = glossaryLibraryMapper.selectOne(qw);
        if (model == null) {
            throw new FkException(ResultEnum.ERROR, "术语库不存在");
        }

        // 查询修改后的名称是否重复
        qw = new QueryWrapper<>();
        qw.eq("name", dto.name).eq("del_flag", 1).isNull("pid");
        GlossaryLibraryPO preModel = glossaryLibraryMapper.selectOne(qw);
        if (preModel != null && !String.valueOf(preModel.getId()).equals(dto.getGuid())) {
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
        //是否为术语库
        if (parent) {
            return new ArrayList<>();
        }
        // 查询术语信息
        QueryWrapper<GlossaryPO> qw = new QueryWrapper<>();
        qw.eq("glossary_library_id", guid);
        List<GlossaryPO> termList = glossaryMapper.selectList(qw);
        if (CollectionUtils.isEmpty(termList)) {
            return new ArrayList<>();
        }

        List<TermDTO> list = new ArrayList<>();
        for (GlossaryPO model : termList) {
            TermDTO dto = new TermDTO();
            dto.setGuid(String.valueOf(model.id));
            dto.setGlossaryLibraryId(Integer.parseInt(guid));
            dto.setName(model.name);
            // 20240321 去掉含义 前端不展示
//            dto.setShortDescription(model.shortDescription);
            dto.setLongDescription(model.longDescription);
            list.add(dto);
        }

        // 加载所有数据
        List<GlossaryLibraryPO> allData = glossaryLibraryMapper.selectList(new QueryWrapper<>());

        if (!CollectionUtils.isEmpty(allData)) {
            for (TermDTO model : list) {
                // 查询术语所在术语库中的术语类别
                GlossaryLibraryPO category = allData.stream().filter(item -> item.id == model.glossaryLibraryId).findFirst().orElse(null);
                if (category != null) {
                    CategoryDetailsDTO cdDto = new CategoryDetailsDTO();
                    cdDto.setDisplayText(category.name);
                    cdDto.setCategoryGuid(String.valueOf(category.id));
                    model.setCategories(Collections.singletonList(cdDto));
                    // 查询所在术语库
                    GlossaryLibraryPO libraryPO = recursionData(allData, category.getPid().toString());
                    if (libraryPO != null) {
                        // 设置全限定名
                        model.setQualifiedName(model.name + "@" + libraryPO.name);
                        GlossaryAnchorDTO gaDto = new GlossaryAnchorDTO();
                        gaDto.setGlossaryGuid(String.valueOf(libraryPO.id));
                        model.setAnchor(gaDto);
                    }
                }
            }
        }

        return list;
    }

    private GlossaryLibraryPO recursionData(List<GlossaryLibraryPO> allData, String pid) {
        for (GlossaryLibraryPO item : allData) {
            if (!StringUtils.isEmpty(item.pid) && !String.valueOf(item.id).equals(pid)) {
                recursionData(allData, String.valueOf(item.pid));
            }
            return item;
        }
        return null;
    }

    public GlossaryPO getInfoByName(String glossaryName) {
        GlossaryPO po = this.query().eq("name", glossaryName).one();
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return po;
    }

    public GlossaryPO getInfoByName(Integer id) {
        GlossaryPO po = this.query().eq("id", id).one();
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return po;
    }

    public List<Map> getEntityGlossData(Integer entityId) {
        List<MetaDataGlossaryMapDTO> entityGlossary = metaDataGlossaryMapMapper.getEntityGlossary(entityId);
        if (CollectionUtils.isEmpty(entityGlossary)) {
            return new ArrayList<>();
        }
        List<Map> list = new ArrayList<>();
        for (MetaDataGlossaryMapDTO item : entityGlossary) {
            Map map = new HashMap();
            map.put("displayText", item.glossaryName);
            map.put("guid", item.glossaryId);
            list.add(map);
        }

        return list;
    }

    public List<Map> getEntityGlossDataByName(String qualifiedName) {
        List<MetaDataGlossaryMapDTO> entityGlossary = metaDataGlossaryMapMapper.getEntityGlossaryByQName(qualifiedName);
        if (CollectionUtils.isEmpty(entityGlossary)) {
            return new ArrayList<>();
        }
        List<Map> list = new ArrayList<>();
        for (MetaDataGlossaryMapDTO item : entityGlossary) {
            Map map = new HashMap();
            map.put("displayText", item.glossaryName);
            map.put("guid", item.glossaryId);
            list.add(map);
        }

        return list;
    }

//    public List<Integer> getClassificationByEntityId(Integer glossaryId, Integer offset, Integer pageSize) {
//        QueryWrapper<MetaDataGlossaryMapPO> queryWrapper = new QueryWrapper<>();
//        queryWrapper.select("metadata_entity_id").lambda().eq(MetaDataGlossaryMapPO::getGlossaryId, glossaryId);
//        List<MetaDataGlossaryMapPO> list = metaDataGlossaryMapMapper.selectList(queryWrapper);
//        if (CollectionUtils.isEmpty(list)) {
//            return new ArrayList<>();
//        }
//        return list.stream()
//                .skip(offset)
//                .limit(pageSize)
//                .map(e -> e.metadataEntityId).collect(Collectors.toList());
//    }

    public List<String> getClassificationByEntityId(Integer glossaryId) {
        QueryWrapper<MetaDataGlossaryMapPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("metadata_qualified_name").lambda().eq(MetaDataGlossaryMapPO::getGlossaryId, glossaryId);
        List<MetaDataGlossaryMapPO> list = metaDataGlossaryMapMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(e -> e.metadataQualifiedName).collect(Collectors.toList());
    }

    /**
     * 模糊查询术语
     *
     * @param keyword
     * @return
     */
    public List<GlossaryPO> queryLikeGlossaryList(String keyword) {
        return this.query().like(org.apache.commons.lang.StringUtils.isNotEmpty(keyword), "name", keyword).list();
    }

    @Override
    public List<GlossaryDTO> queryGlossaryListById(GlobalSearchDto dto) {
        List<GlossaryPO> listPo = new ArrayList<>();
        switch (dto.type) {
            case GLOSSARY:
                listPo = this.query().eq("del_flag", 1).eq("id", dto.id).list();
                break;
            case GLOSSARY_CATEGORY:
                listPo = this.query().eq("del_flag", 1).eq("glossary_library_id", dto.id).list();
            default:
                break;
        }
        //listPo=this.query().list();
        return GlossaryMap.INSTANCES.poToDtoList(listPo);
    }

    @Override
    public Integer getGlossaryTotal() {
        return glossaryMapper.getGlossaryTotal();
    }

    /**
     * 业务术语全局搜索
     *
     * @param keyword
     * @return
     */
    @Override
    public List<GlossaryDTO> glossaryGlobalSearch(String keyword) {
        List<GlossaryPO> list = this.list(
                new LambdaQueryWrapper<GlossaryPO>()
                        .like(org.apache.commons.lang.StringUtils.isNotEmpty(keyword), GlossaryPO::getName, keyword)
        );

        List<GlossaryPO> list1 = this.list(
                new LambdaQueryWrapper<GlossaryPO>()
                        .like(org.apache.commons.lang.StringUtils.isNotEmpty(keyword), GlossaryPO::getLongDescription, keyword)
        );

        list.addAll(list1);
        List<GlossaryDTO> glossaryDTOS = GlossaryMap.INSTANCES.poToDtoList(list);

        //去重
        Map<String, GlossaryDTO> collect = glossaryDTOS.stream().collect(Collectors.toMap(GlossaryDTO::getGuid, glossaryDTO -> glossaryDTO, (k1, k2) -> k1));
        return new ArrayList<>(collect.values());
    }
}