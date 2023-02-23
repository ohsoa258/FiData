package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.server.metadata.ClassificationInfoDTO;
import com.fisk.datamanagement.dto.businessclassification.BusinessClassificationTreeDTO;
import com.fisk.datamanagement.dto.classification.*;
import com.fisk.datamanagement.dto.entity.EntityFilterDTO;
import com.fisk.datamanagement.entity.BusinessClassificationPO;
import com.fisk.datamanagement.entity.MetadataClassificationMapPO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.map.ClassificationMap;
import com.fisk.datamanagement.mapper.BusinessClassificationMapper;
import com.fisk.datamanagement.mapper.GlossaryLibraryMapper;
import com.fisk.datamanagement.mapper.MetaDataClassificationMapMapper;
import com.fisk.datamanagement.service.IClassification;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class ClassificationImpl
        extends ServiceImpl<BusinessClassificationMapper, BusinessClassificationPO>
        implements IClassification {

    @Resource
    BusinessClassificationMapper businessClassificationMapper;
    @Resource
    UserHelper userHelper;
    @Resource
    GlossaryLibraryMapper glossaryLibraryMapper;
    @Resource
    MetaDataClassificationMapMapper metaDataClassificationMapMapper;

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
        // 获取全部数据
        List<BusinessClassificationPO> all = businessClassificationMapper.selectList(new QueryWrapper<>());
        if (CollectionUtils.isEmpty(all)){
            return new ClassificationDefsDTO();
        }

        // 数据转换
        List<ClassificationDefContentDTO> allData = all.stream().map(item -> {
            ClassificationDefContentDTO dto = new ClassificationDefContentDTO();
            dto.setId(String.valueOf(item.id));
            dto.setGuid(String.valueOf(item.id));
            dto.setPid(String.valueOf(item.pid));
            dto.setName(item.name);
            dto.setDescription(item.description);
            dto.setCreateTime(item.createTime);
            return dto;
        }).collect(Collectors.toList());

        List<ClassificationDefContentDTO> allDatas = allData;

        // 设置子集、父级
        for (ClassificationDefContentDTO parent : allData){
            List<ClassificationDefContentDTO> subList = allDatas.stream().filter(item -> !StringUtils.isEmpty(item.getPid()) && item.getPid().equals(parent.getId())).collect(Collectors.toList());
            parent.setSubTypes(subList.stream().map(ClassificationDefContentDTO::getName).collect(Collectors.toList()));

            if (!StringUtils.isEmpty(parent.getPid())){
                List<ClassificationDefContentDTO> superList = allDatas.stream().filter(item -> item.getId().equals(parent.getPid())).collect(Collectors.toList());
                parent.setSuperTypes(superList.stream().map(ClassificationDefContentDTO::getName).collect(Collectors.toList()));
            }
        }
        data.setClassificationDefs(allData);
        return data;
    }

    @Override
    public List<BusinessClassificationTreeDTO> getClassificationTree() {
        // 查询所有数据
        List<BusinessClassificationPO> data = businessClassificationMapper.selectList(new QueryWrapper<>());
        if (CollectionUtils.isEmpty(data)){
            return new ArrayList<>();
        }

        // 数据转换
        List<BusinessClassificationTreeDTO> allData = data.stream().map(item -> {
            BusinessClassificationTreeDTO dto = new BusinessClassificationTreeDTO();
            dto.setId(String.valueOf(item.id));
            dto.setGuid(String.valueOf(item.id));
            if (item.pid == null){
                dto.setPid(null);
            }else{
                dto.setPid(item.getPid().toString());
            }
            dto.setName(item.name);
            dto.setDescription(item.description);
            dto.setCreateTime(item.createTime);
            return dto;
        }).collect(Collectors.toList());

        // 获取父级
        List<BusinessClassificationTreeDTO> parentList = allData.stream().filter(item -> StringUtils.isEmpty(item.pid)).collect(Collectors.toList());
        if (parentList.size() > 1){
            parentList.sort(Comparator.comparing(BusinessClassificationTreeDTO::getCreateTime).reversed());
        }
        // 递归处理子集
        recursionClassificationTree(allData, parentList);
        return parentList;
    }

    /**
     * 递归处理子集数据
     * @param allData 原始数据集
     * @param parentList 父级数据集
     */
    private void recursionClassificationTree(List<BusinessClassificationTreeDTO> allData, List<BusinessClassificationTreeDTO> parentList){
        // 遍历父级
        for (BusinessClassificationTreeDTO parent : parentList){
            // 子集容器
            List<BusinessClassificationTreeDTO> children = new ArrayList<>();
            for (BusinessClassificationTreeDTO sub : allData){
                if (parent.getId().equals(sub.getPid())){
                    children.add(sub);
                }
                // 递归处理
                recursionClassificationTree(allData, children);
                children.sort(Comparator.comparing(BusinessClassificationTreeDTO::getCreateTime).reversed());
            }
            // 加入父级
            parent.setChild(children);
        }
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
        ClassificationDefContentDTO param = dto.getClassificationDefs().get(0);
        // 查询数据
        QueryWrapper<BusinessClassificationPO> qw = new QueryWrapper<>();
        qw.eq("name", param.name).eq("del_flag", 1);
        BusinessClassificationPO model = businessClassificationMapper.selectOne(qw);
        if (model == null){
            throw new FkException(ResultEnum.ERROR, "业务分类不存在");
        }
        model.setDescription(param.description);
        if (businessClassificationMapper.updateByName(model) > 0){
            return ResultEnum.SUCCESS;
        }else{
            throw new FkException(ResultEnum.ERROR, "修改业务分类失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteClassification(String classificationName)
    {
        // 查询数据
        QueryWrapper<BusinessClassificationPO> qw = new QueryWrapper<>();
        qw.eq("name", classificationName);
        BusinessClassificationPO po = businessClassificationMapper.selectOne(qw);
        if (po == null){
            throw new FkException(ResultEnum.ERROR, "业务分类不存在");
        }

        List<Long> idList = new ArrayList<>();

        // 查询子集
        qw = new QueryWrapper<>();
        qw.eq("pid", po.getId());
        List<BusinessClassificationPO> children = businessClassificationMapper.selectList(qw);
        if (!CollectionUtils.isEmpty(children)){
            idList = children.stream().map(BusinessClassificationPO::getId).collect(Collectors.toList());
        }
        idList.add(po.getId());
        if (businessClassificationMapper.deleteBatchIds(idList) > 0){
            return ResultEnum.SUCCESS;
        }else{
            throw new FkException(ResultEnum.ERROR, "删除业务分类失败");
        }
    }

    @Override
    public ResultEnum addClassification(ClassificationDefsDTO dto)
    {
        List<ClassificationDefContentDTO> classificationDefList = dto.getClassificationDefs();
        for (ClassificationDefContentDTO item : classificationDefList){
            if (StringUtils.isEmpty(item.name)){
                throw new FkException(ResultEnum.ERROR, "业务分类名称不能为空");
            }

            // 查询数据
            QueryWrapper<BusinessClassificationPO> qw = new QueryWrapper<>();
            qw.eq("name", item.name).eq("del_flag", 1);
            BusinessClassificationPO bcPO = businessClassificationMapper.selectOne(qw);
            if (bcPO != null){
                throw new FkException(ResultEnum.ERROR, "业务分类名称已经存在");
            }

            // 添加数据
            BusinessClassificationPO model = new BusinessClassificationPO();
            model.setName(item.name);
            model.setDescription(item.description);

            // 设置父级id
            if (!CollectionUtils.isEmpty(item.superTypes)){
                model.setPid(Integer.valueOf(businessClassificationMapper.selectParentId(item.superTypes.get(0))));
            }else {
                model.setPid(null);
            }

            // 设置创建者信息
            //model.setCreateUser(userHelper.getLoginUserInfo().id.toString());
            int flag = businessClassificationMapper.insert(model);
            if (flag < 0){
                throw new FkException(ResultEnum.ERROR, "保存失败");
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum classificationAddAssociatedEntity(ClassificationAddEntityDTO dto)
    {

        // 业务分类和实体id
        MetadataClassificationMapPO model = new MetadataClassificationMapPO();
        model.setMetadataEntityId(Integer.parseInt(dto.entityGuids.get(0)));

        // 查询分类id
        QueryWrapper<BusinessClassificationPO> qw = new QueryWrapper<>();
        qw.eq("name", dto.classification.typeName);
        BusinessClassificationPO bcPo = businessClassificationMapper.selectOne(qw);
        model.setBusinessClassificationId((int) bcPo.id);
        if (metaDataClassificationMapMapper.insert(model) <= 0){
            throw new FkException(ResultEnum.ERROR, "业务分类关联实体失败");
        }
//        String jsonParameter=JSONArray.toJSON(dto).toString();
//        ResultDataDTO<String> result = atlasClient.post(bulkClassification, jsonParameter);
        Boolean exist = redisTemplate.hasKey("metaDataEntityData:"+dto.entityGuids.get(0));
        if (exist)
        {
            entity.setRedis(dto.entityGuids.get(0));
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum classificationDelAssociatedEntity(ClassificationDelAssociatedEntityDTO dto) {
        if (metaDataClassificationMapMapper.deleteById(dto.entityGuid) <= 0){
            throw new FkException(ResultEnum.ERROR, "业务分类删除实体失败");
        }

//        ResultDataDTO<String> result = atlasClient.delete(entityByGuid + "/" + dto.entityGuid + "/classification/" + dto.classificationName);
        return ResultEnum.SUCCESS;
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
        List<BusinessClassificationPO> poList = new ArrayList<>();

        //同步数据接入业务分类
        BusinessClassificationPO dataAccess = new BusinessClassificationPO();
        dataAccess.name = "业务数据";
        dataAccess.description = "业务数据";
        poList.add(dataAccess);

        //同步数仓建模业务分类
        BusinessClassificationPO dataModel = new BusinessClassificationPO();
        dataModel.name = "分析数据";
        dataModel.description = "分析数据";
        poList.add(dataModel);

        boolean flat = this.saveBatch(poList);
        if (flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
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
