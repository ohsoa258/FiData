package com.fisk.datamanagement.service.impl;

import com.fisk.datamanagement.dto.assetsdirectory.AssetsDirectoryDTO;
import com.fisk.datamanagement.dto.classification.ClassificationDTO;
import com.fisk.datamanagement.dto.classification.ClassificationDefContentDTO;
import com.fisk.datamanagement.dto.classification.ClassificationDefsDTO;
import com.fisk.datamanagement.dto.entity.EntityFilterDTO;
import com.fisk.datamanagement.dto.search.EntitiesDTO;
import com.fisk.datamanagement.dto.search.SearchBusinessGlossaryEntityDTO;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.service.IAssetsDirectory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class AssetsDirectoryImpl implements IAssetsDirectory {

    @Resource
    ClassificationImpl classification;
    @Resource
    EntityImpl entity;

    @Override
    public List<AssetsDirectoryDTO> assetsDirectoryData() {
        List<AssetsDirectoryDTO> data = new ArrayList<>();
        data.addAll(getAssociationEntity());
        String firstLevel = UUID.randomUUID().toString();
        //第一级目录
        data.add(setAssetsDirectory(firstLevel, "资产目录", "", 1, true, null));
        return getChildAssets(data, firstLevel);
    }

    /**
     * 获取业务过程关联实体
     *
     * @return
     */
    public List<AssetsDirectoryDTO> getAssociationEntity() {
        List<AssetsDirectoryDTO> data = new ArrayList<>();
        //获取业务过程数据
        ClassificationDefsDTO classificationList = classification.getClassificationList();
        if (classificationList != null && CollectionUtils.isEmpty(classificationList.classificationDefs)) {
            return data;
        }
        EntityFilterDTO parameter = new EntityFilterDTO();
        parameter.limit = 100;
        parameter.offset = 0;
        parameter.includeClassificationAttributes = true;
        for (ClassificationDefContentDTO classification : classificationList.classificationDefs) {
            data.add(setAssetsDirectory(classification.guid, classification.name, "", 0, false, classification.superTypes));
            parameter.classification = classification.name;
            SearchBusinessGlossaryEntityDTO eDto = entity.searchBasicEntity(parameter);
            List<EntitiesDTO> entities = eDto.entities;
            if (entities == null) {
                continue;
            }
            for (int i = 0; i < entities.size(); i++) {
                //获取业务过程直接关联的实体
                List<String> entityList = new ArrayList<>();
                List<ClassificationDTO> classifications = entities.get(i).getClassifications();
                for (int j = 0; j < classifications.size(); j++) {
                    entityList.add(classifications.get(i).entityGuid);
                }
                if (EntityTypeEnum.RDBMS_TABLE.getName().equals(entities.get(i).typeName)
                        && "ACTIVE".equals(entities.get(i).status)
                        && entityList.contains(entities.get(i).guid)) {
                    data.add(setAssetsDirectory(entities.get(i).guid,
                            entities.get(i).displayText,
                            classification.guid, 0, true, null));
                }
            }
        }
        return data;
    }

    public List<AssetsDirectoryDTO> getChildAssets(List<AssetsDirectoryDTO> data, String firstCode) {
        //获取第一级
        List<AssetsDirectoryDTO> firstLevel =
                data.stream()
                        .filter(e -> e.superTypes != null && e.superTypes.size() == 0)
                        .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(firstLevel)) {

        }
        //第二级目录
        for (AssetsDirectoryDTO item : firstLevel) {
            item.parent = firstCode;
            item.level = 2;
            //递归获取tree结构
            buildChildTree(item, data);
        }
        //关联实体修改level属性
        List<AssetsDirectoryDTO> collect = data.stream().filter(e -> e.superTypes == null && e.level == 0).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            return data;
        }
        for (AssetsDirectoryDTO item : collect) {
            Optional<AssetsDirectoryDTO> first = data.stream().filter(e -> item.parent.equals(e.key)).findFirst();
            if (!first.isPresent()) {
                continue;
            }
            item.level = first.get().level + 1;
        }
        return data;
    }

    /**
     * 递归创建树形结构
     *
     * @param pNode
     * @param data
     */
    public void buildChildTree(AssetsDirectoryDTO pNode, List<AssetsDirectoryDTO> data) {
        List<AssetsDirectoryDTO> collect = data.stream()
                .filter(e -> e.superTypes != null && e.superTypes.contains(pNode.name)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            return;
        }
        for (AssetsDirectoryDTO item : collect) {
            item.parent = pNode.key;
            item.level = pNode.level + 1;
            buildChildTree(item, data);
        }
    }

    /**
     * dto添加值
     *
     * @param key
     * @param name
     * @param parent
     * @param level
     * @param skip
     * @param superTypes
     * @return
     */
    public AssetsDirectoryDTO setAssetsDirectory(String key, String name,
                                                 String parent, Integer level,
                                                 Boolean skip,
                                                 List<String> superTypes) {
        AssetsDirectoryDTO dto = new AssetsDirectoryDTO();
        dto.key = key;
        dto.name = name;
        dto.parent = parent;
        //第几级
        dto.level = level;
        //是否可跳转
        dto.skip = skip;
        //上级
        dto.superTypes = superTypes;
        return dto;
    }


}
