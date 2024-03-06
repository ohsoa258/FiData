package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.MetadataEntityClassificationAttributeMapDTO;
import com.fisk.datamanagement.dto.classification.ClassificationDefsDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryCategoryPathDto;
import com.fisk.datamanagement.dto.label.LabelInfoDTO;
import com.fisk.datamanagement.dto.label.GlobalSearchDto;
import com.fisk.datamanagement.dto.labelcategory.LabelCategoryDTO;
import com.fisk.datamanagement.dto.labelcategory.LabelCategoryPathDto;
import com.fisk.datamanagement.dto.search.EntitiesDTO;
import com.fisk.datamanagement.dto.search.SearchDslDTO;
import com.fisk.datamanagement.entity.GlossaryLibraryPO;
import com.fisk.datamanagement.entity.GlossaryPO;
import com.fisk.datamanagement.entity.MetadataEntityPO;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.map.*;
import com.fisk.datamanagement.mapper.MetaDataGlossaryMapMapper;
import com.fisk.datamanagement.mapper.MetadataEntityMapper;
import com.fisk.datamanagement.service.IGlobalSearch;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class GlobalSearchImpl implements IGlobalSearch {

    @Resource
    MetadataEntityMapper metadataEntityMapper;
    @Resource
    MetaDataGlossaryMapMapper metaDataGlossaryMapMapper;


    @Resource
    AtlasClient atlasClient;
    @Resource
    ClassificationImpl classification;
    @Resource
    MetadataEntityClassificationAttributeMapImpl metadataEntityClassificationAttributeMap;
    @Resource
    GlossaryImpl glossary;
    @Resource
    UserClient userClient;
    @Resource
    LabelImpl label;
    @Resource
    GlossaryCategoryImpl glossaryCategory;
    @Resource
    LabelCategoryImpl labelCategory;


    @Value("${atlas.searchQuick}")
    private String searchQuick;
    @Value("${atlas.searchSuggestions}")
    private String searchSuggestions;

   /* @Override
    public JSONObject searchQuick(String query, int limit, int offset) {
        ResultDataDTO<String> result = atlasClient.get(searchQuick + "?query=" + query + "&limit=" + limit + "&offset=" + offset);

        if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
            JSONObject msg = JSON.parseObject(result.data);
            throw new FkException(ResultEnum.BAD_REQUEST, msg.getString("errorMessage"));
        }
        return JSON.parseObject(result.data);
    }*/

    @Override
    public List<EntitiesDTO> searchQuick(String query, int limit, int offset) {
        return metadataEntityMapper.searchEntitys(query, offset, limit);
    }

    @Override
    public List<GlobalSearchDto> searchSuggestions(String prefixString) {
        //获取标签分类
        List<LabelCategoryDTO> labelcategory = labelCategory.queryLikeLabelCategory(prefixString);
        //获取标签
        List<LabelInfoDTO> labels = label.getLabelList(prefixString);
        //转换为同一结构
        List<GlobalSearchDto> categoryDtos = LabelCategoryMap.INSTANCES.labelCategoryDtoToTermDtoList(labelcategory);
        List<GlobalSearchDto> labelDtos = LabelMap.INSTANCES.labelDtoToTermDtoList(labels);
        //获取属性分类路径
        List<LabelCategoryPathDto> pathDtos = labelCategory.getLabelCategoryPathDto();
        //设置标签分类的路径
        categoryDtos.stream().forEach(dto -> {
            pathDtos.stream()
                    .filter(pathDto -> pathDto.getCategoryCode().equals(dto.getCategoryParentCode()))
                    .findFirst()
                    .ifPresent(pathDto -> dto.setPath(pathDto.getPath()));
        });
        //设置标签的路径
        labelDtos.stream().forEach(dto -> {
            pathDtos.stream()
                    .filter(pathDto -> pathDto.getId() == dto.getPid())
                    .findFirst()
                    .ifPresent(pathDto -> dto.setPath(pathDto.getPath()));
        });

        categoryDtos.addAll(labelDtos);

        return categoryDtos;
//        ResultDataDTO<String> result = atlasClient.get(searchSuggestions + "?prefixString=" + prefixString);
//        if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
//            JSONObject msg = JSON.parseObject(result.data);
//            throw new FkException(ResultEnum.BAD_REQUEST, msg.getString("errorMessage"));
//        }
//        return JSON.parseObject(result.data);
    }

    @Override
    public ClassificationDefsDTO searchClassification(String keyword) {
        ClassificationDefsDTO classificationList = classification.getClassificationList();
        //filter过滤
        classificationList.classificationDefs = classificationList.classificationDefs
                .stream()
                .filter(e -> e.name.contains(keyword.toLowerCase()) || e.name.contains(keyword.toUpperCase()))
                .collect(Collectors.toList());
        return classificationList;
    }

    @Override
    public List<GlobalSearchDto> searchTerms(String keyword) {
        //获取术语库列表
        List<GlossaryLibraryPO> glossaryLibraryPOS = glossaryCategory.queryLikeGlossaryLibrary(keyword);
        List<GlossaryPO> glossaryPOS = glossary.queryLikeGlossaryList(keyword);
        List<GlobalSearchDto> glossaryLibraryDtos = GlossaryCategoryMap.INSTANCES.poToGlobalSearchDtoList(glossaryLibraryPOS);
        List<GlobalSearchDto> glossaryDtos = GlossaryMap.INSTANCES.poToGlobalSearchDtoList(glossaryPOS);
        //获取术语分类的路径
        List<GlossaryCategoryPathDto> glossaryCategoryPathDtoList = glossaryCategory.getGlossaryCategoryPath();
        //设置术语分类的路径
        glossaryLibraryDtos.stream().forEach(dto -> {
            glossaryCategoryPathDtoList.stream()
                    .filter(pathDto -> pathDto.getId() == dto.getPid())
                    .findFirst()
                    .ifPresent(pathDto -> dto.setPath(pathDto.getPath()));

        });
        //设置术语的路径
        glossaryDtos.stream().forEach(dto -> {
            glossaryCategoryPathDtoList.stream()
                    .filter(pathDto -> pathDto.getId() == dto.getPid())
                    .findFirst()
                    .ifPresent(pathDto -> dto.setPath(pathDto.getPath()));
        });
        //合并集合
        glossaryLibraryDtos.addAll(glossaryDtos);

//        //元数据
//        List<EntitiesDTO> entitiesDTOS = metadataEntityMapper.searchEntitys(keyword, 0, 100);
//        List<GlobalSearchDto> entityGlobalSearchDtos = MetadataEntityMap.INSTANCES.entitiesDtoToTermDtoList(entitiesDTOS);
//        glossaryLibraryDtos.addAll(entityGlobalSearchDtos);
        return glossaryLibraryDtos;
    }

    @Override
    public JSONObject searchDsl(SearchDslDTO dto) {
        List<MetadataEntityPO> list = metadataEntityMapper.getMetadataEntityByType(EntityTypeEnum.getValue(dto.query).getValue());
        if (CollectionUtils.isEmpty(list)) {
            return new JSONObject();
        }

        Map map = new HashMap();

        List<MetadataEntityPO> collect = list.stream().skip(dto.offset).limit(dto.limit).collect(Collectors.toList());
        List<Map> data = new ArrayList<>();

        List<String> userList = list.stream().map(e -> e.createUser).filter(e -> e != null).distinct().collect(Collectors.toList());
        ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userList.stream().map(Long::parseLong).collect(Collectors.toList()));
        if (userListByIds.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }

        for (MetadataEntityPO po : collect) {
            Map mapPo = new HashMap();
            mapPo.put("displayText", po.name);
            mapPo.put("guid", po.id);
            mapPo.put("typeName", dto.query);

            Map attribute = new HashMap();
            attribute.put("description", po.description);
            attribute.put("name", po.name);

            attribute.put("owner", po.createUser);
            Optional<UserDTO> first = userListByIds.data.stream().filter(e -> e.id.toString().equals(po.createUser)).findFirst();
            if (first.isPresent()) {
                attribute.put("owner", first.get().username);
            }

            attribute.put("qualifiedName", po.qualifiedName);

            mapPo.put("attributes", attribute);

            Integer entityId = (int) po.id;

            //业务分类
            List<MetadataEntityClassificationAttributeMapDTO> metadataEntityClassification = metadataEntityClassificationAttributeMap
                    .getMetadataEntityClassification(entityId);
            if (CollectionUtils.isEmpty(metadataEntityClassification)) {
                mapPo.put("classificationNames", new ArrayList<>());
            } else {
                List<String> collect1 = metadataEntityClassification.stream().map(e -> e.classificationName).distinct().collect(Collectors.toList());
                mapPo.put("classificationNames", collect1);
            }
            //术语
            List<String> glossary = metaDataGlossaryMapMapper.selectGlossary(entityId);
            mapPo.put("meaningNames", glossary == null ? new ArrayList<>() : glossary);

            data.add(mapPo);

        }
        map.put("entities", data);

        return JSONObject.parseObject(JSONObject.toJSONString(map));
    }

}
