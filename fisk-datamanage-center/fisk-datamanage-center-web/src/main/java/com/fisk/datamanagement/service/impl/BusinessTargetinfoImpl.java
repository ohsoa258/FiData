package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisk.chartvisual.enums.IndicatorTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.BeanHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.mdc.TraceType;
import com.fisk.common.framework.mdc.TraceTypeEnum;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.consumeserveice.client.ConsumeServeiceClient;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflow.WorkFlowQueryDTO;
import com.fisk.datafactory.dto.customworkflow.WorkflowDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datafactory.enums.DelFlagEnum;
import com.fisk.datamanagement.dto.businessclassification.ChildBusinessTreeDTO;
import com.fisk.datamanagement.dto.category.CategoryQueryDTO;
import com.fisk.datamanagement.dto.category.IndexForAssetCatalogDTO;
import com.fisk.datamanagement.dto.classification.*;
import com.fisk.datamanagement.dto.metadataentity.DBTableFiledNameDto;
import com.fisk.datamanagement.entity.*;
import com.fisk.datamanagement.enums.DownLoadTypeEnum;
import com.fisk.datamanagement.map.ApiTreeHistoryMap;
import com.fisk.datamanagement.map.FactTreeHistoryMap;
import com.fisk.datamanagement.map.FactTreeMap;
import com.fisk.datamanagement.mapper.*;
import com.fisk.datamanagement.service.*;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.businessarea.BusinessAreaDTO;
import com.fisk.datamodel.dto.businessprocess.BusinessQueryDataParamDTO;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDTO;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderDTO;
import com.fisk.datamodel.dto.fact.FactDTO;
import com.fisk.datamodel.dto.factattribute.FactAttributeDTO;
import com.fisk.dataservice.vo.api.ApiConfigVO;
import com.fisk.dataservice.vo.api.FieldConfigVO;
import com.fisk.dataservice.vo.app.AppRegisterVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
import com.fisk.system.dto.userinfo.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xgf
 * @date 2023年11月20日 10:44
 */
@Slf4j
@Service
public class BusinessTargetinfoImpl extends ServiceImpl<BusinessTargetinfoMapper, BusinessTargetinfoPO> implements BusinessTargetinfoService {
    @Resource
    BusinessTargetinfoMapper businessTargetinfoMapper;
    @Resource
    MetadataEntityImpl metadataEntity;

    @Resource
    GenerateCondition generateCondition;
    @Resource
    UserHelper userHelper;
    @Resource
    FactTreeListMapper factTreeListMapper;
    @Resource
    ApitreelistMapper apitreelistMapper;
    @Resource
    ApitreelistService apitreelistService;
    @Resource
    BusinessExtendedfieldsMapper businessExtendedfieldsMapper;
    @Resource
    BusinessTargetinfoHistoryService businessTargetinfoHistoryService;
    @Resource
    BusinessExtendedfieldsHistoryService businessExtendedfieldsHistoryService;
    @Resource
    FacttreelistHistoryService facttreelistHistoryService;

    @Resource
    ApitreelistHistoryService apitreelistHistoryService;

    @Resource
    BusinessHistoryService businessHistoryService;

    @Resource
    BusinessCategoryMapper businessCategoryMapper;

    @Resource
    BusinessExtendedfieldsService businessExtendedfieldsService;

    @Resource
    FactTreeListExtendedfieldsService factTreeService;

    @Resource
    UserClient userClient;

    @Resource
    DataModelClient modelClient;

    @Resource
    BusinessCategoryAssignmentService businessCategoryAssignmentService;

    @Resource
    DataFactoryClient factoryClient;

    @Resource
    ConsumeServeiceClient consumeServeiceClient;

    private static final String[] parentTargetinfoHeaders = {"一级分类", "二级分类", "负责部门", "指标编码", "指标类型", "指标名称", "指标描述/口径", "指标范围",
            "计量单位", "统计周期", "指标公式", "指标脚本", "指标来源", "数据筛选条件", "来源系统", "来源数据表", "指标状态", "应用", "订单渠道", "数据粒度"};
    private static final String[] parentTargetinfoHeaders1 = {"负责部门", "指标编码", "指标类型", "指标名称", "指标描述/口径", "指标范围",
            "计量单位", "统计周期", "指标公式", "指标脚本", "指标来源", "数据筛选条件", "来源系统", "来源数据表", "指标状态", "应用", "数据粒度"};


    @Override
    public List<BusinessTargetinfoMenuDTO> getBusinessMetaDataDetailMenuList(String pid) {
        List<BusinessCategoryPO> businessCategoryPOS = businessCategoryMapper.selectList(new QueryWrapper<>());
        List<BusinessCategoryPO> self = businessCategoryPOS.stream().filter(i -> (int) i.getId() == Integer.valueOf(pid)).collect(Collectors.toList());
        self = self.stream().map(i -> {
            if (i.getPid() == null) {
                i.setPid(0);
            }
            return i;
        }).collect(Collectors.toList());
        List<BusinessCategoryPO> allChildrenCategories = getAllChildrenCategories(businessCategoryPOS, Integer.valueOf(pid));
        allChildrenCategories.addAll(self);
        List<Long> ids = allChildrenCategories.stream().map(i -> i.getId()).collect(Collectors.toList());
        List<BusinessTargetinfoMenuDTO> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            List<BusinessTargetinfoPO> list = businessTargetinfoMapper.selectClassification(ids);
            List<BusinessTargetinfoPO> parentBusinessIdIds = list.stream().filter(i -> i.getParentBusinessId() != null && i.getParentBusinessId() != 0).collect(Collectors.toList());
            Map<String,BusinessTargetinfoPO> parentBusinessMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(parentBusinessIdIds)){
                List<Integer> parentIds = parentBusinessIdIds.stream().map(BusinessTargetinfoPO::getParentBusinessId).collect(Collectors.toList());
                List<BusinessTargetinfoPO> businessTargetinfoPOList = businessTargetinfoMapper.selectBatchIds(parentIds);
                parentBusinessMap = businessTargetinfoPOList.stream().collect(Collectors.toMap(i->String.valueOf(i.getId()), i->i));
            }
            List<Map<String, Object>> menuTreeNames = businessTargetinfoMapper.getMenuTreeNames();
            Map<String, BusinessTargetinfoPO> finalParentBusinessMap = parentBusinessMap;
            result = list.stream().map(i -> {
                BusinessTargetinfoMenuDTO businessTargetinfoMenuDTO = new BusinessTargetinfoMenuDTO();
                businessTargetinfoMenuDTO.setId((int) i.getId());
                businessTargetinfoMenuDTO.setName(i.getIndicatorName());
                businessTargetinfoMenuDTO.setType(i.getIndicatorType());
//                Map<String, String> categoryNameMaps = businessCategoryPOS.stream().collect(Collectors.toMap(v -> String.valueOf(v.getId()), BusinessCategoryPO::getName));
                String path = null;
                if (!StringUtils.isEmpty(i.getName())){
                    businessTargetinfoMenuDTO.setCategoryId(i.getName());
                    List<Map<String, Object>> menuTreeName = menuTreeNames.stream().filter(j -> i.getName().equals(j.get("id").toString())).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(menuTreeName)){
                        Map<String, Object> menuTreeNameMap = menuTreeName.get(0);
                        Object full_path = menuTreeNameMap.get("full_path");
                        if (ObjectUtils.isNotEmpty(full_path)){
                            path = full_path.toString().replace("->", "/");
                        }
                        businessTargetinfoMenuDTO.setCategoryName(path);
                    }
                }
                businessTargetinfoMenuDTO.setParentBusinessId(i.getParentBusinessId());
                BusinessTargetinfoPO businessTargetinfoPO = finalParentBusinessMap.get(String.valueOf(i.getParentBusinessId()));
                if(businessTargetinfoPO != null){
                    businessTargetinfoMenuDTO.setParentBusinessCategoryId(businessTargetinfoPO.getPid());
                    businessTargetinfoMenuDTO.setParentBusinessName(businessTargetinfoPO.getIndicatorName());
                }
                businessTargetinfoMenuDTO.setIndicatorStatus(i.getIndicatorStatus());
                businessTargetinfoMenuDTO.setIndicatorCcode(i.getIndicatorCode());
                businessTargetinfoMenuDTO.setLargeScreenLink(i.getLargeScreenLink());
                businessTargetinfoMenuDTO.setSourceSystem(i.getSourceSystem());
                businessTargetinfoMenuDTO.setIndicatorDescription(i.getIndicatorDescription());
                businessTargetinfoMenuDTO.setCreateTime(i.getCreateTime());
                return businessTargetinfoMenuDTO;
            }).collect(Collectors.toList());
        }
        result.sort(Comparator.comparing(BusinessTargetinfoMenuDTO::getType,
                (type1, type2) -> {
                    if (StringUtils.isEmpty(type1) && StringUtils.isEmpty(type2)) {
                        return 0;
                    } else if (StringUtils.isEmpty(type1)) {
                        return 1;
                    } else if (StringUtils.isEmpty(type2)) {
                        return -1;
                    } else {
                        Map<String, Integer> order = new HashMap<>();
                        order.put("原子指标", 1);
                        order.put("派生指标", 2);
                        order.put("衍生指标", 3);

                        return order.get(type1) - order.get(type2);
                    }
                }).thenComparing(BusinessTargetinfoMenuDTO::getName, Collator.getInstance(Locale.CHINA)));
        return result;
    }

    /**
     * 查询指标明细数据
     *
     * @param id
     * @return
     */
    @Override
    public JSONObject SelectClassification(String id) {

//        JSONArray array1 =new JSONArray();
//        List<BusinessTargetinfoPO> list = businessTargetinfoMapper.selectClassification(pid);
//        if(CollectionUtils.isEmpty(list)){
//            return array1;
//        }
//        String indexid= pid;
        //List<FactTreePOs> list2 = factTreeListMapper.selectParentpIds(pid);
//        List<Integer> ParentBusinessIds = list.stream().map(BusinessTargetinfoPO::getParentBusinessId).collect(Collectors.toList());
//        LambdaQueryWrapper<BusinessTargetinfoPO> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(BusinessTargetinfoPO::getId,ParentBusinessIds);
        BusinessTargetinfoPO businessTargetinfoPO = businessTargetinfoMapper.selectById(id);
        BusinessCategoryPO businessCategoryPO = businessCategoryMapper.selectById(businessTargetinfoPO.getName());
//        String indexid = businessTargetinfoPO.getPid();
//        for(int i=0;i<list.size();i++){
        List<BusinessExtendedfieldsPO> list1 = businessExtendedfieldsMapper.selectParentpId(businessTargetinfoPO.getId() + "");
        List<FactTreePOs> list2 = factTreeListMapper.selectParentpIds(businessTargetinfoPO.getId() + "");
        List<Long> userIds = list2.stream()
                .filter(x -> org.apache.commons.lang.StringUtils.isNotEmpty(x.createUser))
                .map(x -> Long.valueOf(x.createUser))
                .distinct()
                .collect(Collectors.toList());
        ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userIds);
        if (userListByIds.code == ResultEnum.SUCCESS.getCode()
                && CollectionUtils.isNotEmpty(userListByIds.getData())) {
            list2.forEach(e -> {
                userListByIds.getData()
                        .stream()
                        .filter(user -> user.getId().toString().equals(e.createUser))
                        .findFirst()
                        .ifPresent(user -> e.createUser = user.userAccount);
            });
        }

        List<ApitreePO> list3 = apitreelistMapper.selectParentpIds(businessTargetinfoPO.getId() + "");
        List<Long> apiUserIds = list3.stream()
                .filter(x -> org.apache.commons.lang.StringUtils.isNotEmpty(x.createUser))
                .map(x -> Long.valueOf(x.createUser))
                .distinct()
                .collect(Collectors.toList());
        ResultEntity<List<UserDTO>> apiUserListByIds = userClient.getUserListByIds(apiUserIds);
        if (apiUserListByIds.code == ResultEnum.SUCCESS.getCode()
                && CollectionUtils.isNotEmpty(apiUserListByIds.getData())) {
            list3.forEach(e -> {
                apiUserListByIds.getData()
                        .stream()
                        .filter(user -> user.getId().toString().equals(e.createUser))
                        .findFirst()
                        .ifPresent(user -> e.createUser = user.userAccount);
            });
        }
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("id", id);
        jsonObject1.put("createTime", businessTargetinfoPO.getCreateTime());
        jsonObject1.put("createUser", businessTargetinfoPO.getCreateUser());
        jsonObject1.put("updateTime", businessTargetinfoPO.getUpdateTime());
        jsonObject1.put("updateUser", businessTargetinfoPO.getUpdateUser());
        jsonObject1.put("delFlag", businessTargetinfoPO.getDelFlag());
        jsonObject1.put("pid", businessTargetinfoPO.getPid());
        jsonObject1.put("responsibleDept", businessTargetinfoPO.getResponsibleDept());
        jsonObject1.put("indicatorCode", businessTargetinfoPO.getIndicatorCode());
        jsonObject1.put("indicatorName", businessTargetinfoPO.getIndicatorName());
        jsonObject1.put("indicatorDescription", businessTargetinfoPO.getIndicatorDescription());
        jsonObject1.put("indicatorLevel", businessTargetinfoPO.getIndicatorLevel());
        jsonObject1.put("largeScreenLink", businessTargetinfoPO.getLargeScreenLink());
        jsonObject1.put("unitMeasurement", businessTargetinfoPO.getUnitMeasurement());
        jsonObject1.put("statisticalCycle", businessTargetinfoPO.getStatisticalCycle());
        jsonObject1.put("indicatorformula", businessTargetinfoPO.getIndicatorformula());
        jsonObject1.put("indicatorStatus", businessTargetinfoPO.getIndicatorStatus());
        jsonObject1.put("filteringCriteria", businessTargetinfoPO.getFilteringCriteria());
        jsonObject1.put("dataGranularity", businessTargetinfoPO.getDataGranularity());
        jsonObject1.put("operationalAttributes", businessTargetinfoPO.getOperationalAttributes());
        jsonObject1.put("sourceSystem", businessTargetinfoPO.getSourceSystem());
        jsonObject1.put("sourceDataTable", businessTargetinfoPO.getSourceDataTable());
        jsonObject1.put("sourceIndicators", businessTargetinfoPO.getSourceIndicators());
        jsonObject1.put("orderChannel", businessTargetinfoPO.getOrderChannel());
        jsonObject1.put("indicatorType", businessTargetinfoPO.getIndicatorType());
        jsonObject1.put("nameId", businessTargetinfoPO.getName());
        jsonObject1.put("name", businessCategoryPO.getName());
        jsonObject1.put("sqlScript", businessTargetinfoPO.getSqlScript());
        if (CollectionUtils.isNotEmpty(list1)) {
            List<Integer> dimAttributeIds = list1.stream().map(q -> Integer.valueOf(q.getAttributeid())).collect(Collectors.toList());
            ResultEntity<List<DimensionAttributeDTO>> dimensionAttributeByIds = modelClient.getDimensionAttributeByIds(dimAttributeIds);
            List<DimensionAttributeDTO> dimAttributeDTOS = new ArrayList<>();
            if (dimensionAttributeByIds.code == ResultEnum.SUCCESS.getCode()) {
                dimAttributeDTOS = dimensionAttributeByIds.data;
            }
            List<Integer> dimFolderIds = list1.stream().map(q -> Integer.valueOf(q.getDimdomainid())).collect(Collectors.toList());
            ResultEntity<List<DimensionFolderDTO>> dimensionFolderByIds = modelClient.getDimensionFolderByIds(dimFolderIds);
            List<DimensionFolderDTO> dimFolderDTOS = new ArrayList<>();
            if (dimensionAttributeByIds.code == ResultEnum.SUCCESS.getCode()) {
                dimFolderDTOS = dimensionFolderByIds.data;
            }

            List<Integer> dimTableIds = list1.stream().map(q -> Integer.valueOf(q.getDimtableid())).collect(Collectors.toList());
            WorkFlowQueryDTO workFlowQueryDTO = new WorkFlowQueryDTO();
            workFlowQueryDTO.setTableIds(dimTableIds);
            workFlowQueryDTO.setTableType(ChannelDataEnum.DW_DIMENSION_TASK.getValue());
            ResultEntity<List<WorkflowDTO>> workFlowNameByTableId = factoryClient.getWorkFlowNameByTableId(workFlowQueryDTO);
            Map<Integer, List<WorkflowDTO>> dimWorkFlowMap = new HashMap<>();
            if (workFlowNameByTableId.code == ResultEnum.SUCCESS.getCode()) {
                List<WorkflowDTO> workFlows = workFlowNameByTableId.data;
                dimWorkFlowMap = workFlows.stream().collect(Collectors.groupingBy(WorkflowDTO::getTableId));
            }

            ResultEntity<List<DimensionDTO>> dimensionTableByIds = modelClient.getDimensionTableByIds(dimTableIds);
            List<DimensionDTO> dimTableDTOS = new ArrayList<>();
            if (dimensionAttributeByIds.code == ResultEnum.SUCCESS.getCode()) {
                dimTableDTOS = dimensionTableByIds.data;
            }
            Map<Integer, DimensionAttributeDTO> dimAttributeMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(dimAttributeDTOS)) {
                dimAttributeMap = dimAttributeDTOS.stream().collect(Collectors.toMap(e -> (int) e.getId(), e -> e));
            }
            Map<Integer, DimensionFolderDTO> dimFolderMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(dimFolderDTOS)) {
                dimFolderMap = dimFolderDTOS.stream().collect(Collectors.toMap(e -> (int) e.getBusinessId(), e -> e));
            }
            Map<Integer, DimensionDTO> dimTableMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(dimTableDTOS)) {
                dimTableMap = dimTableDTOS.stream().collect(Collectors.toMap(e -> (int) e.getId(), e -> e));
            }
            List<BusinessExtendedfieldsDTO> dimensionData = new ArrayList<>();
            for (BusinessExtendedfieldsPO businessExtendedfieldsPO : list1) {
                DimensionAttributeDTO dimensionAttribute = dimAttributeMap.get(Integer.valueOf(businessExtendedfieldsPO.getAttributeid()));
                DimensionFolderDTO dimensionFolderDTO = dimFolderMap.get(Integer.valueOf(businessExtendedfieldsPO.getDimdomainid()));
                DimensionDTO dimensionDTO = dimTableMap.get(Integer.valueOf(businessExtendedfieldsPO.getDimtableid()));

                BusinessExtendedfieldsDTO businessExtendedfieldsDTO = new BusinessExtendedfieldsDTO();
                businessExtendedfieldsDTO.setId(businessExtendedfieldsPO.id);
                businessExtendedfieldsDTO.setDimdomainid(businessExtendedfieldsPO.dimdomainid);
                if (dimensionFolderDTO != null) {
                    businessExtendedfieldsDTO.setDimdomain(dimensionFolderDTO.getDimensionFolderCnName());
                    businessExtendedfieldsDTO.setDimdomaintype(dimensionFolderDTO.dimensionFolderCnName);
                }
                String dimtableid = businessExtendedfieldsPO.dimtableid;
                if (dimtableid != null){
                    businessExtendedfieldsDTO.setDimtableid(dimtableid);
                    List<WorkflowDTO> workflowDTOS = dimWorkFlowMap.get(Integer.valueOf(dimtableid));
                    if (CollectionUtils.isNotEmpty(workflowDTOS)) {
                        businessExtendedfieldsDTO.setWorkflow(workflowDTOS);
                    }
                }
                if (dimensionDTO != null) {
                    businessExtendedfieldsDTO.setDimtable(dimensionDTO.getDimensionTabName());
                }
                businessExtendedfieldsDTO.setAttributeid(businessExtendedfieldsPO.attributeid);
                if (dimensionAttribute != null) {
                    businessExtendedfieldsDTO.setAttribute(dimensionAttribute.getDimensionFieldCnName());
                    businessExtendedfieldsDTO.setAttributeEnName(dimensionAttribute.getDimensionFieldEnName());
                }
                businessExtendedfieldsDTO.setIndexid(String.valueOf(id));
                businessExtendedfieldsDTO.setCreatedUser(businessExtendedfieldsPO.getCreatedUser());
                businessExtendedfieldsDTO.setCreatedTime(LocalDateTime.parse(businessExtendedfieldsPO.getCreatedTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"))));
                businessExtendedfieldsDTO.setDelFlag(businessExtendedfieldsPO.getDelFlag());
                dimensionData.add(businessExtendedfieldsDTO);
            }
            jsonObject1.put("dimensionData", dimensionData);
        }
        List<FacttreeListDTO> facttreeList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list2)) {
            List<Integer> factAttributeIds = list2.stream().map(e -> Integer.valueOf(e.getFactFieldEnNameId())).collect(Collectors.toList());
            ResultEntity<List<FactAttributeDTO>> factAttributeByIds = modelClient.getFactAttributeByIds(factAttributeIds);
            List<FactAttributeDTO> factAttributeDTOS = new ArrayList<>();
            if (factAttributeByIds.code == ResultEnum.SUCCESS.getCode()) {
                factAttributeDTOS = factAttributeByIds.data;
            }
            Map<Integer, FactAttributeDTO> factAttributeMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(factAttributeDTOS)) {
                factAttributeMap = factAttributeDTOS.stream().collect(Collectors.toMap(e -> (int) e.getId(), e -> e));
            }
            List<Integer> businessAreaIds = list2.stream().map(e -> Integer.valueOf(e.getBusinessNameId())).collect(Collectors.toList());
            ResultEntity<List<BusinessAreaDTO>> businessAreaByIds = modelClient.getBusinessAreaByIds(businessAreaIds);
            List<BusinessAreaDTO> businessAreaDTOS = new ArrayList<>();
            if (businessAreaByIds.code == ResultEnum.SUCCESS.getCode()) {
                businessAreaDTOS = businessAreaByIds.data;
            }
            Map<Integer, BusinessAreaDTO> businessAreaMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(factAttributeDTOS)) {
                businessAreaMap = businessAreaDTOS.stream().collect(Collectors.toMap(e -> (int) e.getId(), e -> e));
            }
            List<Integer> factTableIds = list2.stream().map(e -> Integer.valueOf(e.getFactTabNameId())).collect(Collectors.toList());
            WorkFlowQueryDTO workFlowQueryDTO = new WorkFlowQueryDTO();
            workFlowQueryDTO.setTableIds(factTableIds);
            workFlowQueryDTO.setTableType(ChannelDataEnum.DW_FACT_TASK.getValue());
            ResultEntity<List<WorkflowDTO>> workFlowNameByTableId = factoryClient.getWorkFlowNameByTableId(workFlowQueryDTO);
            Map<Integer, List<WorkflowDTO>> factWorkFlowMap = new HashMap<>();
            if (workFlowNameByTableId.code == ResultEnum.SUCCESS.getCode()) {
                List<WorkflowDTO> workFlows = workFlowNameByTableId.data;
                factWorkFlowMap = workFlows.stream().collect(Collectors.groupingBy(WorkflowDTO::getTableId));
            }

            ResultEntity<List<FactDTO>> factTableByIds = modelClient.getFactTableByIds(factTableIds);
            List<FactDTO> factTableDTOS = new ArrayList<>();
            if (factTableByIds.code == ResultEnum.SUCCESS.getCode()) {
                factTableDTOS = factTableByIds.data;
            }
            Map<Integer, FactDTO> factTableMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(factAttributeDTOS)) {
                factTableMap = factTableDTOS.stream().collect(Collectors.toMap(e -> (int) e.getId(), e -> e));
            }


            for (FactTreePOs factTreePOs : list2) {
                FacttreeListDTO facttreeListDTO = new FacttreeListDTO();
                facttreeListDTO.setId((int) factTreePOs.getId());
                facttreeListDTO.setPid(String.valueOf(id));
                BusinessAreaDTO businessAreaDTO = businessAreaMap.get(Integer.valueOf(factTreePOs.getBusinessNameId()));
                if (businessAreaDTO != null) {
                    facttreeListDTO.setBusinessName(businessAreaDTO.getBusinessName());
                }
                facttreeListDTO.setBusinessNameId(factTreePOs.businessNameId);
                FactDTO factDTO = factTableMap.get(Integer.valueOf(factTreePOs.getFactTabNameId()));
                if (factDTO != null) {
                    facttreeListDTO.setFactTabName(factDTO.getFactTabName());
                }
                String factTabNameId = factTreePOs.factTabNameId;
                if (factTabNameId != null){
                    facttreeListDTO.setFactTabNameId(factTabNameId);
                    List<WorkflowDTO> workflowDTOS = factWorkFlowMap.get(Integer.valueOf(factTabNameId));
                    if (CollectionUtils.isNotEmpty(workflowDTOS)) {
                        facttreeListDTO.setWorkflow(workflowDTOS);
                    }
                }
                FactAttributeDTO factAttribute = factAttributeMap.get(Integer.valueOf(factTreePOs.getFactFieldEnNameId()));
                if (factAttribute != null) {
                    facttreeListDTO.setFactFieldCnName(factAttribute.getFactFieldCnName());
                    facttreeListDTO.setFactFieldEnName(factAttribute.getFactFieldEnName());
                }
                facttreeListDTO.setFactFieldEnNameId(factTreePOs.factFieldEnNameId);
                facttreeListDTO.setCreateUser(factTreePOs.getCreateUser());
                facttreeListDTO.setCreateTime(factTreePOs.getCreateTime());
                facttreeListDTO.setDelFlag(factTreePOs.getDelFlag());
                facttreeList.add(facttreeListDTO);
            }

            jsonObject1.put("facttreeListData", facttreeList);
        }

        List<ApitreeListDTO> apitreeList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list3)) {
            List<Integer> factAttributeIds = list3.stream().map(e -> Integer.valueOf(e.getAttributeId())).collect(Collectors.toList());
            ResultEntity<List<FieldConfigVO>> apiAttributeByIds = consumeServeiceClient.getApiAttributeByIds(factAttributeIds);
            List<FieldConfigVO> apiAttributeDTOS = new ArrayList<>();
            if (apiAttributeByIds.code == ResultEnum.SUCCESS.getCode()) {
                apiAttributeDTOS = apiAttributeByIds.data;
            }
            Map<Integer, FieldConfigVO> apiAttributeMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(apiAttributeDTOS)) {
                apiAttributeMap = apiAttributeDTOS.stream().collect(Collectors.toMap(e -> (int) e.getId(), e -> e));
            }
            List<Integer> businessAppIds = list3.stream().map(e -> Integer.valueOf(e.getAppId())).collect(Collectors.toList());
            ResultEntity<List<AppRegisterVO>> businessAppByIds = consumeServeiceClient.getBusinessAppByIds(businessAppIds);
            List<AppRegisterVO> businessAppDTOS = new ArrayList<>();
            if (businessAppByIds.code == ResultEnum.SUCCESS.getCode()) {
                businessAppDTOS = businessAppByIds.data;
            }
            Map<Integer, AppRegisterVO> businessAppMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(apiAttributeDTOS)) {
                businessAppMap = businessAppDTOS.stream().collect(Collectors.toMap(e -> (int) e.getId(), e -> e));
            }
            List<Integer> apiIds = list3.stream().map(e -> Integer.valueOf(e.getApiId())).collect(Collectors.toList());

            ResultEntity<List<ApiConfigVO>> apiByIds = consumeServeiceClient.getApiByIds(apiIds);
            List<ApiConfigVO> apiDTOS = new ArrayList<>();
            if (apiByIds.code == ResultEnum.SUCCESS.getCode()) {
                apiDTOS = apiByIds.data;
            }
            Map<Integer, ApiConfigVO> apiMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(apiDTOS)) {
                apiMap = apiDTOS.stream().collect(Collectors.toMap(e -> (int) e.getId(), e -> e));
            }


            for (ApitreePO apiTreePO : list3) {
                ApitreeListDTO apitreeListDTO = new ApitreeListDTO();
                apitreeListDTO.setId((int) apiTreePO.getId());
                apitreeListDTO.setPid(String.valueOf(id));
                AppRegisterVO appRegisterVO = businessAppMap.get(Integer.valueOf(apiTreePO.getAppId()));
                if (appRegisterVO != null) {
                    apitreeListDTO.setAppName(appRegisterVO.getAppName());
                }
                apitreeListDTO.setAppId(apiTreePO.getAppId());
                ApiConfigVO apiConfigVO = apiMap.get(Integer.valueOf(apiTreePO.getApiId()));
                if (apiConfigVO != null) {
                    apitreeListDTO.setApiName(apiConfigVO.getApiName());
                }
                FieldConfigVO apiAttribute = apiAttributeMap.get(Integer.valueOf(apiTreePO.getAttributeId()));
                if (apiAttribute != null) {
                    apitreeListDTO.setAttributeName(apiAttribute.getFieldName());
                }
                apitreeListDTO.setApiId(apiTreePO.getApiId());
                apitreeListDTO.setAttributeId(apiTreePO.getAttributeId());
                apitreeListDTO.setCreateUser(apiTreePO.getCreateUser());
                apitreeListDTO.setCreateTime(apiTreePO.getCreateTime());
                apitreeListDTO.setDelFlag(apiTreePO.getDelFlag());
                apitreeList.add(apitreeListDTO);
            }

            jsonObject1.put("apitreeListData", apitreeList);
        }
        Integer parentBusinessId = businessTargetinfoPO.getParentBusinessId();
        List<BusinessCategoryPO> businessCategoryPOS = businessCategoryMapper.selectList(new QueryWrapper<>());
        if (parentBusinessId != null) {
            BusinessTargetinfoPO parentBusiness = businessTargetinfoMapper.selectById(parentBusinessId);
            if (parentBusiness != null) {
                jsonObject1.put("parentBusinessId", parentBusinessId);
                String hierarchyPath = getHierarchyPath(businessCategoryPOS, Integer.parseInt(parentBusiness.getPid()));
                jsonObject1.put("parentBusinessName", hierarchyPath+"/"+parentBusiness.getIndicatorName());
            }
        }
        if (IndicatorTypeEnum.ATOMIC_INDICATORS.getName().equals(businessTargetinfoPO.getIndicatorType())) {
            LambdaQueryWrapper<BusinessTargetinfoPO> businessTargetinfoWrapper = new LambdaQueryWrapper<>();
            businessTargetinfoWrapper.eq(BusinessTargetinfoPO::getParentBusinessId, businessTargetinfoPO.id);
            List<BusinessTargetinfoPO> businessTargetinfoPOList = businessTargetinfoMapper.selectList(businessTargetinfoWrapper);

            Map<String, ChildBusinessTreeDTO> childBusinessMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(businessTargetinfoPOList)) {
                childBusinessMap = businessTargetinfoPOList.stream().map(t -> {
                    ChildBusinessTreeDTO treeDTO = new ChildBusinessTreeDTO();
                    treeDTO.setId(String.valueOf(t.getId()));
                    treeDTO.setPid(t.getPid());
                    treeDTO.setType(2);
                    treeDTO.setName(t.getIndicatorName());
                    return treeDTO;
                }).collect(Collectors.toMap(ChildBusinessTreeDTO::getId, t -> t));
            }

            List<BusinessCategoryPO> data = businessCategoryPOS;
            List<ChildBusinessTreeDTO> allBusinessTree = data.stream().map(e -> {
                ChildBusinessTreeDTO treeDTO = new ChildBusinessTreeDTO();
                treeDTO.setId(String.valueOf(e.getId()));
                treeDTO.setPid(String.valueOf(e.getPid()));
                treeDTO.setType(1);
                treeDTO.setName(e.getName());
                return treeDTO;
            }).collect(Collectors.toList());


            List<ChildBusinessTreeDTO> parentTree = new ArrayList<>();
            for (Map.Entry<String, ChildBusinessTreeDTO> entry : childBusinessMap.entrySet()) {
                ChildBusinessTreeDTO child = entry.getValue();
                child = findParentById(allBusinessTree, child.getPid(), child);
                parentTree.add(child);
            }
            if (CollectionUtils.isNotEmpty(parentTree)){
                parentTree.sort(Comparator.comparing(ChildBusinessTreeDTO::getName, Collator.getInstance(Locale.CHINA)));
            }
            jsonObject1.put("derivedMetric", parentTree);
        }
//
//            array1.set(i,jsonObject1);
//        }

        return jsonObject1;
    }

    private String getHierarchyPath(List<BusinessCategoryPO> nodes, int pid) {
        for (BusinessCategoryPO node : nodes) {
            if (node.id == pid) {
                if (node.pid == null || node.pid == 0) {
                    return node.name;
                } else {
                    String parentPath = getHierarchyPath(nodes, node.pid);
                    return parentPath + "/" + node.name;
                }
            }
        }
        return ""; // 如果未找到匹配的 pid，返回空字符串
    }
    private ChildBusinessTreeDTO findParentById(List<ChildBusinessTreeDTO> treeList, String pid, ChildBusinessTreeDTO child) {
        ChildBusinessTreeDTO tree = child;
        for (ChildBusinessTreeDTO childBusinessTreeDTO : treeList) {
            ChildBusinessTreeDTO newChild = new ChildBusinessTreeDTO();
            newChild.setId(childBusinessTreeDTO.getId());
            newChild.setType(childBusinessTreeDTO.getType());
            newChild.setSort(childBusinessTreeDTO.getSort());
            newChild.setPid(childBusinessTreeDTO.getPid());
            newChild.setName(childBusinessTreeDTO.getName());
            if (newChild.id.equals(pid)) {
                newChild.setChild(child);
                tree = findParentById(treeList, newChild.pid, newChild);
            }
        }
        return tree;
    }


    @Override
    public JSONArray SelectClassifications(Integer fieldMetadataId) {

        JSONArray array1 = new JSONArray();
        DBTableFiledNameDto dbTableFiledNameDto = metadataEntity.getParentNameByFieldIdV2(fieldMetadataId);
        if (dbTableFiledNameDto == null) {
            return array1;
        }
        List<String> list2 = factTreeListMapper.selectsParentpIdsV2(dbTableFiledNameDto.getTbId(), dbTableFiledNameDto.getFieldId());
        for (int n = 0; n < list2.size(); n++) {
            String id = list2.get(n);
            List<BusinessTargetinfoPO> list = businessTargetinfoMapper.selectClassificationss(id);
            //List<FactTreePOs> list2 = factTreeListMapper.selectParentpIds(pid);
            for (int i = 0; i < list.size(); i++) {
                List<BusinessExtendedfieldsPO> list1 = businessExtendedfieldsMapper.selectParentpId(list.get(i).getId() + "");
                List<FactTreePOs> list3 = factTreeListMapper.selectParentpIds(list.get(i).getId() + "");
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("id", list.get(i).getId());
                jsonObject1.put("createTime", list.get(i).getCreateTime());
                jsonObject1.put("createUser", list.get(i).getCreateUser());
                jsonObject1.put("updateTime", list.get(i).getUpdateTime());
                jsonObject1.put("updateUser", list.get(i).getUpdateUser());
                jsonObject1.put("delFlag", list.get(i).getDelFlag());
                jsonObject1.put("pid", list.get(i).getPid());
                jsonObject1.put("responsibleDept", list.get(i).getResponsibleDept());
                jsonObject1.put("indicatorCode", list.get(i).getIndicatorCode());
                jsonObject1.put("indicatorName", list.get(i).getIndicatorName());
                jsonObject1.put("indicatorDescription", list.get(i).getIndicatorDescription());
                jsonObject1.put("indicatorLevel", list.get(i).getIndicatorLevel());
                jsonObject1.put("unitMeasurement", list.get(i).getUnitMeasurement());
                jsonObject1.put("statisticalCycle", list.get(i).getStatisticalCycle());
                jsonObject1.put("indicatorformula", list.get(i).getIndicatorformula());
                jsonObject1.put("indicatorStatus", list.get(i).getIndicatorStatus());
                jsonObject1.put("filteringCriteria", list.get(i).getFilteringCriteria());
                jsonObject1.put("dataGranularity", list.get(i).getDataGranularity());
                jsonObject1.put("operationalAttributes", list.get(i).getOperationalAttributes());
                jsonObject1.put("sourceSystem", list.get(i).getSourceSystem());
                jsonObject1.put("sourceDataTable", list.get(i).getSourceDataTable());
                jsonObject1.put("sourceIndicators", list.get(i).getSourceIndicators());
                jsonObject1.put("orderChannel", list.get(i).getOrderChannel());
                jsonObject1.put("indicatorType", list.get(i).getIndicatorType());
                jsonObject1.put("name", list.get(i).getName());
                jsonObject1.put("sqlScript", list.get(i).getSqlScript());
                jsonObject1.put("dimensionData", list1);
                List<FacttreeListDTO> facttreeListData = list3.stream().map(FactTreeMap.INSTANCES::poToDto).collect(Collectors.toList());
                jsonObject1.put("facttreeListData", facttreeListData);
                if (jsonObject1 != null) {
                    array1.set(n, jsonObject1);
                }

            }

        }

        return array1;
    }


    /**
     * 查询数据类型范围的数据
     *
     * @return
     */
    @Override
    public JSONObject SelecttypeClassification() {
        List<BusinessSynchronousPO> list = businessTargetinfoMapper.selecttypeClassification();
        JSONArray statisticalcycle = new JSONArray();
        JSONArray indicatorlevel = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            String typeData = list.get(i).typeData;
            if ("1".equals(typeData)) {
                JSONObject statisticalcyclejson = new JSONObject();
                statisticalcyclejson.put("statisticalcyclejson", list.get(i).typeName);
                statisticalcycle.add(statisticalcyclejson);
            } else if ("2".equals(typeData)) {
                JSONObject indicatorleveljson = new JSONObject();
                indicatorleveljson.put("indicatorleveljson", list.get(i).typeName);
                indicatorlevel.add(indicatorleveljson);
            }
        }
        JSONObject json = new JSONObject();
        json.put("indicatorlevel", indicatorlevel.toJSONString());
        json.put("statisticalcycle", statisticalcycle.toJSONString());
        return json;
    }

    /**
     * 向数据库中添加指标明细数据
     *
     * @param
     */
    @Override
    public ResultEnum addTargetinfo(BusinessTargetinfoDefsDTO dto) {
        List<BusinessTargetinfoDTO> classificationDefList = dto.businessTargetinfoDefs;
        for (BusinessTargetinfoDTO item : classificationDefList) {
            if (StringUtils.isEmpty(item.indicatorName)) {
                throw new FkException(ResultEnum.ERROR, "指标主题名称不能为空");
            }
            // 查询数据
            QueryWrapper<BusinessTargetinfoPO> qw = new QueryWrapper<>();
            qw.eq("indicator_name", item.indicatorName).eq("del_flag", 1).eq("pid", item.pid);
            BusinessTargetinfoPO bcPO = businessTargetinfoMapper.selectOne(qw);
            if (bcPO != null) {
                throw new FkException(ResultEnum.ERROR, "指标主题名称已经存在");
            }
            // 添加数据
            BusinessTargetinfoPO model = new BusinessTargetinfoPO();
            model.setIndicatorName(item.indicatorName);
            model.setIndicatorDescription(item.indicatorDescription);
            model.setPid(item.pid);
            model.setAttributesNumber(item.attributesNumber);
            model.setDataGranularity(item.dataGranularity);
            model.setFilteringCriteria(item.filteringCriteria);
            model.setIndicatorCode(item.indicatorCode);
            model.setIndicatorformula(item.indicatorformula);
            model.setIndicatorLevel(item.indicatorLevel);
            model.setIndicatorStatus(item.indicatorStatus);
            model.setLargeScreenLink(item.largeScreenLink);
            model.setOperationalAttributes(item.operationalAttributes);
            model.setOrderChannel(item.orderChannel);
            model.setSourceDataTable(item.sourceDataTable);
            model.setSourceIndicators(item.sourceIndicators);
            model.setStatisticalCycle(item.statisticalCycle);
            model.setResponsibleDept(item.responsibleDept);
            model.setSourceSystem(item.sourceSystem);
            model.setUnitMeasurement(item.unitMeasurement);
            model.setIndicatorType(item.indicatorType);
            model.setName(item.name);
            model.setSqlScript(item.sqlScript);
            if (IndicatorTypeEnum.DERIVED_INDICATORS.getName().equals(item.indicatorType)) {
                model.setParentBusinessId(item.parentBusinessId);
            }
            int flag = businessTargetinfoMapper.insert(model);
            if (flag < 0) {
                throw new FkException(ResultEnum.ERROR, "保存失败");
            }
            QueryWrapper<BusinessTargetinfoPO> qwnew = new QueryWrapper<>();
            qw.eq("indicator_name", item.indicatorName).eq("del_flag", 1).eq("pid", item.pid);
            BusinessTargetinfoPO bcPOnew = businessTargetinfoMapper.selectOne(qw);
            for (int j = 0; j < item.getDimensionData().size(); j++) {
                BusinessExtendedfieldsDTO model2 = item.getDimensionData().get(j);
                BusinessExtendedfieldsPO model1 = new BusinessExtendedfieldsPO();
                model1.setAttributeid(model2.attributeid);
                model1.setDimdomainid(model2.dimdomainid);
                model1.setDimtableid(model2.dimtableid);
                model1.setIndexid(bcPOnew.id + "");
                model1.setCreatedUser(userHelper.getLoginUserInfo().username);
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                model1.setCreatedTime(format.format(new Date()));
                model1.setDelFlag(1);
                int flag1 = businessExtendedfieldsMapper.insert(model1);
                if (flag1 < 0) {
                    throw new FkException(ResultEnum.ERROR, "保存失败");
                }
            }
            for (int m = 0; m < item.getFacttreeListData().size(); m++) {
                FacttreeListDTO model4 = item.getFacttreeListData().get(m);
                FactTreePOs model3 = new FactTreePOs();
                model3.setPid(bcPOnew.id + "");
                model3.setBusinessNameId(model4.businessNameId);
                model3.setFactTabNameId(model4.factTabNameId);
                model3.setFactFieldEnNameId(model4.factFieldEnNameId);
                model3.setCreateUser(userHelper.getLoginUserInfo().username);
                model3.setCreateTime(LocalDateTime.now());
                model3.setDelFlag(1);
                int flag2 = factTreeListMapper.insert(model3);
                if (flag2 < 0) {
                    throw new FkException(ResultEnum.ERROR, "保存失败");
                }
            }
            for (int m = 0; m < item.getApitreeListData().size(); m++) {
                ApitreeListDTO apitreeListDTO = item.getApitreeListData().get(m);
                ApitreePO apitreePO = new ApitreePO();
                apitreePO.setPid(bcPOnew.id + "");
                apitreePO.setAppId(apitreeListDTO.getAppId());
                apitreePO.setApiId(apitreeListDTO.getApiId());
                apitreePO.setAttributeId(apitreeListDTO.getAttributeId());
                apitreePO.setCreateUser(userHelper.getLoginUserInfo().username);
                apitreePO.setCreateTime(LocalDateTime.now());
                apitreePO.setDelFlag(1);
                int flag2 = apitreelistMapper.insert(apitreePO);
                if (flag2 < 0) {
                    throw new FkException(ResultEnum.ERROR, "保存失败");
                }
            }

        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum addHistoryBusinessMetaDataDetail(BusinessTargetinfoDefsDTO dto) {
        List<BusinessTargetinfoDTO> classificationDefList = dto.businessTargetinfoDefs;
        for (BusinessTargetinfoDTO item : classificationDefList) {
            if (StringUtils.isEmpty(item.indicatorName)) {
                throw new FkException(ResultEnum.ERROR, "指标主题名称不能为空");
            }
            // 查询数据
            BusinessTargetinfoPO model = businessTargetinfoMapper.selectById(item.id);
            // 设置当前时间为historyId
            Date currentTime = new Date();
            // 创建时间格式化对象
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String historyId = sdf.format(currentTime);
            BusinessTargetinfoHistoryPO targetinfoHistoryPO = BeanHelper.copyProperties(model, BusinessTargetinfoHistoryPO.class);
            targetinfoHistoryPO.setPid(Integer.valueOf(model.pid));
            targetinfoHistoryPO.setHistoryId(historyId);
            BusinessCategoryPO businessCategoryPO = businessCategoryMapper.selectById(model.name);
            targetinfoHistoryPO.setName(businessCategoryPO.getName());
            if (IndicatorTypeEnum.ATOMIC_INDICATORS.getName().equals(targetinfoHistoryPO.getIndicatorType())) {
                LambdaQueryWrapper<BusinessTargetinfoPO> businessTargetinfoWrapper = new LambdaQueryWrapper<>();
                businessTargetinfoWrapper.eq(BusinessTargetinfoPO::getParentBusinessId, model.getId());
                List<BusinessTargetinfoPO> businessTargetinfoPOList = businessTargetinfoMapper.selectList(businessTargetinfoWrapper);

                Map<String, ChildBusinessTreeDTO> childBusinessMap = new HashMap<>();
                if (CollectionUtils.isNotEmpty(businessTargetinfoPOList)) {
                    childBusinessMap = businessTargetinfoPOList.stream().map(t -> {
                        ChildBusinessTreeDTO treeDTO = new ChildBusinessTreeDTO();
                        treeDTO.setId(String.valueOf(t.getId()));
                        treeDTO.setPid(t.getPid());
                        treeDTO.setType(2);
                        treeDTO.setName(t.getIndicatorName());
                        return treeDTO;
                    }).collect(Collectors.toMap(ChildBusinessTreeDTO::getId, t -> t));
                }

                List<BusinessCategoryPO> data = businessCategoryMapper.selectList(new QueryWrapper<>());
                List<ChildBusinessTreeDTO> allBusinessTree = data.stream().map(e -> {
                    ChildBusinessTreeDTO treeDTO = new ChildBusinessTreeDTO();
                    treeDTO.setId(String.valueOf(e.getId()));
                    treeDTO.setPid(String.valueOf(e.getPid()));
                    treeDTO.setType(1);
                    treeDTO.setName(e.getName());
                    return treeDTO;
                }).collect(Collectors.toList());


                List<ChildBusinessTreeDTO> parentTree = new ArrayList<>();
                for (Map.Entry<String, ChildBusinessTreeDTO> entry : childBusinessMap.entrySet()) {
                    ChildBusinessTreeDTO child = entry.getValue();
                    parentTree.add(findParentById(allBusinessTree, child.getPid(), child));
                }
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    if (CollectionUtils.isNotEmpty(parentTree)) {
                        targetinfoHistoryPO.setDerivedMetric(objectMapper.writeValueAsString(parentTree));
                    }
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

            } else if (IndicatorTypeEnum.DERIVED_INDICATORS.getName().equals(targetinfoHistoryPO.getIndicatorType())) {
                List<BusinessCategoryPO> data = businessCategoryMapper.selectList(new QueryWrapper<>());
                if (model.parentBusinessId != null) {
                    LambdaQueryWrapper<BusinessTargetinfoPO> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(BusinessTargetinfoPO::getId, model.parentBusinessId);
                    BusinessTargetinfoPO businessTargetinfoPOS = businessTargetinfoMapper.selectOne(queryWrapper);
                    if (businessTargetinfoPOS != null){
                        targetinfoHistoryPO.setParentBusinessId(businessTargetinfoPOS.parentBusinessId);
                        String hierarchyPath = getHierarchyPath(data, Integer.parseInt(businessTargetinfoPOS.getPid()));
                        targetinfoHistoryPO.setParentBusinessName(hierarchyPath+"/"+businessTargetinfoPOS.getIndicatorName());
                    }
                }
            }

            boolean save = businessTargetinfoHistoryService.save(targetinfoHistoryPO);
            if (!save) {
                throw new FkException(ResultEnum.ERROR, "保存历史数据失败");
            }

            String finalHistoryId = historyId;
            List<BusinessExtendedfieldsDTO> dimensionData = item.getDimensionData();
        //保存历史数据
        if (CollectionUtils.isNotEmpty(dimensionData)){
            List<BusinessExtendedfieldsHistoryPO> extendedfieldsHistorys = dimensionData.stream().map(i -> {
                BusinessExtendedfieldsHistoryPO businessExtendedfieldsHistoryPO = BeanHelper.copyProperties(i, BusinessExtendedfieldsHistoryPO.class);
                businessExtendedfieldsHistoryPO.setHistoryId(finalHistoryId);
                businessExtendedfieldsHistoryPO.setCreatedUser(i.createdUser);
                businessExtendedfieldsHistoryPO.setCreatedTime(String.valueOf(i.createdTime));
                businessExtendedfieldsHistoryPO.setDimdomainid(i.dimdomainid);
                businessExtendedfieldsHistoryPO.setDimdomain(i.dimdomain);
                businessExtendedfieldsHistoryPO.setDimdomainid(i.dimdomainid);
                businessExtendedfieldsHistoryPO.setDimtable(i.dimtable);
                businessExtendedfieldsHistoryPO.setDimdomaintype(i.dimdomaintype);
                businessExtendedfieldsHistoryPO.setAttribute(i.attribute);
                businessExtendedfieldsHistoryPO.setAttributeid(i.attributeid);
                businessExtendedfieldsHistoryPO.setAttributeEnName(i.attributeEnName);
                businessExtendedfieldsHistoryPO.setDelFlag(1);
                return businessExtendedfieldsHistoryPO;
            }).collect(Collectors.toList());
            boolean b = businessExtendedfieldsHistoryService.saveBatch(extendedfieldsHistorys);
            if (!b){
                throw new FkException(ResultEnum.ERROR, "保存历史数据失败");
            }
        }

        //保存历史数据
        List<FacttreeListDTO> facttreeListData = item.getFacttreeListData();
        if (CollectionUtils.isNotEmpty(facttreeListData)){
            String finalHistoryId1 = historyId;
            List<FacttreelistHistoryPO> factHistoryTreePOs = facttreeListData.stream().map(i -> {
                FacttreelistHistoryPO facttreelistHistoryPO = new FacttreelistHistoryPO();
                facttreelistHistoryPO.setHistoryId(finalHistoryId1);
                facttreelistHistoryPO.setPid(item.id+"");
                facttreelistHistoryPO.setBusinessNameId(i.businessNameId);
                facttreelistHistoryPO.setBusinessName(i.businessName);
                facttreelistHistoryPO.setFactFieldEnNameId(i.factFieldEnNameId);
                facttreelistHistoryPO.setFactTabName(i.factTabName);
                facttreelistHistoryPO.setFactFieldEnNameId(i.factFieldEnNameId);
                facttreelistHistoryPO.setFactFieldEnName(i.factFieldEnName);
                facttreelistHistoryPO.setFactFieldCnName(i.factFieldCnName);
                facttreelistHistoryPO.setCreateUser(i.createUser);
                facttreelistHistoryPO.setCreateTime(i.createTime);
                facttreelistHistoryPO.setDelFlag(1);
                return facttreelistHistoryPO;
            }).collect(Collectors.toList());
            boolean b = facttreelistHistoryService.saveBatch(factHistoryTreePOs);
            if (!b){
                throw new FkException(ResultEnum.ERROR, "保存历史数据失败");
            }
        }

        //保存历史数据
        List<ApitreeListDTO> apitreeListData = item.getApitreeListData();
        if (CollectionUtils.isNotEmpty(apitreeListData)){
            String finalHistoryId1 = historyId;
            List<ApitreelistHistoryPO> apiHistoryTreePOs = apitreeListData.stream().map(i -> {
                ApitreelistHistoryPO apitreelistHistoryPO = new ApitreelistHistoryPO();
                apitreelistHistoryPO.setHistoryId(finalHistoryId1);
                apitreelistHistoryPO.setPid(item.id+"");
                apitreelistHistoryPO.setApiId(i.apiId);
                apitreelistHistoryPO.setApiName(i.apiName);
                apitreelistHistoryPO.setAppName(i.appName);
                apitreelistHistoryPO.setAppId(i.appId);
                apitreelistHistoryPO.setAttributeName(i.attributeName);
                apitreelistHistoryPO.setAttributeId(i.attributeId);
                apitreelistHistoryPO.setCreateUser(i.createUser);
                apitreelistHistoryPO.setCreateTime(i.createTime);
                apitreelistHistoryPO.setDelFlag(1);
                return apitreelistHistoryPO;
            }).collect(Collectors.toList());
            boolean b = apitreelistHistoryService.saveBatch(apiHistoryTreePOs);
            if (!b){
                throw new FkException(ResultEnum.ERROR, "保存历史数据失败");
            }
        }
        BusinessHistoryPO businessHistoryPO = new BusinessHistoryPO();
        businessHistoryPO.setHistoryId(historyId);
        businessHistoryPO.setTargetinfoId((int) model.getId());
        boolean save1 = businessHistoryService.save(businessHistoryPO);
        if (!save1) {
            throw new FkException(ResultEnum.ERROR, "保存历史数据失败");
        }
        }
        return ResultEnum.SUCCESS;
    }


    /**
     * 根据指标id删除指标数据
     *
     * @param Id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteTargetinfo(long Id) {
        // 查询数据
        QueryWrapper<BusinessTargetinfoPO> qw = new QueryWrapper<>();
        qw.eq("id", Id);
        BusinessTargetinfoPO po = businessTargetinfoMapper.selectOne(qw);
        if (po == null) {
            throw new FkException(ResultEnum.ERROR, "指标数据不存在");
        }

        List<Long> idList = new ArrayList<>();
        qw.eq("id", po.getId());
        idList.add(po.getId());
        if (businessTargetinfoMapper.deleteBatchIds(idList) > 0) {
            businessTargetinfoMapper.updateParentBusinessId(po.getParentBusinessId());
            businessExtendedfieldsMapper.updateByName(po.getId() + "");

            LambdaQueryWrapper<FactTreePOs> factDeleteWrapper = new LambdaQueryWrapper<>();
            factDeleteWrapper.eq(FactTreePOs::getPid, po.getId());
            factTreeListMapper.delete(factDeleteWrapper);

            LambdaQueryWrapper<ApitreePO> apiDeleteWrapper = new LambdaQueryWrapper<>();
            apiDeleteWrapper.eq(ApitreePO::getPid, po.getId());
            apitreelistMapper.delete(apiDeleteWrapper);

            return ResultEnum.SUCCESS;
        } else {
            throw new FkException(ResultEnum.ERROR, "删除指标数据失败");
        }
    }

    /**
     * 更改指标明细数据
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum updateTargetinfo(BusinessTargetinfoDefsDTO dto) {
        // 参数校验
        if (CollectionUtils.isEmpty(dto.getBusinessTargetinfoDefs())) {
            throw new FkException(ResultEnum.ERROR, "修改指标明细数据错误");
        }
        BusinessTargetinfoDTO item = dto.getBusinessTargetinfoDefs().get(0);

        // 查询是否存在重复数据
        List<String> nameList = businessTargetinfoMapper.selectNameList(item.pid, DelFlagEnum.NORMAL_FLAG.getValue());
        if (nameList.contains(item.indicatorName)) {
            throw new FkException(ResultEnum.ERROR, "同一分类下指标名称已存在");
        }

        // 查询当前业务分类
        QueryWrapper<BusinessTargetinfoPO> qw = new QueryWrapper<>();
        qw.eq("id", item.id).eq("del_flag", 1);
        BusinessTargetinfoPO model = businessTargetinfoMapper.selectOne(qw);
        if (Objects.isNull(model)) {
            throw new FkException(ResultEnum.ERROR, "指标明细数据不存在");
        }
//
//        // 设置当前时间为historyId
//        Date currentTime = new Date();
//        // 创建时间格式化对象
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
//        String historyId = sdf.format(currentTime);
//        BusinessTargetinfoHistoryPO targetinfoHistoryPO = BeanHelper.copyProperties(model, BusinessTargetinfoHistoryPO.class);
//        targetinfoHistoryPO.setPid(Integer.valueOf(model.pid));
//        targetinfoHistoryPO.setHistoryId(historyId);
//        BusinessCategoryPO businessCategoryPO = businessCategoryMapper.selectById(model.name);
//        targetinfoHistoryPO.setName(businessCategoryPO.getName());
//        if (IndicatorTypeEnum.ATOMIC_INDICATORS.getName().equals(targetinfoHistoryPO.getIndicatorType())) {
//            LambdaQueryWrapper<BusinessTargetinfoPO> businessTargetinfoWrapper = new LambdaQueryWrapper<>();
//            businessTargetinfoWrapper.eq(BusinessTargetinfoPO::getParentBusinessId, model.getId());
//            List<BusinessTargetinfoPO> businessTargetinfoPOList = businessTargetinfoMapper.selectList(businessTargetinfoWrapper);
//
//            Map<String, ChildBusinessTreeDTO> childBusinessMap = new HashMap<>();
//            if (CollectionUtils.isNotEmpty(businessTargetinfoPOList)) {
//                childBusinessMap = businessTargetinfoPOList.stream().map(t -> {
//                    ChildBusinessTreeDTO treeDTO = new ChildBusinessTreeDTO();
//                    treeDTO.setId(String.valueOf(t.getId()));
//                    treeDTO.setPid(t.getPid());
//                    treeDTO.setType(2);
//                    treeDTO.setName(t.getIndicatorName());
//                    return treeDTO;
//                }).collect(Collectors.toMap(ChildBusinessTreeDTO::getId, t -> t));
//            }
//
//            List<BusinessCategoryPO> data = businessCategoryMapper.selectList(new QueryWrapper<>());
//            List<ChildBusinessTreeDTO> allBusinessTree = data.stream().map(e -> {
//                ChildBusinessTreeDTO treeDTO = new ChildBusinessTreeDTO();
//                treeDTO.setId(String.valueOf(e.getId()));
//                treeDTO.setPid(String.valueOf(e.getPid()));
//                treeDTO.setType(1);
//                treeDTO.setName(e.getName());
//                return treeDTO;
//            }).collect(Collectors.toList());
//
//
//            List<ChildBusinessTreeDTO> parentTree = new ArrayList<>();
//            for (Map.Entry<String, ChildBusinessTreeDTO> entry : childBusinessMap.entrySet()) {
//                ChildBusinessTreeDTO child = entry.getValue();
//                parentTree.add(findParentById(allBusinessTree, child.getPid(), child));
//            }
//            ObjectMapper objectMapper = new ObjectMapper();
//            try {
//                if (CollectionUtils.isNotEmpty(parentTree)) {
//                    targetinfoHistoryPO.setDerivedMetric(objectMapper.writeValueAsString(parentTree));
//                }
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//
//        } else if (IndicatorTypeEnum.DERIVED_INDICATORS.getName().equals(targetinfoHistoryPO.getIndicatorType())) {
//            List<BusinessCategoryPO> data = businessCategoryMapper.selectList(new QueryWrapper<>());
//            if (model.parentBusinessId != null) {
//                LambdaQueryWrapper<BusinessTargetinfoPO> queryWrapper = new LambdaQueryWrapper<>();
//                queryWrapper.eq(BusinessTargetinfoPO::getId, model.parentBusinessId);
//                BusinessTargetinfoPO businessTargetinfoPOS = businessTargetinfoMapper.selectOne(queryWrapper);
//                if (businessTargetinfoPOS != null){
//                    targetinfoHistoryPO.setParentBusinessId(businessTargetinfoPOS.parentBusinessId);
//                    String hierarchyPath = getHierarchyPath(data, Integer.parseInt(businessTargetinfoPOS.getPid()));
//                    targetinfoHistoryPO.setParentBusinessName(hierarchyPath+"/"+businessTargetinfoPOS.getIndicatorName());
//                }
//            }
//        }
//
//        boolean save = businessTargetinfoHistoryService.save(targetinfoHistoryPO);
//        if (!save) {
//            throw new FkException(ResultEnum.ERROR, "保存历史数据失败");
//        }
        model.setIndicatorName(item.indicatorName);
        model.setIndicatorDescription(item.indicatorDescription);
        model.setPid(item.pid);
        model.setAttributesNumber(item.attributesNumber);
        model.setDataGranularity(item.dataGranularity);
        model.setFilteringCriteria(item.filteringCriteria);
        model.setIndicatorCode(item.indicatorCode);
        model.setIndicatorformula(item.indicatorformula);
        model.setIndicatorLevel(item.indicatorLevel);
        model.setIndicatorStatus(item.indicatorStatus);
        model.setLargeScreenLink(item.largeScreenLink);
        model.setOperationalAttributes(item.operationalAttributes);
        model.setOrderChannel(item.orderChannel);
        model.setSourceDataTable(item.sourceDataTable);
        model.setSourceIndicators(item.sourceIndicators);
        model.setStatisticalCycle(item.statisticalCycle);
        model.setResponsibleDept(item.responsibleDept);
        model.setSourceSystem(item.sourceSystem);
        model.setUnitMeasurement(item.unitMeasurement);
        model.setIndicatorType(item.indicatorType);
        model.setName(item.name);
        model.setSqlScript(item.sqlScript);
        if (IndicatorTypeEnum.DERIVED_INDICATORS.getName().equals(item.indicatorType)) {
            model.setParentBusinessId(item.parentBusinessId);
        } else {
            model.setParentBusinessId(null);
        }
        if (businessTargetinfoMapper.updateById(model) <= 0) {
            throw new FkException(ResultEnum.ERROR, "修改指标明细数据失败");
        }

        //处理指标粒度
        List<BusinessExtendedfieldsDTO> dimensionData = item.getDimensionData();

        //找出待删除的数据
        LambdaQueryWrapper<BusinessExtendedfieldsPO> extendedfieldsWrapper = new LambdaQueryWrapper<>();
        extendedfieldsWrapper.eq(BusinessExtendedfieldsPO::getIndexid, model.id);
        List<BusinessExtendedfieldsPO> businessExtendedfieldsPOS = businessExtendedfieldsMapper.selectList(extendedfieldsWrapper);

        if (!CollectionUtils.isEmpty(businessExtendedfieldsPOS)) {
            List<Integer> dimensionDataIds = dimensionData.stream().map(BusinessExtendedfieldsDTO::getId).collect(Collectors.toList());
            List<BusinessExtendedfieldsPO> delExtendedfields = businessExtendedfieldsPOS.stream().filter(i -> !dimensionDataIds.contains(i.getId())).collect(Collectors.toList());
            List<Integer> delIds = delExtendedfields.stream().map(BusinessExtendedfieldsPO::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(delIds)) {
                businessExtendedfieldsMapper.deleteBatchIds(delIds);
            }
        }
        List<BusinessExtendedfieldsPO> extendedfieldsPOS = dimensionData.stream().map(i -> {
            BusinessExtendedfieldsPO model1 = new BusinessExtendedfieldsPO();
            if (i.getId() != null) {
                model1.setId(i.getId());
            }
//            model1.setAttribute(i.getAttribute());
            model1.setAttributeid(i.getAttributeid());
//            model1.setDimdomain(i.getDimdomain());
            model1.setDimdomainid(i.getDimdomainid());
//            model1.setDimdomaintype(i.getDimdomaintype());
//            model1.setDimtable(i.getDimtable());
            model1.setDimtableid(i.getDimtableid());
            model1.setIndexid(String.valueOf(item.getId()));
//            model1.setAttributeEnName(i.getAttributeEnName());
            if (i.getCreatedUser() != null) {
                model1.setCreatedUser(i.getCreatedUser());
            } else {
                model1.setCreatedUser(userHelper.getLoginUserInfo().username);
            }

            if (i.getCreatedTime() != null) {
//                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
//                LocalDateTime localDateTime = LocalDateTime.parse(i.getCreatedTime().toString(), inputFormatter);
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                // 格式化为指定格式的字符串
                String outputDateTime = i.getCreatedTime().format(outputFormatter);
                model1.setCreatedTime(outputDateTime);
            } else {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                model1.setCreatedTime(format.format(new Date()));
            }
            model1.setDelFlag(1);
            return model1;
        }).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(extendedfieldsPOS)) {
            //找出待添加数据
            List<BusinessExtendedfieldsPO> addExtendedfields = extendedfieldsPOS.stream().filter(i -> i.getId() == 0).collect(Collectors.toList());
            //找出待修改数据
            List<BusinessExtendedfieldsPO> updateExtendedfields = extendedfieldsPOS.stream().filter(i -> i.getId() != 0).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(addExtendedfields)) {
                businessExtendedfieldsService.saveBatch(addExtendedfields);
            }
            if (CollectionUtils.isNotEmpty(updateExtendedfields)) {
                businessExtendedfieldsService.updateBatchById(updateExtendedfields);
            }
        }

//        String finalHistoryId = historyId;
//        //保存历史数据
//        if (CollectionUtils.isNotEmpty(businessExtendedfieldsPOS)){
//            List<BusinessExtendedfieldsHistoryPO> extendedfieldsHistorys = businessExtendedfieldsPOS.stream().map(i -> {
//                BusinessExtendedfieldsHistoryPO businessExtendedfieldsHistoryPO = BeanHelper.copyProperties(i, BusinessExtendedfieldsHistoryPO.class);
//                businessExtendedfieldsHistoryPO.setHistoryId(finalHistoryId);
//                businessExtendedfieldsHistoryPO.setCreatedUser(i.createdUser);
//                businessExtendedfieldsHistoryPO.setCreatedTime(i.createdTime);
//                businessExtendedfieldsHistoryPO.setAttributeEnName(i.attributeEnName);
//                businessExtendedfieldsHistoryPO.setDelFlag(1);
//                return businessExtendedfieldsHistoryPO;
//            }).collect(Collectors.toList());
//            boolean b = businessExtendedfieldsHistoryService.saveBatch(extendedfieldsHistorys);
//            if (!b){
//                throw new FkException(ResultEnum.ERROR, "保存历史数据失败");
//            }
//        }
        //处理指标所属
        List<FacttreeListDTO> facttreeListData = item.getFacttreeListData();
        List<FactTreePOs> facttreeListPOs = facttreeListData.stream().map(i -> {
            FactTreePOs model1 = new FactTreePOs();
            if (i.getId() != null) {
                model1.setId(i.getId());
            }
            model1.setPid(item.id + "");
            model1.setBusinessNameId(i.getBusinessNameId());
//            model1.setBusinessName(i.getBusinessName());
            model1.setFactTabNameId(i.getFactTabNameId());
//            model1.setFactTabName(i.getFactTabName());
            model1.setFactFieldEnNameId(i.getFactFieldEnNameId());
//            model1.setFactFieldEnName(i.getFactFieldEnName());
//            model1.setFactFieldCnName(i.getFactFieldCnName());
            if (i.getCreateUser() == null) {
                model1.setCreateUser(userHelper.getLoginUserInfo().id.toString());
            }
            if (i.getCreateTime() == null) {
                model1.setCreateTime(LocalDateTime.now());
            }
            model1.setDelFlag(1);
            return model1;
        }).collect(Collectors.toList());
        //找出待删除的数据
        LambdaQueryWrapper<FactTreePOs> factTreeWrapper = new LambdaQueryWrapper<>();
        factTreeWrapper.eq(FactTreePOs::getPid, model.id);
        List<FactTreePOs> factTreePOS = factTreeService.list(factTreeWrapper);
        if (!CollectionUtils.isEmpty(factTreePOS)) {
            List<Integer> factTreeIds = facttreeListPOs.stream().map(i -> (int) i.getId()).collect(Collectors.toList());
            List<FactTreePOs> delFactTrees = factTreePOS.stream().filter(i -> !factTreeIds.contains((int) i.getId())).collect(Collectors.toList());
            List<Integer> delIds = delFactTrees.stream().map(i -> (int) i.getId()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(delIds)) {
                factTreeListMapper.deleteBatchIds(delIds);
            }
        }

        if (CollectionUtils.isNotEmpty(facttreeListPOs)) {
            //找出待添加数据
            List<FactTreePOs> addFactTrees = facttreeListPOs.stream().filter(i -> i.getId() == 0).collect(Collectors.toList());
            //找出待修改数据
            List<FactTreePOs> updateFactTrees = facttreeListPOs.stream().filter(i -> i.getId() != 0).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(addFactTrees)) {
                factTreeService.saveBatch(addFactTrees);
            }
            if (CollectionUtils.isNotEmpty(updateFactTrees)) {
                factTreeService.updateBatchById(updateFactTrees);
            }
        }


        //保存历史数据
//        if (CollectionUtils.isNotEmpty(factTreePOS)){
//            String finalHistoryId1 = historyId;
//            List<FacttreelistHistoryPO> factHistoryTreePOs = factTreePOS.stream().map(i -> {
//                FacttreelistHistoryPO facttreelistHistoryPO = new FacttreelistHistoryPO();
//                facttreelistHistoryPO.setHistoryId(finalHistoryId1);
//                facttreelistHistoryPO.setPid(item.id+"");
//                facttreelistHistoryPO.setFactFieldEnNameId(i.factFieldEnNameId);
//                facttreelistHistoryPO.setFactFieldEnName(i.factFieldEnName);
//                facttreelistHistoryPO.setFactFieldCnName(i.factFieldCnName);
//                facttreelistHistoryPO.setCreateUser(i.createUser);
//                facttreelistHistoryPO.setCreateTime(i.createTime);
//                facttreelistHistoryPO.setDelFlag(1);
//                return facttreelistHistoryPO;
//            }).collect(Collectors.toList());
//            boolean b = facttreelistHistoryService.saveBatch(factHistoryTreePOs);
//            if (!b){
//                throw new FkException(ResultEnum.ERROR, "保存历史数据失败");
//            }
//        }

        //处理指标粒度
        List<ApitreeListDTO> apitreeListDTOS = item.getApitreeListData();

        //找出待删除的数据
        LambdaQueryWrapper<ApitreePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApitreePO::getPid, String.valueOf(model.id));
        List<ApitreePO> apitreePOS = apitreelistMapper.selectList(queryWrapper);

        if (!CollectionUtils.isEmpty(apitreePOS)) {
            List<Integer> apitreeListIds = apitreeListDTOS.stream().map(ApitreeListDTO::getId).collect(Collectors.toList());
            List<ApitreePO> delapitreeIds = apitreePOS.stream().filter(i -> !apitreeListIds.contains(i.getId())).collect(Collectors.toList());
            List<Integer> delIds = delapitreeIds.stream().map(i->(int)i.getId()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(delIds)) {
                apitreelistMapper.deleteBatchIds(delIds);
            }
        }
        List<ApitreePO> apitreePOList = apitreeListDTOS.stream().map(i -> {
            ApitreePO model1 = new ApitreePO();
            if (i.getId() != null) {
                model1.setId(i.getId());
            }
//            model1.setAttribute(i.getAttribute());
            model1.setAttributeId(i.getAttributeId());
//            model1.setDimdomain(i.getDimdomain());
            model1.setAppId(i.getAppId());
//            model1.setDimdomaintype(i.getDimdomaintype());
//            model1.setDimtable(i.getDimtable());
            model1.setApiId(i.getApiId());
            model1.setPid(String.valueOf(item.getId()));
//            model1.setAttributeEnName(i.getAttributeEnName());
            if (i.getCreateUser() == null) {
                model1.setCreateUser(userHelper.getLoginUserInfo().id.toString());
            }
            if (i.getCreateTime() == null) {
                model1.setCreateTime(LocalDateTime.now());
            }
            model1.setDelFlag(1);
            return model1;
        }).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(apitreePOList)) {
            //找出待添加数据
            List<ApitreePO> addApitreePOS = apitreePOList.stream().filter(i -> i.getId() == 0).collect(Collectors.toList());
            //找出待修改数据
            List<ApitreePO> updateApitreePOS = apitreePOList.stream().filter(i -> i.getId() != 0).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(addApitreePOS)) {
                apitreelistService.saveBatch(addApitreePOS);
            }
            if (CollectionUtils.isNotEmpty(updateApitreePOS)) {
                apitreelistService.updateBatchById(updateApitreePOS);
            }
        }
        return ResultEnum.SUCCESS;
    }

    @TraceType(type = TraceTypeEnum.CHARTVISUAL_QUERY)
    @Override
    public void downLoad(String type, List<String> tableIds, HttpServletResponse response) {
        // 查询数据
        List<Map<String, Object>> list = null;
        if (DownLoadTypeEnum.ALL.getValue().equals(type)) {
            list = businessTargetinfoMapper.selectClassification2();
        }else if (DownLoadTypeEnum.SECTION.getValue().equals(type)){
            list = businessTargetinfoMapper.selectClassification1(tableIds);
        }
        if (CollectionUtils.isEmpty(list)){
            throw new FkException(ResultEnum.ERROR, "没有数据");
        }
        List<Integer> pid = list.stream().map(i -> Integer.valueOf(i.get("pid").toString())).collect(Collectors.toList());
        List<Map<String, Object>> menuTreeNames = businessTargetinfoMapper.getMenuTreeNames();
        List<Map<String, Object>> level = menuTreeNames.stream()
                .filter(v -> pid.contains(Integer.valueOf(v.get("id").toString()))).collect(Collectors.toList());
        int maxLevel = level.stream()
                .map(map -> Integer.valueOf(map.get("level").toString()))
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        Map<Integer, List<Map<String, Object>>> menus = menuTreeNames.stream().collect(Collectors.groupingBy(i -> Integer.valueOf(i.get("id").toString())));
        uploadExcelAboutUser(response, "TargetinfoDetailData.xlsx", list,maxLevel,menus);
    }

    public static List<BusinessCategoryPO> getAllChildrenCategories(List<BusinessCategoryPO> allCategories, Integer pid) {
        List<BusinessCategoryPO> children = new ArrayList<>();
        for (BusinessCategoryPO category : allCategories) {
            if (Objects.equals(category.getPid(), pid)) {
                children.add(category);
                children.addAll(getAllChildrenCategories(allCategories, (int) category.getId()));
            }
        }
        return children;
    }


    /**
     * 用户信息导出类
     *
     * @param response 响应
     * @param fileName 文件名
     * @param dataList 导出的数据
     */
    public static void uploadExcelAboutUser(HttpServletResponse response, String fileName,
                                            List<Map<String, Object>> dataList,
                                            int maxLevel,
                                            Map<Integer, List<Map<String, Object>>> menus) {
        //声明输出流
        OutputStream os = null;
        try {
            List<String> heardrs = Arrays.stream(parentTargetinfoHeaders1).collect(Collectors.toList());
            for (int i = 0; i < maxLevel; i++) {
                heardrs.add(i,i+1+"级分类");
            }
            //设置响应头
            setResponseHeader(response, fileName);
            //获取输出流
            os = response.getOutputStream();
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("sheet1");
            XSSFRow row1 = sheet.createRow(0);
            //添加表头
            for (int i = 0; i < heardrs.size(); i++) {
                row1.createCell(i).setCellValue(heardrs.get(i));
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<LinkedHashMap<String, Object>> data = new ArrayList<>();
            for (Map<String, Object> aa : dataList) {
                for (int i = 0; i < maxLevel; i++) {
                    aa.put("menu"+(i+1),"");
                }
//                if (!aa.containsKey("namepid")) {
//                    aa.put("namepid", "");
//                }
//                if (!aa.containsKey("name")) {
//                    aa.put("name", "");
//                }
                if (!aa.containsKey("large_screen_link")) {
                    aa.put("large_screen_link", "");
                }
                if (!aa.containsKey("responsible_dept")) {
                    aa.put("responsible_dept", "");
                }
                if (!aa.containsKey("indicator_code")) {
                    aa.put("indicator_code", "");
                }
                if (!aa.containsKey("statistical_cycle")) {
                    aa.put("statistical_cycle", "");
                }
                if (!aa.containsKey("source_system")) {
                    aa.put("source_system", "");
                }
                if (!aa.containsKey("indicator_name")) {
                    aa.put("indicator_name", "");
                }
                if (!aa.containsKey("unit_measurement")) {
                    aa.put("unit_measurement", "");
                }
                if (!aa.containsKey("source_indicators")) {
                    aa.put("source_indicators", "");
                }
                if (!aa.containsKey("indicator_type")) {
                    aa.put("indicator_type", "");
                }
                if (!aa.containsKey("indicator_status")) {
                    aa.put("indicator_status", "");
                }
                if (!aa.containsKey("indicator_level")) {
                    aa.put("indicator_level", "");
                }
                if (!aa.containsKey("source_data_table")) {
                    aa.put("source_data_table", "");
                }
                if (!aa.containsKey("indicatorformula")) {
                    aa.put("indicatorformula", "");
                }
                if (!aa.containsKey("sql_script")) {
                    aa.put("sql_script", "");
                }
                if (!aa.containsKey("filtering_criteria")) {
                    aa.put("filtering_criteria", "");
                }
                if (!aa.containsKey("indicator_description")) {
                    aa.put("indicator_description", "");
                }
                if (!aa.containsKey("data_granularity")) {
                    aa.put("data_granularity", "");
                }
            }
            for (Map<String, Object> stringObjectMap : dataList) {
                LinkedHashMap<String, Object> datamap = new LinkedHashMap<>();

                Integer pid = Integer.valueOf(stringObjectMap.get("pid").toString());
                List<Map<String, Object>> maps = menus.get(pid);
                if (maps != null){
                    Map<String, Object> map = maps.get(0);
                    String menuName = map.get("full_path").toString();
                    String[] split = menuName.split("->");
                    for (int i = 0; i < maxLevel; i++) {
                        if(i<split.length){
                            datamap.put("menu"+(i+1), split[i]);
                        }else {
                            datamap.put("menu"+(i+1), null);
                        }
                    }
                }else {
                    for (int i = 0; i < maxLevel; i++) {
                        datamap.put("menu"+(i+1), null);
                    }
                }
//                if (!StringUtils.isEmpty(stringObjectMap.get("namepid"))) {
//                    datamap.put("namepid", stringObjectMap.get("namepid"));
//                } else {
//                    datamap.put("namepid", null);
//                }
//
//                if (!StringUtils.isEmpty(stringObjectMap.get("name"))) {
//                    datamap.put("name", stringObjectMap.get("name"));
//                } else {
//                    datamap.put("name", null);
//                }
                if (!StringUtils.isEmpty(stringObjectMap.get("responsible_dept"))) {
                    datamap.put("responsible_dept", stringObjectMap.get("responsible_dept"));
                } else {
                    datamap.put("responsible_dept", null);
                }
                if (!StringUtils.isEmpty(stringObjectMap.get("indicator_code"))) {
                    datamap.put("indicator_code", stringObjectMap.get("indicator_code"));
                } else {
                    datamap.put("indicator_code", null);
                }
                if (!StringUtils.isEmpty(stringObjectMap.get("indicator_type"))) {
                    datamap.put("indicator_type", stringObjectMap.get("indicator_type"));
                } else {
                    datamap.put("indicator_type", null);
                }
                if (!StringUtils.isEmpty(stringObjectMap.get("indicator_name"))) {
                    datamap.put("indicator_name", stringObjectMap.get("indicator_name"));
                } else {
                    datamap.put("indicator_name", null);
                }
                if (!StringUtils.isEmpty(stringObjectMap.get("indicator_description"))) {
                    datamap.put("indicator_description", stringObjectMap.get("indicator_description"));
                } else {
                    datamap.put("indicator_description", null);
                }
                if (!StringUtils.isEmpty(stringObjectMap.get("indicator_level"))) {
                    datamap.put("indicator_level", stringObjectMap.get("indicator_level"));
                } else {
                    datamap.put("indicator_level", null);
                }
                if (!StringUtils.isEmpty(stringObjectMap.get("unit_measurement"))) {
                    datamap.put("unit_measurement", stringObjectMap.get("unit_measurement"));
                } else {
                    datamap.put("unit_measurement", null);
                }
                if (!StringUtils.isEmpty(stringObjectMap.get("statistical_cycle"))) {
                    datamap.put("statistical_cycle", stringObjectMap.get("statistical_cycle"));
                } else {
                    datamap.put("statistical_cycle", null);
                }
                String indicatorformula = (String) stringObjectMap.get("indicatorformula");
                if (!StringUtils.isEmpty(indicatorformula)) {
                    indicatorformula = indicatorformula.replaceAll("<span class=\"tribute-selected-item\">([\\s\\S]*?)<\\/span>", "$1")
                            .replace("&nbsp;", " ");
                    datamap.put("indicatorformula", indicatorformula);
                } else {
                    datamap.put("indicatorformula", null);
                }
                if (!StringUtils.isEmpty(stringObjectMap.get("sql_script"))) {
                    datamap.put("sql_script", stringObjectMap.get("sql_script"));
                } else {
                    datamap.put("sql_script", null);
                }
                if (!StringUtils.isEmpty(stringObjectMap.get("source_indicators"))) {
                    datamap.put("source_indicators", stringObjectMap.get("source_indicators"));
                } else {
                    datamap.put("source_indicators", null);
                }
                if (!StringUtils.isEmpty(stringObjectMap.get("filtering_criteria"))) {
                    datamap.put("filtering_criteria", stringObjectMap.get("filtering_criteria"));
                } else {
                    datamap.put("filtering_criteria", null);
                }
                if (!StringUtils.isEmpty(stringObjectMap.get("source_system"))) {
                    datamap.put("source_system", stringObjectMap.get("source_system"));
                } else {
                    datamap.put("source_system", null);
                }
                if (!StringUtils.isEmpty(stringObjectMap.get("source_data_table"))) {
                    datamap.put("source_data_table", stringObjectMap.get("source_data_table"));
                } else {
                    datamap.put("source_data_table", null);
                }
                if (!StringUtils.isEmpty(stringObjectMap.get("indicator_status"))) {
                    datamap.put("indicator_status", stringObjectMap.get("indicator_status"));
                } else {
                    datamap.put("indicator_status", null);
                }
                if (!StringUtils.isEmpty(stringObjectMap.get("large_screen_link"))) {
                    datamap.put("large_screen_link", stringObjectMap.get("large_screen_link"));
                } else {
                    datamap.put("large_screen_link", null);
                }
                if (!StringUtils.isEmpty(stringObjectMap.get("data_granularity"))) {
                    datamap.put("data_granularity", stringObjectMap.get("data_granularity"));
                } else {
                    datamap.put("data_granularity", null);
                }
                data.add(datamap);
            }

            if (!CollectionUtils.isEmpty(data)) {
                for (int i = 0; i < data.size(); i++) {
                    XSSFRow row = sheet.createRow(i + 1);
                    Map<String, Object> jsonObject = data.get(i);
                    int index = 0;
                    for (Map.Entry<String, Object> stringObjectEntry : jsonObject.entrySet()) {
                        row.createCell(index).setCellValue(stringObjectEntry.getValue() == null ? "" : stringObjectEntry.getValue().toString());
                        index++;
                    }
                }
            }
            //将整理好的excel数据写入流中
            workbook.write(os);

        } catch (IOException e) {
            log.error("Excel导出失败，ex", e);
        } finally {
            try {
                // 关闭输出流
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                log.error("Excel导出 流关闭失败，ex", e);
            }
        }
    }


    /**
     * 设置浏览器下载响应头
     */
    public static void setResponseHeader(HttpServletResponse response, String fileName) {
        try {
            fileName = new String(fileName.getBytes(), "GBK");
//            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes(), "ISO8859-1"));
            response.addHeader("Pargam", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
        } catch (Exception ex) {
            log.error("设置响应对象失败，ex", ex);
        }
    }


    @Override
    public List<BusinessTargetinfoPO> getDimensionList(String name) {
        String sql = "select * from tb_business_targetinfo where indicator_name like '%" + name + "%' and del_flag = 1 ";
        List<BusinessTargetinfoPO> list = businessTargetinfoMapper.selectDimensionList(sql);
        return list;
    }

    @Override
    public JSONObject getTargetinfoHistory(String historyId) {
        BusinessTargetinfoHistoryPO targetinfoHistoryPO = businessTargetinfoHistoryService.selectClassification(historyId);
            List<BusinessExtendedfieldsHistoryPO> list1= businessExtendedfieldsHistoryService.selectHistoryId(historyId);
            List<FacttreelistHistoryPO> list2 = facttreelistHistoryService.selectHistoryId(historyId);
        List<ApitreelistHistoryPO> list3 = apitreelistHistoryService.selectHistoryId(historyId);
        List<Long> userIds = list2.stream()
                .filter(x -> org.apache.commons.lang.StringUtils.isNotEmpty(x.createUser))
                .map(x -> Long.valueOf(x.createUser))
                .distinct()
                .collect(Collectors.toList());

        ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userIds);
        if (userListByIds.code == ResultEnum.SUCCESS.getCode()
                && CollectionUtils.isNotEmpty(userListByIds.getData())) {
            list2.forEach(e -> {
                userListByIds.getData()
                        .stream()
                        .filter(user -> user.getId().toString().equals(e.createUser))
                        .findFirst()
                        .ifPresent(user -> e.createUser = user.userAccount);
            });
        }

        List<Long> apiUserIds = list3.stream()
                .filter(x -> org.apache.commons.lang.StringUtils.isNotEmpty(x.createUser))
                .map(x -> Long.valueOf(x.createUser))
                .distinct()
                .collect(Collectors.toList());

        ResultEntity<List<UserDTO>> apiUserListByIds = userClient.getUserListByIds(apiUserIds);
        if (apiUserListByIds.code == ResultEnum.SUCCESS.getCode()
                && CollectionUtils.isNotEmpty(apiUserListByIds.getData())) {
            list3.forEach(e -> {
                apiUserListByIds.getData()
                        .stream()
                        .filter(user -> user.getId().toString().equals(e.createUser))
                        .findFirst()
                        .ifPresent(user -> e.createUser = user.userAccount);
            });
        }
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("id", targetinfoHistoryPO.getId());
        jsonObject1.put("createTime", targetinfoHistoryPO.getCreateTime());
        jsonObject1.put("createUser", targetinfoHistoryPO.getCreateUser());
        jsonObject1.put("updateTime", targetinfoHistoryPO.getUpdateTime());
        jsonObject1.put("updateUser", targetinfoHistoryPO.getUpdateUser());
        jsonObject1.put("delFlag", targetinfoHistoryPO.getDelFlag());
        jsonObject1.put("pid", targetinfoHistoryPO.getPid());
        jsonObject1.put("responsibleDept", targetinfoHistoryPO.getResponsibleDept());
        jsonObject1.put("indicatorCode", targetinfoHistoryPO.getIndicatorCode());
        jsonObject1.put("indicatorName", targetinfoHistoryPO.getIndicatorName());
        jsonObject1.put("indicatorDescription", targetinfoHistoryPO.getIndicatorDescription());
        jsonObject1.put("largeScreenLink", targetinfoHistoryPO.getLargeScreenLink());
        jsonObject1.put("indicatorLevel", targetinfoHistoryPO.getIndicatorLevel());
        jsonObject1.put("unitMeasurement", targetinfoHistoryPO.getUnitMeasurement());
        jsonObject1.put("statisticalCycle", targetinfoHistoryPO.getStatisticalCycle());
        jsonObject1.put("indicatorformula", targetinfoHistoryPO.getIndicatorformula());
        jsonObject1.put("indicatorStatus", targetinfoHistoryPO.getIndicatorStatus());
        jsonObject1.put("filteringCriteria", targetinfoHistoryPO.getFilteringCriteria());
        jsonObject1.put("dataGranularity", targetinfoHistoryPO.getDataGranularity());
        jsonObject1.put("operationalAttributes", targetinfoHistoryPO.getOperationalAttributes());
        jsonObject1.put("sourceSystem", targetinfoHistoryPO.getSourceSystem());
        jsonObject1.put("sourceDataTable", targetinfoHistoryPO.getSourceDataTable());
        jsonObject1.put("sourceIndicators", targetinfoHistoryPO.getSourceIndicators());
        jsonObject1.put("orderChannel", targetinfoHistoryPO.getOrderChannel());
        jsonObject1.put("indicatorType", targetinfoHistoryPO.getIndicatorType());
        jsonObject1.put("name", targetinfoHistoryPO.getName());
        jsonObject1.put("sqlScript", targetinfoHistoryPO.getSqlScript());
            jsonObject1.put("dimensionData",list1);
            List<FacttreelistHistoryDTO> facttreeListData = list2.stream().map(FactTreeHistoryMap.INSTANCES::poToDto).collect(Collectors.toList());
            jsonObject1.put("facttreeListData",facttreeListData);

        List<ApitreelistHistoryDTO> apitreeListData = list3.stream().map(ApiTreeHistoryMap.INSTANCES::poToDto).collect(Collectors.toList());
        jsonObject1.put("apitreeListData",apitreeListData);
        Integer parentBusinessId = targetinfoHistoryPO.getParentBusinessId();


        if (IndicatorTypeEnum.DERIVED_INDICATORS.getName().equals(targetinfoHistoryPO.getIndicatorType())) {
            jsonObject1.put("parentBusinessId", parentBusinessId);
            jsonObject1.put("parentBusinessName", targetinfoHistoryPO.getParentBusinessName());
        } else if (IndicatorTypeEnum.ATOMIC_INDICATORS.getName().equals(targetinfoHistoryPO.getIndicatorType())) {
            String derivedMetric = targetinfoHistoryPO.getDerivedMetric();
            if (derivedMetric != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                List<ChildBusinessTreeDTO> childBusinessTreeDTOList;
                try {
                    childBusinessTreeDTOList = objectMapper.readValue(derivedMetric, new TypeReference<List<ChildBusinessTreeDTO>>() {});
                    if (CollectionUtils.isNotEmpty(childBusinessTreeDTOList)){
                        childBusinessTreeDTOList.sort(Comparator.comparing(ChildBusinessTreeDTO::getName, Collator.getInstance(Locale.CHINA)));
                    }
                    jsonObject1.put("derivedMetric", childBusinessTreeDTOList);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        return jsonObject1;
    }

    /**
     * 数仓建模获取所有业务指标 只获取id 名称
     *
     * @return
     */
    @Override
    public List<BusinessTargetinfoDTO> modelGetBusinessTargetInfoList() {
        LambdaQueryWrapper<BusinessTargetinfoPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(BusinessTargetinfoPO::getId, BusinessTargetinfoPO::getIndicatorName);
        List<BusinessTargetinfoPO> list = this.list();
        List<BusinessTargetinfoDTO> dtos = new ArrayList<>();
        for (BusinessTargetinfoPO po : list) {
            //只获取id 名称
            BusinessTargetinfoDTO businessTargetinfoDTO = new BusinessTargetinfoDTO();
            businessTargetinfoDTO.setId(po.getId());
            businessTargetinfoDTO.setIndicatorName(po.getIndicatorName());
            dtos.add(businessTargetinfoDTO);
        }
        return dtos;
    }

    /**
     * 获取数仓字段和指标所属表里所有关联关系 只获取字段id 和指标id
     *
     * @return
     */
    @Override
    public List<FacttreeListDTO> modelGetFactTreeList(Integer tblId) {
        LambdaQueryWrapper<FactTreePOs> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(FactTreePOs::getPid, FactTreePOs::getFactFieldEnNameId)
                .eq(FactTreePOs::getFactTabNameId, tblId);
        List<FactTreePOs> list = factTreeService.list();
        List<FacttreeListDTO> dtos = new ArrayList<>();
        for (FactTreePOs po : list) {
            FacttreeListDTO dto = new FacttreeListDTO();
            dto.setPid(po.getPid());
            dto.setFactFieldEnNameId(po.getFactFieldEnNameId());
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * 获取数仓字段和指标粒度表里所有关联关系 只获取字段id 和指标id
     *
     * @return
     */
    @Override
    public List<BusinessExtendedfieldsDTO> modelGetMetricMapList() {
        LambdaQueryWrapper<BusinessExtendedfieldsPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(BusinessExtendedfieldsPO::getIndexid, BusinessExtendedfieldsPO::getAttributeid);
        List<BusinessExtendedfieldsPO> list = businessExtendedfieldsService.list();
        List<BusinessExtendedfieldsDTO> dtos = new ArrayList<>();
        for (BusinessExtendedfieldsPO po : list) {
            BusinessExtendedfieldsDTO dto = new BusinessExtendedfieldsDTO();
            dto.setIndexid(po.getIndexid());
            dto.setAttributeid(po.getAttributeid());
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public List<BusinessTargetinfoMenuDTO> getBusinessMetaDataNameList(String key) {

        UserInfo userInfo = userHelper.getLoginUserInfo();
        ResultEntity<List<RoleInfoDTO>> rolebyUserId = userClient.getRolebyUserId(userInfo.getId().intValue());
        List<RoleInfoDTO> businessAssignment = new ArrayList<>();
        if (rolebyUserId.code == ResultEnum.SUCCESS.getCode()) {
            businessAssignment = rolebyUserId.data;
        } else {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        List<Integer> roleIds = businessAssignment.stream().map(i -> (int) i.getId()).collect(Collectors.toList());

        LambdaQueryWrapper<BusinessCategoryAssignmentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BusinessCategoryAssignmentPO::getRoleId, roleIds);
        List<BusinessCategoryAssignmentPO> list = businessCategoryAssignmentService.list(wrapper);
        List<Integer> menuId = list.stream().map(BusinessCategoryAssignmentPO::getCategoryId).distinct().collect(Collectors.toList());

        List<BusinessCategoryPO> allMenuList = businessCategoryMapper.selectList(new QueryWrapper<>());
        allMenuList = allMenuList.stream().map(i -> {
            if (i.getPid() == null) {
                i.setPid(0);
            }
            return i;
        }).collect(Collectors.toList());
        List<Integer> allChildIds = getAllChildIds(allMenuList, menuId);
        allChildIds.addAll(menuId);
        LambdaQueryWrapper<BusinessTargetinfoPO> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.in(BusinessTargetinfoPO::getPid, allChildIds);
        if (!StringUtils.isEmpty(key)) {
            wrapper1.and(i -> i
                .like(BusinessTargetinfoPO::getIndicatorName, key)
                .or()
                .like(BusinessTargetinfoPO::getIndicatorCode, key)
                .or()
                .like(BusinessTargetinfoPO::getLargeScreenLink, key)
                .or()
                .like(BusinessTargetinfoPO::getSourceSystem, key)
                .or()
                .like(BusinessTargetinfoPO::getIndicatorDescription, key)
            );
        }
        List<BusinessTargetinfoPO> businessTargetinfoPOList = businessTargetinfoMapper.selectList(wrapper1);
        List<BusinessTargetinfoPO> parentBusinessIdIds = businessTargetinfoPOList.stream().filter(i -> i.getParentBusinessId() != null && i.getParentBusinessId() != 0).collect(Collectors.toList());
        Map<String,BusinessTargetinfoPO> parentBusinessMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(parentBusinessIdIds)){
            List<Integer> parentIds = parentBusinessIdIds.stream().map(BusinessTargetinfoPO::getParentBusinessId).collect(Collectors.toList());
            List<BusinessTargetinfoPO> targetinfoPOList = businessTargetinfoMapper.selectBatchIds(parentIds);
            parentBusinessMap = targetinfoPOList.stream().collect(Collectors.toMap(i->String.valueOf(i.getId()), i->i));
        }
        Map<String, BusinessTargetinfoPO> finalParentBusinessMap = parentBusinessMap;
        List<BusinessCategoryPO> finalAllMenuList = allMenuList;
        List<BusinessTargetinfoMenuDTO> result = businessTargetinfoPOList.stream().map(i -> {
            BusinessTargetinfoMenuDTO businessTargetinfoMenuDTO = new BusinessTargetinfoMenuDTO();
            businessTargetinfoMenuDTO.setId((int) i.getId());
            businessTargetinfoMenuDTO.setName(i.getIndicatorName());
            businessTargetinfoMenuDTO.setType(i.getIndicatorType());
            Map<String, String> categoryNameMaps = finalAllMenuList.stream().collect(Collectors.toMap(v -> String.valueOf(v.getId()), BusinessCategoryPO::getName));
            businessTargetinfoMenuDTO.setCategoryId(i.getName());
            businessTargetinfoMenuDTO.setCategoryName(categoryNameMaps.get(i.getName()));
            businessTargetinfoMenuDTO.setParentBusinessId(i.getParentBusinessId());
            BusinessTargetinfoPO businessTargetinfoPO = finalParentBusinessMap.get(String.valueOf(i.getParentBusinessId()));
            if(businessTargetinfoPO != null){
                businessTargetinfoMenuDTO.setParentBusinessCategoryId(businessTargetinfoPO.getPid());
                businessTargetinfoMenuDTO.setParentBusinessName(businessTargetinfoPO.getIndicatorName());
            }
            businessTargetinfoMenuDTO.setIndicatorStatus(i.getIndicatorStatus());
            businessTargetinfoMenuDTO.setIndicatorCcode(i.getIndicatorCode());
            businessTargetinfoMenuDTO.setLargeScreenLink(i.getLargeScreenLink());
            businessTargetinfoMenuDTO.setSourceSystem(i.getSourceSystem());
            businessTargetinfoMenuDTO.setIndicatorDescription(i.getIndicatorDescription());
            businessTargetinfoMenuDTO.setCreateTime(i.getCreateTime());
            return businessTargetinfoMenuDTO;
        }).collect(Collectors.toList());

        result.sort(Comparator.comparing(BusinessTargetinfoMenuDTO::getType,
                (type1, type2) -> {
                    if (StringUtils.isEmpty(type1) && StringUtils.isEmpty(type2)) {
                        return 0;
                    } else if (StringUtils.isEmpty(type1)) {
                        return 1;
                    } else if (StringUtils.isEmpty(type2)) {
                        return -1;
                    } else {
                        Map<String, Integer> order = new HashMap<>();
                        order.put("原子指标", 1);
                        order.put("派生指标", 2);
                        order.put("衍生指标", 3);

                        return order.get(type1) - order.get(type2);
                    }
                }).thenComparing(BusinessTargetinfoMenuDTO::getName, Collator.getInstance(Locale.CHINA)));
        return result;
    }

    /**
     * 数据资产 - 资产目录 按指标标准分类
     *
     * @return
     */
    @Override
    public List<IndexForAssetCatalogDTO> getIndexForAssetCatalog() {
        List<IndexForAssetCatalogDTO> result = new ArrayList<>();

        //只查询指标目录根节点 pid为null
        List<BusinessCategoryPO> data = businessCategoryMapper.selectList(
                new LambdaQueryWrapper<BusinessCategoryPO>()
                        .select(BusinessCategoryPO::getId, BusinessCategoryPO::getName, BusinessCategoryPO::getPid)
                        .isNull(BusinessCategoryPO::getPid)
        );

        //查询所有指标目录
        List<BusinessCategoryPO> allData = businessCategoryMapper.selectList(
                new LambdaQueryWrapper<BusinessCategoryPO>()
                        .select(BusinessCategoryPO::getId, BusinessCategoryPO::getName, BusinessCategoryPO::getPid)
                        .isNotNull(BusinessCategoryPO::getPid)
        );

        //查询所有指标详情 只查询pid 只是为了计数
        List<String> indexes = businessTargetinfoMapper.selectList(
                new LambdaQueryWrapper<BusinessTargetinfoPO>()
                        .select(BusinessTargetinfoPO::getPid)
        ).stream().map(BusinessTargetinfoPO::getPid).collect(Collectors.toList());

        //查询授权表 和指标目录对比 只获取授权表中存在的目录（为了与页面保持一致）
        List<Long> collect = businessCategoryAssignmentService.list()
                .stream()
                .map(BusinessCategoryAssignmentPO::getCategoryId)
                .map(Integer::longValue)
                .distinct()
                .collect(Collectors.toList());

        //真正存在的指标根目录
        List<BusinessCategoryPO> existsIndexes = data.stream()
                .filter(businessCategoryPO -> collect.contains(businessCategoryPO.getId()))
                .collect(Collectors.toList());

        for (BusinessCategoryPO po : existsIndexes) {
            IndexForAssetCatalogDTO dto = new IndexForAssetCatalogDTO();
            dto.setIndexId(po.getId());
            dto.setIndexName(po.getName());

            //递归计算指标个数
            int count = RecursivelyFetchMetrics(po.getId(), allData, indexes);
            dto.setIndexCount(count);
            dto.setIndexdMetaCount(count);
            result.add(dto);
        }

        return result;
    }

    @Override
    public Integer getBusinessTargetinfoTotal() {
        return baseMapper.getBusinessTargetinfoTotal();
    }

    @Override
    public List<BusinessTargetinfoMenuDTO> pageFilter(CategoryQueryDTO query) {
        StringBuilder querySql = new StringBuilder();
        // 拼接原生筛选条件
        querySql.append(generateCondition.getCondition(query.dto));

        UserInfo userInfo = userHelper.getLoginUserInfo();
        ResultEntity<List<RoleInfoDTO>> rolebyUserId = userClient.getRolebyUserId(userInfo.getId().intValue());
        List<RoleInfoDTO> businessAssignment = new ArrayList<>();
        if (rolebyUserId.code == ResultEnum.SUCCESS.getCode()) {
            businessAssignment = rolebyUserId.data;
        } else {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        List<Integer> roleIds = businessAssignment.stream().map(i -> (int) i.getId()).collect(Collectors.toList());

        LambdaQueryWrapper<BusinessCategoryAssignmentPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BusinessCategoryAssignmentPO::getRoleId, roleIds);
        List<BusinessCategoryAssignmentPO> list = businessCategoryAssignmentService.list(wrapper);
        List<Integer> menuId = list.stream().map(BusinessCategoryAssignmentPO::getCategoryId).distinct().collect(Collectors.toList());

        List<BusinessCategoryPO> allMenuList = businessCategoryMapper.selectList(new QueryWrapper<>());
        allMenuList = allMenuList.stream().map(i -> {
            if (i.getPid() == null) {
                i.setPid(0);
            }
            return i;
        }).collect(Collectors.toList());
        List<Integer> allChildIds = getAllChildIds(allMenuList, menuId);
        allChildIds.addAll(menuId);

        List<BusinessTargetinfoMenuDTO> filter = baseMapper.filter(allChildIds,querySql.toString());
        return filter;
    }

    @Override
    public BusinessQueryDataParamDTO getBusinessQueryDataParam(Integer fieldId) {
        BusinessQueryDataParamDTO data = new BusinessQueryDataParamDTO();
        ResultEntity<BusinessQueryDataParamDTO> businessQueryDataParam = modelClient.getBusinessQueryDataParam(fieldId);
        if (businessQueryDataParam.code == ResultEnum.SUCCESS.getCode() && businessQueryDataParam.data != null){
            data = businessQueryDataParam.data;
        }
        return data;
    }

    private int RecursivelyFetchMetrics(long id, List<BusinessCategoryPO> allData, List<String> indexes) {
        int indexCount = 0;
        List<BusinessCategoryPO> collect = allData.stream().filter(i -> i.getPid() == id).collect(Collectors.toList());
        for (BusinessCategoryPO businessCategoryPO : collect) {
            //如果指标目录的id在指标详情里面存在 就计算个数 相加
            if (indexes.contains(businessCategoryPO.getId() + "")) {
                indexCount = indexCount + (int) indexes.stream().filter(i -> i.equals(businessCategoryPO.getId() + "")).count();
            } else {
                indexCount += RecursivelyFetchMetrics(businessCategoryPO.getId(), allData, indexes);
            }
        }
        return indexCount;
    }

    // 递归获取多个根节点的子节点的方法
//    public List<Integer> getAllChildIds(List<BusinessCategoryPO> allCategory, List<Integer> parentIds) {
//        List<Integer> childIds = new ArrayList<>();
//
//        Map<Integer, BusinessCategoryPO> allMap = allCategory.stream().collect(Collectors.toMap(BusinessCategoryPO::getPid, i -> i));
//
//        // 遍历所有分类
//        for (BusinessCategoryPO category : allCategory) {
//            // 如果当前分类的父节点 ID 包含在指定的父节点 ID 集合中，则将其添加到子节点集合中
//            if (parentIds.contains(category.pid)) {
//                childIds.add((int)category.getId());
//                // 递归调用，查找当前分类的子节点
//                childIds.addAll(getAllChildIds(allCategory, Collections.singletonList((int)category.getId())));
//            }
//        }
//        childIds.addAll(parentIds);
//        return childIds;
//    }

    // 将分类列表按照 pid 分组
    private Map<Integer, List<BusinessCategoryPO>> groupByPid(List<BusinessCategoryPO> allCategory) {
        return allCategory.stream()
                .collect(Collectors.groupingBy(category -> category.pid));
    }

    // 递归获取多个根节点的子节点的方法
    public List<Integer> getAllChildIds(List<BusinessCategoryPO> allCategory, List<Integer> parentIds) {
        Map<Integer, List<BusinessCategoryPO>> categoryMap = groupByPid(allCategory);
        List<Integer> childIds = new ArrayList<>();
        // 遍历父节点 ID 集合
        for (Integer parentId : parentIds) {
            // 如果当前父节点存在子节点，则递归添加子节点
            if (categoryMap.containsKey(parentId)) {
                List<BusinessCategoryPO> children = categoryMap.get(parentId);
                List<Integer> ids = children.stream().map(i -> (int) i.getId()).collect(Collectors.toList());
                // 递归调用，查找当前父节点的子节点
                List<Integer> childrenIds = getAllChildIds(allCategory, ids);
                childIds.addAll(ids);
                childIds.addAll(childrenIds);
            }
        }
        // 转换为 List 返回
        return childIds;
    }
}
