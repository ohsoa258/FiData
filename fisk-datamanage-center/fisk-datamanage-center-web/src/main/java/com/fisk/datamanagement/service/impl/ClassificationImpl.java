package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.server.metadata.ClassificationInfoDTO;
import com.fisk.datamanagement.dto.classification.*;
import com.fisk.datamanagement.dto.entity.EntityFilterDTO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.map.ClassificationMap;
import com.fisk.datamanagement.service.IClassification;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class ClassificationImpl implements IClassification {

    @Resource
    AtlasClient atlasClient;

    @Value("${atlas.typedefs}")
    private String typedefs;
    @Value("${atlas.delTypeDefs}")
    private String delTypeDefs;
    @Value("${atlas.bulkClassification}")
    private String bulkClassification;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;
    @Value("${atlas.searchBasic}")
    private String searchBasic;
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    EntityImpl entity;

    @Override
    public ClassificationDefsDTO getClassificationList() {
        ClassificationDefsDTO data = new ClassificationDefsDTO();
        try {
            ResultDataDTO<String> result = atlasClient.get(typedefs + "?type=classification");
            if (result.code != AtlasResultEnum.REQUEST_SUCCESS)
            {
                throw new FkException(ResultEnum.BAD_REQUEST);
            }
            JSONObject jsonObj = JSON.parseObject(result.data);
            String classificationDefs=jsonObj.getString("classificationDefs");
            data.classificationDefs = JSONObject.parseArray(classificationDefs, ClassificationDefContentDTO.class);
            //根据创建时间升序
            data.classificationDefs.sort(Comparator.comparing(ClassificationDefContentDTO::getCreateTime));
            //反转
            Collections.reverse(data.classificationDefs);
        } catch (Exception e) {
            log.error("getClassificationList ex:" + e);
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }
        return data;
    }

    @Override
    public List<ClassificationTreeDTO> getClassificationTree() {
        ClassificationDefsDTO data = getClassificationList();
        if (data == null || CollectionUtils.isEmpty(data.classificationDefs)) {
            return new ArrayList<>();
        }
        //获取第一级
        List<ClassificationDefContentDTO> firstLevel =
                data.classificationDefs
                        .stream()
                        .filter(e -> e.superTypes.size() == 0)
                        .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(firstLevel)) {
            return new ArrayList<>();
        }
        List<ClassificationTreeDTO> dto = ClassificationMap.INSTANCES.poListToDtoList(firstLevel);
        for (ClassificationTreeDTO item : dto) {
            buildChildTree(item, data.classificationDefs);
        }
        return dto;
    }

    /**
     * 递归拼接tree
     *
     * @param pNode
     * @param data
     * @return
     */
    public ClassificationTreeDTO buildChildTree(ClassificationTreeDTO pNode, List<ClassificationDefContentDTO> data) {
        List<ClassificationTreeDTO> childList = new ArrayList<>();
        for (ClassificationDefContentDTO item : data) {
            if (item.superTypes.contains(pNode.name)) {
                childList.add(buildChildTree(ClassificationMap.INSTANCES.poToDto(item), data));
            }
        }
        pNode.child = childList;
        return pNode;
    }

    @Override
    public ResultEnum updateClassification(ClassificationDefsDTO dto) {
        String jsonParameter = JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.put(typedefs + "?type=classification", jsonParameter);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum deleteClassification(String classificationName)
    {
        ResultDataDTO<String> result = atlasClient.delete(delTypeDefs + classificationName);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum addClassification(ClassificationDefsDTO dto)
    {
        //设置时间戳
        dto.classificationDefs
                .stream()
                .map(e->e.createTime=System.currentTimeMillis())
                .collect(Collectors.toList());
        String jsonParameter=JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.post(typedefs + "?type=classification", jsonParameter);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum classificationAddAssociatedEntity(ClassificationAddEntityDTO dto)
    {
        String jsonParameter=JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.post(bulkClassification, jsonParameter);
        Boolean exist = redisTemplate.hasKey("metaDataEntityData:"+dto.entityGuids.get(0));
        if (exist)
        {
            entity.setRedis(dto.entityGuids.get(0));
        }
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum classificationDelAssociatedEntity(ClassificationDelAssociatedEntityDTO dto) {
        ResultDataDTO<String> result = atlasClient.delete(entityByGuid + "/" + dto.entityGuid + "/classification/" + dto.classificationName);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum appSynchronousClassification(ClassificationInfoDTO dto) {
        log.info("开始同步业务， 参数:{}", JSON.toJSONString(dto));
        //是否删除
        if (dto.delete) {
            return deleteClassification(dto.name);
        }

        ClassificationDefsDTO data = new ClassificationDefsDTO();
        List<ClassificationDefContentDTO> list = new ArrayList<>();

        //同步主数据业务分类
        ClassificationDefContentDTO masterData = new ClassificationDefContentDTO();
        masterData.name = dto.name;
        masterData.description = dto.description;

        List<String> analysisModelSuperType = new ArrayList<>();
        if (dto.sourceType == 1) {
            analysisModelSuperType.add("业务数据");
        } else {
            analysisModelSuperType.add("分析数据");
        }
        masterData.superTypes = analysisModelSuperType;

        list.add(masterData);

        data.classificationDefs = list;

        return this.addClassification(data);
    }

    @Override
    public ResultEnum synchronousClassification() {
        ClassificationDefsDTO data = new ClassificationDefsDTO();
        List<ClassificationDefContentDTO> list = new ArrayList<>();

        //同步数据接入业务分类
        ClassificationDefContentDTO dataAccess = new ClassificationDefContentDTO();
        dataAccess.name = "业务数据";
        dataAccess.description = "业务数据";
        list.add(dataAccess);

        //同步数仓建模业务分类
        ClassificationDefContentDTO dataModel = new ClassificationDefContentDTO();
        dataModel.name = "分析数据";
        dataModel.description = "分析数据";
        list.add(dataModel);

        data.classificationDefs = list;

        return this.addClassification(data);
    }

    @Override
    public ResultEnum delClassificationEntity(String classification) {

        ClassificationDefsDTO classificationList = getClassificationList();
        for (ClassificationDefContentDTO item : classificationList.classificationDefs) {
            EntityFilterDTO dto = new EntityFilterDTO();
            dto.classification = item.name;
            dto.excludeDeletedEntities = false;
            dto.limit = 10000;
            dto.offset = 0;

            String jsonParameter = JSONArray.toJSON(dto).toString();
            ResultDataDTO<String> result = atlasClient.post(searchBasic, jsonParameter);
            if (result.code != AtlasResultEnum.REQUEST_SUCCESS) {
                JSONObject msg = JSON.parseObject(result.data);
                throw new FkException(ResultEnum.BAD_REQUEST, msg.getString("errorMessage"));
            }
            JSONObject jsonObject = JSON.parseObject(result.data);

            String entities = jsonObject.getString("entities");
            if (entities == null) {
                deleteClassification(item.name);
                continue;
            }
            JSONArray jsonArray = JSON.parseArray(entities);
            if (jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject1 = JSON.parseObject(jsonArray.get(i).toString());
                    String guid = jsonObject1.getString("guid");
                    String url = "/api/atlas/v2/entity/guid/";
                    url += guid;
                    url += "/classification/";
                    url += item.name;

                    ResultDataDTO<String> resultDataDTO = atlasClient.delete(url);

                }
            }

            deleteClassification(item.name);
        }

        return ResultEnum.SUCCESS;
    }


}
