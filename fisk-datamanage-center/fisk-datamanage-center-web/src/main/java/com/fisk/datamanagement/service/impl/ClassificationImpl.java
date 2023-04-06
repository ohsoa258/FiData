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
import com.fisk.datafactory.enums.DelFlagEnum;
import com.fisk.datamanagement.dto.businessclassification.BusinessClassificationTreeDTO;
import com.fisk.datamanagement.dto.classification.*;
import com.fisk.datamanagement.dto.entity.EntityFilterDTO;
import com.fisk.datamanagement.entity.*;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.enums.ClassificationTypeEnum;
import com.fisk.datamanagement.map.ClassificationMap;
import com.fisk.datamanagement.mapper.*;
import com.fisk.datamanagement.service.IClassification;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.AttributeTypeVO;
import com.fisk.datamanagement.vo.ResultDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
    MetaDataClassificationMapMapper metaDataClassificationMapMapper;

    @Resource
    AttributeTypeMapper attributeTypeMapper;
    @Resource
    ClassificationMapper classificationMapper;
    @Resource
    MetadataEntityClassificationAttributeMapper metadataEntityClassificationAttributeMapper;
    @Resource
    MetadataEntityClassificationAttributeMapImpl metadataEntityClassificationAttributeMap;

    @Resource
    AtlasClient atlasClient;

    @Value("${atlas.searchBasic}")
    private String searchBasic;


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
        // 参数校验
        if (CollectionUtils.isEmpty(dto.getClassificationDefs())){
            throw new FkException(ResultEnum.ERROR, "修改业务分类参数错误");
        }
        ClassificationDefContentDTO param = dto.getClassificationDefs().get(0);

        // 查询是否存在重复数据
        List<String> nameList = businessClassificationMapper.selectNameList(param.getGuid(), DelFlagEnum.NORMAL_FLAG.getValue());
        if (nameList.contains(param.name)){
            throw new FkException(ResultEnum.ERROR, "业务分类名称已存在");
        }

        // 查询当前业务分类
        QueryWrapper<BusinessClassificationPO> qw = new QueryWrapper<>();
        qw.eq("id", param.guid).eq("del_flag", 1);
        BusinessClassificationPO model = businessClassificationMapper.selectOne(qw);
        if (Objects.isNull(model)){
            throw new FkException(ResultEnum.ERROR, "业务分类不存在");
        }
        model.setName(param.name);
        model.setDescription(param.description);
        if (businessClassificationMapper.updateById(model) <= 0){
            throw new FkException(ResultEnum.ERROR, "修改业务分类失败");
        }
        return ResultEnum.SUCCESS;
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum classificationAddAssociatedEntity(ClassificationAddEntityDTO dto) {

        // 业务分类和实体id
        MetadataClassificationMapPO model = new MetadataClassificationMapPO();
        model.setMetadataEntityId(Integer.parseInt(dto.entityGuids.get(0)));

        // 查询分类id
        QueryWrapper<BusinessClassificationPO> qw = new QueryWrapper<>();
        qw.eq("name", dto.classification.typeName);
        BusinessClassificationPO bcPo = businessClassificationMapper.selectOne(qw);
        model.setBusinessClassificationId((int) bcPo.id);
        if (metaDataClassificationMapMapper.insert(model) <= 0) {
            throw new FkException(ResultEnum.ERROR, "业务分类关联实体失败");
        }

        //查询业务分类下属性
        QueryWrapper<ClassificationPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ClassificationPO::getBusinessClassificationId, bcPo.id);
        List<ClassificationPO> list = classificationMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return ResultEnum.SUCCESS;
        }
        List<Integer> collect = list.stream().map(e -> e.getAttributeTypeId()).collect(Collectors.toList());

        List<MetadataEntityClassificationAttributePO> dataList = new ArrayList<>();
        for (Integer id : collect) {
            MetadataEntityClassificationAttributePO po = new MetadataEntityClassificationAttributePO();
            po.attributeTypeId = id;
            po.metadataEntityId = Integer.parseInt(dto.entityGuids.get(0));
            po.classificationId = (int) bcPo.id;

            dataList.add(po);
        }

        boolean flat = metadataEntityClassificationAttributeMap.saveBatch(dataList);
        if (!flat) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum classificationDelAssociatedEntity(ClassificationDelAssociatedEntityDTO dto) {

        QueryWrapper<BusinessClassificationPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessClassificationPO::getName, dto.classificationName);
        BusinessClassificationPO po = businessClassificationMapper.selectOne(queryWrapper);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        QueryWrapper<MetadataClassificationMapPO> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.lambda()
                .eq(MetadataClassificationMapPO::getBusinessClassificationId, po.id)
                .eq(MetadataClassificationMapPO::getMetadataEntityId, dto.entityGuid);
        MetadataClassificationMapPO classificationMapPO = metaDataClassificationMapMapper.selectOne(queryWrapper1);
        if (classificationMapPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        int flat = metaDataClassificationMapMapper.deleteById(classificationMapPO.id);
        if (flat == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        QueryWrapper<MetadataEntityClassificationAttributePO> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.lambda()
                .eq(MetadataEntityClassificationAttributePO::getClassificationId, po.id)
                .eq(MetadataEntityClassificationAttributePO::getMetadataEntityId, dto.entityGuid);
        List<MetadataEntityClassificationAttributePO> attribute = metadataEntityClassificationAttributeMapper.selectList(queryWrapper2);
        if (CollectionUtils.isEmpty(attribute)) {
            return ResultEnum.SUCCESS;
        }
        boolean remove = metadataEntityClassificationAttributeMap.remove(queryWrapper2);
        if (!remove) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

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
        switch (dto.sourceType) {
            case 1:
                analysisModelSuperType.add(ClassificationTypeEnum.DATA_ACCESS.getName());
                break;
            case 2:
                analysisModelSuperType.add(ClassificationTypeEnum.ANALYZE_DATA.getName());
                break;
            case 3:
                analysisModelSuperType.add(ClassificationTypeEnum.API_GATEWAY_SERVICE.getName());
                break;
            case 4:
                analysisModelSuperType.add(ClassificationTypeEnum.DATABASE_SYNCHRONIZATION_SERVICE.getName());
                break;
            case 5:
                analysisModelSuperType.add(ClassificationTypeEnum.VIEW_ANALYZE_SERVICE.getName());
                break;
            default:
                break;
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

    @Override
    public ResultEnum addClassificationAttribute(ClassificationAttributeDTO dto) {
        // 查询业务分类是否存在
        BusinessClassificationPO model = businessClassificationMapper.selectById(dto.getGuid());
        if(Objects.isNull(model)){
            throw new FkException(ResultEnum.ERROR, "业务分类不存在");
        }

        // 根据属性类型查询属性id
        QueryWrapper<AttributeTypePO> qw = new QueryWrapper<>();
        qw.lambda().eq(AttributeTypePO::getName, dto.getTypeName());
        AttributeTypePO typePo = attributeTypeMapper.selectOne(qw);
        if (Objects.isNull(typePo)){
            throw new FkException(ResultEnum.ERROR, "属性类型不存在");
        }

        // 查询是否重复
        QueryWrapper<ClassificationPO> cqw = new QueryWrapper<>();
        cqw.lambda().ne(ClassificationPO::getId, dto.guid)
                .eq(ClassificationPO::getBusinessClassificationId, dto.getGuid())
                .eq(ClassificationPO::getAttributeName, dto.getName());
        ClassificationPO classificationPO = classificationMapper.selectOne(cqw);
        if (!Objects.isNull(classificationPO)){
            throw new FkException(ResultEnum.ERROR, "当前业务分类下已存在该属性名称");
        }

        // 添加业务分类属性
        ClassificationPO po = new ClassificationPO();
        po.setAttributeTypeId(typePo.getTypeId());
        po.setAttributeName(dto.getName());
        po.setBusinessClassificationId(Integer.parseInt(dto.getGuid()));

        int insert = classificationMapper.insert(po);
        if (insert <= 0){
            throw new FkException(ResultEnum.ERROR, "添加属性失败");
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<AttributeTypeVO> getClassificationAttributeList(String guid) {
        // 查询业务分类属性列表
        QueryWrapper<ClassificationPO> qw = new QueryWrapper<>();
        qw.eq("business_classification_id", guid);
        List<ClassificationPO> classificationPOList = classificationMapper.selectList(qw);
        if (CollectionUtils.isEmpty(classificationPOList)){
            return new ArrayList<>();
        }

        List<AttributeTypeVO> list = new ArrayList<>();
        for (ClassificationPO model : classificationPOList){
            AttributeTypeVO vo = new AttributeTypeVO();
            vo.setGuid(String.valueOf(model.getId()));
            vo.setName(model.getAttributeName());
            vo.setTypeId(model.getAttributeTypeId());
            vo.setTypeName(attributeTypeMapper.selectTypeName(model.getAttributeTypeId()));
            list.add(vo);
        }
        return list;
    }

    @Override
    public ResultEnum delClassificationAttribute(Integer id) {
        // 查询是否存在
        ClassificationPO model = classificationMapper.selectById(id);
        if (Objects.isNull(model)){
            throw new FkException(ResultEnum.ERROR, "数据不存在");
        }

        int i = classificationMapper.deleteById(id);
        if (i <= 0){
            throw new FkException(ResultEnum.ERROR, "删除失败");
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum updateClassificationAttribute(UpdateClassificationAttributeDTO dto) {
        log.info("属性参数[{}]", JSON.toJSONString(dto));
        // 查询数据
        ClassificationPO model = classificationMapper.selectById(dto.guid);
        if (Objects.isNull(model)){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 查询是否重复
        QueryWrapper<ClassificationPO> cqw = new QueryWrapper<>();
        cqw.lambda().ne(ClassificationPO::getId, dto.guid)
                .eq(ClassificationPO::getBusinessClassificationId, model.getBusinessClassificationId())
                .eq(ClassificationPO::getAttributeName, dto.getName());
        ClassificationPO classificationPO = classificationMapper.selectOne(cqw);
        if (!Objects.isNull(classificationPO)){
            throw new FkException(ResultEnum.ERROR, "当前业务分类下已存在该属性名称");
        }

        // 查询业务分类类型id
        QueryWrapper<AttributeTypePO> qw = new QueryWrapper<>();
        qw.lambda().eq(AttributeTypePO::getName, dto.getTypeName());
        AttributeTypePO typePo = attributeTypeMapper.selectOne(qw);
        if (Objects.isNull(typePo)){
            throw new FkException(ResultEnum.ERROR, "属性类型不存在");
        }

        // 更新属性
        model.setAttributeName(dto.getName());
        model.setAttributeTypeId(typePo.getTypeId());

        if (classificationMapper.updateById(model) <= 0){
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    public BusinessClassificationPO getInfoByName(String classificationName) {
        BusinessClassificationPO po = this.query().eq("name", classificationName).one();
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return po;
    }


}
