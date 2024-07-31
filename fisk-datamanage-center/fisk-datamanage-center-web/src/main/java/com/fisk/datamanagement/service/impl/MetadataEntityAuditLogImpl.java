package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.ObjectInfoUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.assetschangeanalysis.*;
import com.fisk.datamanagement.dto.metaauditlog.AuditAnalysisAllChangeTotalVO;
import com.fisk.datamanagement.dto.metaauditlog.AuditAnalysisDayChangeTotalVO;
import com.fisk.datamanagement.dto.metaauditlog.MetadataEntityAuditLogVO;
import com.fisk.datamanagement.dto.metadataentity.EntitiesClassificationDTO;
import com.fisk.datamanagement.entity.*;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.enums.MetadataAuditOperationTypeEnum;
import com.fisk.datamanagement.map.MetadataEntityAuditLogMap;
import com.fisk.datamanagement.mapper.MetadataEntityAuditLogMapper;
import com.fisk.datamanagement.mapper.MetadataEntityMapper;
import com.fisk.datamanagement.service.IMetadataEntityAuditLog;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author JinXingWang
 * @description 针对表【tb_metadata_entity_audit_log】的数据库操作Service实现
 * @createDate 2024-03-14 11:44:11
 */
@Service
public class MetadataEntityAuditLogImpl extends ServiceImpl<MetadataEntityAuditLogMapper, MetadataEntityAuditLogPO>
        implements IMetadataEntityAuditLog {

    @Resource
    MetadataAttributeImpl metadataAttribute;
    @Resource
    MetadataEntityAuditAttributeChangeImpl metadataEntityAuditAttributeChange;

    @Resource
    MetadataEntityAuditLogMapper mapper;

    @Resource
    private UserClient userClient;

    @Resource
    private LineageMapRelationImpl lineageMapRelationImpl;

    @Resource
    private MetadataEntityImpl metadataEntityImpl;

    @Resource
    private ClassificationImpl classificationImpl;

    @Resource
    private MetadataEntityMapper metadataEntityMapper;

    @Resource
    private MetadataEntityAuditLogMapper auditLogMapper;

    @Override
    public ResultEnum setMetadataAuditLog(Object object, Integer entityId, MetadataAuditOperationTypeEnum operationType, String rdbmsType, String createUser) {

        MetadataEntityAuditLogPO metadataEntityAuditLogPO = new MetadataEntityAuditLogPO();
        metadataEntityAuditLogPO.setEntityId(entityId);
        metadataEntityAuditLogPO.setOperationType(operationType);
        metadataEntityAuditLogPO.setCreateUser(createUser);

        if (operationType.equals(MetadataAuditOperationTypeEnum.EDIT)) {
            if (rdbmsType.equals(EntityTypeEnum.RDBMS_COLUMN.getName())) {
                List<MetadataEntityAuditAttributeChangePO> auditAttributeChangePOList = new ArrayList<>();
                String[] auditAttribute = {"dataType", "length"};
                List<MetadataAttributePO> afterMetadataAttributeList = getMetadataAttributeByObj(object);
                //修改元数据
                List<MetadataAttributePO> beforeMetadataAttributeList = metadataAttribute.getMetadataAttribute(entityId);
                //对比历史数据找出被修改的属性
                for (String attribute : auditAttribute) {
                    MetadataAttributePO afterMetadataAttributePO = afterMetadataAttributeList.stream().filter(e -> e.getName().equals(attribute)).findFirst().orElse(null);
                    MetadataAttributePO beforeMetadataAttributePO = beforeMetadataAttributeList.stream().filter(e -> e.getName().equals(attribute)).findFirst().orElse(null);
                    String afterAttributeValue = afterMetadataAttributePO.getValue() == null ? "" : afterMetadataAttributePO.getValue();
                    String beforeAttributeValue = beforeMetadataAttributePO.getValue() == null ? "" : beforeMetadataAttributePO.getValue();
                    if (!afterAttributeValue.equals(beforeAttributeValue)) {
                        MetadataEntityAuditAttributeChangePO auditAttributeChangePO = new MetadataEntityAuditAttributeChangePO();
                        auditAttributeChangePO.setAttribute(attribute);
                        auditAttributeChangePO.setBeforeValue(beforeAttributeValue);
                        auditAttributeChangePO.setAfterValue(afterAttributeValue);
                        auditAttributeChangePOList.add(auditAttributeChangePO);
                    }
                }
                //判断属性是否被修改，若被修改怎添加审计记录
                if ((long) auditAttributeChangePOList.size() > 0) {
                    this.save(metadataEntityAuditLogPO);
                    Integer auditId = (int) metadataEntityAuditLogPO.getId();
                    auditAttributeChangePOList.forEach(e -> e.setAuditId(auditId));
                    metadataEntityAuditAttributeChange.saveBatch(auditAttributeChangePOList);
                }
            } else {
                //this.save(metadataEntityAuditLogPO);
            }
        } else {
            //新增，删除元数据，只添加审计记录，不添加元数据属性详细变更记录
            this.save(metadataEntityAuditLogPO);
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public List<MetadataEntityAuditLogVO> getMetadataAuditLog(Integer entityId) {
        List<MetadataEntityAuditLogVO> metadataEntityAuditLogVOS = new ArrayList<>();

        QueryWrapper<MetadataEntityAuditLogPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataEntityAuditLogPO::getEntityId, entityId);
        List<MetadataEntityAuditLogPO> list = this.list(queryWrapper);

        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(e -> {
                MetadataEntityAuditLogVO metadataEntityAuditLogVO = MetadataEntityAuditLogMap.INSTANCES.poToVo(e);
                metadataEntityAuditLogVOS.add(metadataEntityAuditLogVO);
                metadataEntityAuditLogVO.attribute = metadataEntityAuditAttributeChange.getAttributeChange((int) e.getId());
            });
        }
        return metadataEntityAuditLogVOS;
    }

    @Override
    public AuditAnalysisAllChangeTotalVO analysisAllChangeTotal() {
        QueryWrapper<MetadataEntityAuditLogPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataEntityAuditLogPO::getOperationType, MetadataAuditOperationTypeEnum.ADD);
        int addCount = this.count(queryWrapper);

        QueryWrapper<MetadataEntityAuditLogPO> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.lambda().eq(MetadataEntityAuditLogPO::getOperationType, MetadataAuditOperationTypeEnum.EDIT);
        int editCount = this.count(queryWrapper2);

        QueryWrapper<MetadataEntityAuditLogPO> queryWrapper3 = new QueryWrapper<>();
        queryWrapper3.lambda().eq(MetadataEntityAuditLogPO::getOperationType, MetadataAuditOperationTypeEnum.DELETE);
        int deleteCount = this.count(queryWrapper3);

        AuditAnalysisAllChangeTotalVO auditAnalysisChangeTotalVO = new AuditAnalysisAllChangeTotalVO();
        auditAnalysisChangeTotalVO.setAdd(addCount);
        auditAnalysisChangeTotalVO.setEdit(editCount);
        auditAnalysisChangeTotalVO.setDelete(deleteCount);
        return auditAnalysisChangeTotalVO;
    }

    @Override
    public List<AuditAnalysisDayChangeTotalVO> analysisDayChangeTotal() {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        // 获取七天前的日期
        LocalDate sevenDaysAgo = currentDate.minusDays(7);
        LocalDateTime beginTime = LocalDateTime.of(sevenDaysAgo, LocalTime.of(00, 00, 00));
        LocalDateTime endTime = LocalDateTime.of(currentDate, LocalTime.of(23, 59, 59));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String beginTimeStr = beginTime.format(formatter);
        String endTimeStr = endTime.format(formatter);
        List<AuditAnalysisDayChangeTotalVO> dayTotal = mapper.getDayTotal(beginTimeStr, endTimeStr);
        return dayTotal;
    }

    private List<MetadataAttributePO> getMetadataAttributeByObj(Object object) {
        String[] fieldNames = ObjectInfoUtils.getFiledName(object);

        Map<String, Object> map = new HashMap<>();
        for (String item : fieldNames) {
            if (org.springframework.util.StringUtils.isEmpty(item)) {
                continue;
            }
            Object value = ObjectInfoUtils.getFieldValueByName(item, object);
            map.put(item, value);
        }

        List<MetadataAttributePO> dataList = new ArrayList<>();

        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            MetadataAttributePO po = new MetadataAttributePO();
            String key = iterator.next();
            po.name = key;
            po.value = map.get(key) == null ? "" : map.get(key).toString();
            po.groupType = 0;

            dataList.add(po);
        }
        return dataList;
    }

    /**
     * 获取元数据变更影响分析首页图表信息
     *
     * @param dto
     * @return
     */
    @Override
    public AssetsChangeAnalysisDTO getMetaChangesCharts(AssetsChangeAnalysisQueryDTO dto) {
        //参数校验
        if (StringUtils.isEmpty(dto.getStartTime())
                || StringUtils.isEmpty(dto.getEndTime())
                || dto.getEntityType() == null
        ) {
            throw new FkException(ResultEnum.PARAMTER_ERROR);
        }

        AssetsChangeAnalysisDTO assetsChangeAnalysisDTO = new AssetsChangeAnalysisDTO();

        //转换时间
        LocalDateTime startTime = getLocalDateTime(dto.getStartTime());
        LocalDateTime endTime = getLocalDateTime(dto.getEndTime());

        //获取类型
        EntityTypeEnum entityType = dto.getEntityType();

        long betweenDays = startTime.until(endTime, ChronoUnit.DAYS);

        //增长线
        List<LineCountDTO> addLine = new ArrayList<>();
        //删除线
        List<LineCountDTO> delLine = new ArrayList<>();
        //更新线
        List<LineCountDTO> updateLine = new ArrayList<>();
        //增长个数
        int addPercent = 0;
        //删除个数
        int delPercent = 0;
        //更新个数
        int updatePercent = 0;

        //表计数
        Integer tblCount = 0;
        //字段计数
        Integer fieldCount = 0;

        assetsChangeAnalysisDTO.setStartTime(startTime);
        assetsChangeAnalysisDTO.setEndTime(endTime);

        // 分类统计变更情况明细
        List<CategoryDetailChangesDTO> categoryDetailChanges = new ArrayList<>();
        // 目前只展示表和字段
        if (entityType == EntityTypeEnum.ALL) {
            for (EntityTypeEnum value : EntityTypeEnum.values()) {
                if (value == EntityTypeEnum.RDBMS_TABLE) {
                    CategoryDetailChangesDTO tbl = new CategoryDetailChangesDTO();
                    tbl.setType(EntityTypeEnum.RDBMS_TABLE);
                    tbl.setTypeName("表");
                    tbl.setAddCount(0);
                    tbl.setUpdateCount(0);
                    tbl.setDelCount(0);
                    categoryDetailChanges.add(tbl);
                } else if (value == EntityTypeEnum.RDBMS_COLUMN) {
                    CategoryDetailChangesDTO coolumn = new CategoryDetailChangesDTO();
                    coolumn.setType(EntityTypeEnum.RDBMS_COLUMN);
                    coolumn.setTypeName("字段");
                    coolumn.setAddCount(0);
                    coolumn.setUpdateCount(0);
                    coolumn.setDelCount(0);
                    categoryDetailChanges.add(coolumn);
                }
            }
        } else {
            CategoryDetailChangesDTO tbl = new CategoryDetailChangesDTO();
            tbl.setType(entityType);
            if (entityType == EntityTypeEnum.RDBMS_TABLE) {
                tbl.setTypeName("表");
            } else if (entityType == EntityTypeEnum.RDBMS_COLUMN) {
                tbl.setTypeName("字段");
            }
            tbl.setAddCount(0);
            tbl.setUpdateCount(0);
            tbl.setDelCount(0);
            categoryDetailChanges.add(tbl);
        }


        List<MetadataEntityAuditLogPOWithEntityType> list;
        List<MetadataEntityAuditLogPOWithEntityType> list1;
        //获取时间区间内的所有元数据变更日志 左连接元数据表获取元数据类型
        if (entityType.equals(EntityTypeEnum.ALL)) {
            list = auditLogMapper.getMetaChangesCharts(startTime, endTime);
        } else {
            list = auditLogMapper.getMetaChangesChartsByOpType(startTime, endTime, entityType.getValue());
        }

        //该集合用来计算表和字段占比 原因：不论选全部和还是选表或字段 那个表和字段占比图不要发生变化
        list1 = auditLogMapper.getMetaChangesCharts(startTime, endTime);

        for (int i = 0; i <= betweenDays; i++) {
            LineCountDTO lineCountDTO = new LineCountDTO();
            lineCountDTO.setDate(startTime.plusDays(i));
            lineCountDTO.setCount(0);

            LineCountDTO lineCountDTO1 = new LineCountDTO();
            lineCountDTO1.setDate(startTime.plusDays(i));
            lineCountDTO1.setCount(0);

            LineCountDTO lineCountDTO2 = new LineCountDTO();
            lineCountDTO2.setDate(startTime.plusDays(i));
            lineCountDTO2.setCount(0);


            addLine.add(lineCountDTO);
            delLine.add(lineCountDTO1);
            updateLine.add(lineCountDTO2);
        }

        for (MetadataEntityAuditLogPOWithEntityType po : list1) {
            //统计表/字段个数
            switch (po.getTypeId()) {
                //EntityTypeEnum
                case 3:
                    tblCount++;
                    break;
                case 6:
                    fieldCount++;
                    break;
                default:
                    break;
            }
        }

        for (MetadataEntityAuditLogPOWithEntityType po : list) {
            if (entityType == EntityTypeEnum.ALL) {
                //统计表/字段个数
                switch (po.getTypeId()) {
                    //EntityTypeEnum
                    case 3:
                        if (po.getOperationType() == MetadataAuditOperationTypeEnum.ADD) {
                            CategoryDetailChangesDTO tblDist = categoryDetailChanges.get(0);
                            tblDist.setAddCount(tblDist.getAddCount() + 1);
                        } else if (po.getOperationType() == MetadataAuditOperationTypeEnum.DELETE) {
                            CategoryDetailChangesDTO tblDist = categoryDetailChanges.get(0);
                            tblDist.setDelCount(tblDist.getDelCount() + 1);
                        } else if (po.getOperationType() == MetadataAuditOperationTypeEnum.EDIT) {
                            CategoryDetailChangesDTO tblDist = categoryDetailChanges.get(0);
                            tblDist.setUpdateCount(tblDist.getUpdateCount() + 1);
                        }
                        break;
                    case 6:
                        if (po.getOperationType() == MetadataAuditOperationTypeEnum.ADD) {
                            CategoryDetailChangesDTO tblDist = categoryDetailChanges.get(1);
                            tblDist.setAddCount(tblDist.getAddCount() + 1);
                        } else if (po.getOperationType() == MetadataAuditOperationTypeEnum.DELETE) {
                            CategoryDetailChangesDTO tblDist = categoryDetailChanges.get(1);
                            tblDist.setDelCount(tblDist.getDelCount() + 1);
                        } else if (po.getOperationType() == MetadataAuditOperationTypeEnum.EDIT) {
                            CategoryDetailChangesDTO tblDist = categoryDetailChanges.get(1);
                            tblDist.setUpdateCount(tblDist.getUpdateCount() + 1);
                        }
                        break;
                    default:
                        break;
                }
            } else {
                //统计表/字段个数
                switch (po.getTypeId()) {
                    //EntityTypeEnum
                    case 3:
                    case 6:
                        if (po.getOperationType() == MetadataAuditOperationTypeEnum.ADD) {
                            CategoryDetailChangesDTO tblDist = categoryDetailChanges.get(0);
                            tblDist.setAddCount(tblDist.getAddCount() + 1);
                        } else if (po.getOperationType() == MetadataAuditOperationTypeEnum.DELETE) {
                            CategoryDetailChangesDTO tblDist = categoryDetailChanges.get(0);
                            tblDist.setDelCount(tblDist.getDelCount() + 1);
                        } else if (po.getOperationType() == MetadataAuditOperationTypeEnum.EDIT) {
                            CategoryDetailChangesDTO tblDist = categoryDetailChanges.get(0);
                            tblDist.setUpdateCount(tblDist.getUpdateCount() + 1);
                        }
                        break;
                    default:
                        break;
                }
            }


            //计算新增线 删除线 更新线 以及时间区间内新增个数 删除个数 修改个数
            switch (po.getOperationType()) {
                case ADD:
                    for (LineCountDTO line : addLine) {
                        if (line.getDate().equals(po.getCreateTime().withHour(0).withMinute(0).withSecond(0))) {
                            line.setCount(line.getCount() + 1);
                        }
                    }
                    //时间区间内新增个数
                    addPercent += 1;
                    break;
                case DELETE:
                    for (LineCountDTO line : delLine) {
                        if (line.getDate().equals(po.getCreateTime().withHour(0).withMinute(0).withSecond(0))) {
                            line.setCount(line.getCount() + 1);
                        }
                    }
                    //时间区间内删除个数
                    delPercent += 1;
                    break;
                case EDIT:
                    for (LineCountDTO line : updateLine) {
                        if (line.getDate().equals(po.getCreateTime().withHour(0).withMinute(0).withSecond(0))) {
                            line.setCount(line.getCount() + 1);
                        }
                    }
                    //时间区间内修改个数
                    updatePercent += 1;
                    break;
                default:
                    break;
            }
        }

        //根据查询变更类型的不同 返回值也不同 前端依据值渲染图表

        assetsChangeAnalysisDTO.setAddLine(addLine);
        assetsChangeAnalysisDTO.setDelLine(delLine);
        assetsChangeAnalysisDTO.setUpdateLine(updateLine);
        assetsChangeAnalysisDTO.setAddPercent(addPercent);
        assetsChangeAnalysisDTO.setDelPercent(delPercent);
        assetsChangeAnalysisDTO.setUpdatePercent(updatePercent);
        assetsChangeAnalysisDTO.setTblCount(tblCount);
        assetsChangeAnalysisDTO.setFieldCount(fieldCount);
        assetsChangeAnalysisDTO.setCategoryDetailChanges(categoryDetailChanges);
        return assetsChangeAnalysisDTO;
    }

    /**
     * 获取元数据变更影响分析
     *
     * @param dto
     * @return
     */
    @Override
    public Page<AssetsChangeAnalysisDetailDTO> getMetaChangesChartsDetail(AssetsChangeAnalysisDetailQueryDTO dto) {
        //参数校验
        if (StringUtils.isEmpty(dto.getStartTime())
                || StringUtils.isEmpty(dto.getEndTime())
                || dto.getEntityType() == null
        ) {
            throw new FkException(ResultEnum.PARAMTER_ERROR);
        }

        //处理当前页
        if (dto.getCurrentPage() != 0) {
            dto.setCurrentPage(dto.getCurrentPage() - 1);
        }

        List<AssetsChangeAnalysisDetailDTO> results = new ArrayList<>();
        //转换时间
        LocalDateTime startTime = getLocalDateTime(dto.getStartTime());
        LocalDateTime endTime = getLocalDateTime(dto.getEndTime());
        /*
        获取查询的类型：
            ALL(0,"全部"),
            ADD(1,"添加"),
            EDIT(2,"编辑"),
            DELETE(3,"删除"),
         */
        //获取元数据类型
        EntityTypeEnum entityType = dto.getEntityType();

        List<AuditLogWithEntityTypeAndDetailPO> auditLogs;
        //根据查询操作类型 决定查询的内容
        Long total;
        if (entityType.equals(EntityTypeEnum.ALL)) {
            /*
             * 连表查询 tb_metadata_entity_audit_log 和 tb_metadata_entity 和 tb_metadata_entity_audit_atrribute_change
             */
            auditLogs = auditLogMapper.getMetaChangesChartsDetail(startTime, endTime, dto.getCurrentPage(), dto.getSize());
            total = auditLogMapper.countMetaChangesCharts(startTime, endTime, entityType.getValue());
        } else {
            auditLogs = auditLogMapper.getMetaChangesChartsDetailByOpType(startTime, endTime, entityType.getValue(),
                    dto.getCurrentPage(), dto.getSize());

            total = auditLogMapper.countMetaChangesChartsByOpType(startTime, endTime, entityType.getValue());
        }

        //获取所有表级元数据
        List<MetadataEntityPO> metadataEntityPOS = getTableEntitys();
        //转为map
        Map<Long, MetadataEntityPO> map = metadataEntityPOS.stream()
                .collect(Collectors.toMap(MetadataEntityPO::getId, Function.identity()));

        //获取用户
        List<UserDTO> data = new ArrayList<>();
        try {
            ResultEntity<List<UserDTO>> resultEntity = userClient.getAllUserList();
            if (resultEntity.getCode() == ResultEnum.SUCCESS.getCode()) {
                data = resultEntity.getData();
            }
        } catch (Exception e) {
            log.error("获取所有用户信息失败" + e);
        }

        for (AuditLogWithEntityTypeAndDetailPO po : auditLogs) {
            List<String> impactNames = new ArrayList<>();
            String content = "";
            AssetsChangeAnalysisDetailDTO detailDTO = new AssetsChangeAnalysisDetailDTO();
            detailDTO.setEntityId(po.getEntityId());
            detailDTO.setAuditId(po.auditId);
            EntityTypeEnum type = EntityTypeEnum.getValue(po.typeId);
            detailDTO.setType(type);

            //切换类型名称
            if (type.equals(EntityTypeEnum.RDBMS_TABLE)) {
                detailDTO.setTypeName("表");
            } else if (type.equals(EntityTypeEnum.RDBMS_COLUMN)) {
                detailDTO.setTypeName("字段");
            } else {
                detailDTO.setTypeName(entityType.getName());
            }

            detailDTO.setEntityName(po.name);

            //父id
            // 然后尝试从Map中获取值
            MetadataEntityPO metadataEntityPO = map.getOrDefault(Long.valueOf(po.parentId), null);
            if (metadataEntityPO != null) {
                detailDTO.setParentName(metadataEntityPO.getName());
            } else {
                detailDTO.setParentName("");
            }

            detailDTO.setEntityType(po.operationType);
            if (po.operationType.equals(MetadataAuditOperationTypeEnum.ADD)) {
                content = po.name;
            } else if (po.operationType.equals(MetadataAuditOperationTypeEnum.EDIT)) {
                if (po.attribute.equals("dataType")) {
                    content = po.beforeValue + " -> " + po.afterValue;
                } else {
                    content = "字符串(" + po.beforeValue + ") -> " + "字符串(" + po.afterValue + ")";
                }
            }
            detailDTO.setChangeContent(content);
            detailDTO.setCreateTime(po.createTime);

            //替换用户id为用户名
            if (!CollectionUtils.isEmpty(data)) {
                data.stream().filter(userDTO -> userDTO.getId().toString().equals(po.createUser)).findFirst().ifPresent(userDTO -> {
                    detailDTO.setOwnerId(userDTO.getUsername());
                });

            } else {
                detailDTO.setOwnerId(po.createUser);
            }

            //如果是字段 则可以检索该字段的影响性分析  （即从血缘表查询数据）
            if (type.equals(EntityTypeEnum.RDBMS_COLUMN)) {
                checkColumnChange(po, impactNames, metadataEntityPOS);
            }
            detailDTO.setImpactAnalysis(impactNames);

            results.add(detailDTO);
        }
        Page<AssetsChangeAnalysisDetailDTO> page = new Page<>();
        page.setCurrent(dto.getCurrentPage());
        page.setSize(dto.getSize());
        page.setTotal(total);
        page.setRecords(results);
        return page;
    }

    /**
     * 获取元数据变更影响分析
     *
     * @param dto
     * @return
     */
    @Override
    public List<AssetsChangeAnalysisDetailDTO> getMetaChangesChartsDetailWithoutPage(AssetsChangeAnalysisDetailQueryDTO dto) {

        List<AssetsChangeAnalysisDetailDTO> results = new ArrayList<>();
        //转换时间
        LocalDateTime startTime = getLocalDateTime(dto.getStartTime());
        LocalDateTime endTime = getLocalDateTime(dto.getEndTime());
        /*
        获取查询的类型：
            ALL(0,"全部"),
            ADD(1,"添加"),
            EDIT(2,"编辑"),
            DELETE(3,"删除"),
         */
        //获取元数据类型
        EntityTypeEnum entityType = dto.getEntityType();

        List<AuditLogWithEntityTypeAndDetailPO> auditLogs;
        //根据查询操作类型 决定查询的内容
        if (entityType.equals(EntityTypeEnum.ALL)) {
            /*
             * 连表查询 tb_metadata_entity_audit_log 和 tb_metadata_entity 和 tb_metadata_entity_audit_atrribute_change
             */
            auditLogs = auditLogMapper.getMetaChangesChartsDetailNoPage(startTime, endTime);
        } else {
            auditLogs = auditLogMapper.getMetaChangesChartsDetailByOpTypeNoPage(startTime, endTime, entityType.getValue());
        }

        //获取所有表级元数据
        List<MetadataEntityPO> metadataEntityPOS = getTableEntitys();
        //转为map
        Map<Long, MetadataEntityPO> map = metadataEntityPOS.stream()
                .collect(Collectors.toMap(MetadataEntityPO::getId, Function.identity()));

        //获取所有库级元数据
        List<MetadataEntityPO> dbEntitys = getDBEntitys();
        //转为map
        Map<Long, MetadataEntityPO> dbMap = dbEntitys.stream()
                .collect(Collectors.toMap(MetadataEntityPO::getId, Function.identity()));

        //获取用户
        List<UserDTO> data = new ArrayList<>();
        try {
            ResultEntity<List<UserDTO>> resultEntity = userClient.getAllUserList();
            if (resultEntity.getCode() == ResultEnum.SUCCESS.getCode()) {
                data = resultEntity.getData();
            }
        } catch (Exception e) {
            log.error("获取所有用户信息失败" + e);
        }

        for (AuditLogWithEntityTypeAndDetailPO po : auditLogs) {
            List<String> impactNames = new ArrayList<>();
            String content = "";
            AssetsChangeAnalysisDetailDTO detailDTO = new AssetsChangeAnalysisDetailDTO();
            detailDTO.setEntityId(po.getEntityId());
//            detailDTO.setAuditId(po.auditId);
            detailDTO.setType(EntityTypeEnum.getValue(po.typeId));

            //切换类型名称
            if (EntityTypeEnum.getValue(po.typeId).equals(EntityTypeEnum.RDBMS_TABLE)) {
                detailDTO.setTypeName("表");
                //父id
                // 然后尝试从Map中获取值
                MetadataEntityPO metadataEntityPO = dbMap.getOrDefault(Long.valueOf(po.parentId), null);
                if (metadataEntityPO != null) {
                    detailDTO.setParentName(metadataEntityPO.getName());
                } else {
                    detailDTO.setParentName("");
                }
            } else if (EntityTypeEnum.getValue(po.typeId).equals(EntityTypeEnum.RDBMS_COLUMN)) {
                detailDTO.setTypeName("字段");
                // 父id
                // 然后尝试从Map中获取值
                MetadataEntityPO metadataEntityPO = map.getOrDefault(Long.valueOf(po.parentId), null);
                if (metadataEntityPO != null) {
                    detailDTO.setParentName(metadataEntityPO.getName());
                } else {
                    detailDTO.setParentName("");
                }
            } else {
                detailDTO.setTypeName(entityType.getName());
            }

            detailDTO.setEntityName(po.name);
//            detailDTO.setEntityType(po.operationType);
            if (po.operationType.equals(MetadataAuditOperationTypeEnum.ADD)) {
                detailDTO.setEntityTypeName("新增");
                content = po.name;
            } else if (po.operationType.equals(MetadataAuditOperationTypeEnum.EDIT)) {
                detailDTO.setEntityTypeName("修改");
                if (po.attribute.equals("dataType")) {
                    content = po.beforeValue + " -> " + po.afterValue;
                } else {
                    content = "字符串(" + po.beforeValue + ") -> " + "字符串(" + po.afterValue + ")";
                }
            } else {
                detailDTO.setEntityTypeName(po.operationType.getName());
            }
            detailDTO.setChangeContent(content);
            detailDTO.setCreateTime(po.createTime);

            //替换用户id为用户名
            if (!CollectionUtils.isEmpty(data)) {
                data.stream().filter(userDTO -> userDTO.getId().toString().equals(po.createUser)).findFirst().ifPresent(userDTO -> {
                    detailDTO.setOwnerId(userDTO.getUsername());
                });
                if (detailDTO.getOwnerId()==null){
                    detailDTO.setOwnerId("Auto");
                }
            } else {
                detailDTO.setOwnerId(po.createUser);
            }

            //如果是字段 则可以检索该字段的影响性分析  （即从血缘表查询数据）
            if (EntityTypeEnum.getValue(po.typeId).equals(EntityTypeEnum.RDBMS_COLUMN)) {
                checkColumnChange(po, impactNames, metadataEntityPOS);
            }
            detailDTO.setImpactAnalysis(impactNames);

            results.add(detailDTO);
        }

        return results;
    }

    /**
     * 获取所有表元数据
     *
     * @return
     */
    private List<MetadataEntityPO> getTableEntitys() {
        return metadataEntityImpl.list(
                new LambdaQueryWrapper<MetadataEntityPO>()
                        .select(MetadataEntityPO::getId, MetadataEntityPO::getName)
                        .eq(MetadataEntityPO::getTypeId, EntityTypeEnum.RDBMS_TABLE.getValue())
        );
    }

    /**
     * 获取所有表元数据
     *
     * @return
     */
    private List<MetadataEntityPO> getDBEntitys() {
        return metadataEntityImpl.list(
                new LambdaQueryWrapper<MetadataEntityPO>()
                        .select(MetadataEntityPO::getId, MetadataEntityPO::getName)
                        .eq(MetadataEntityPO::getTypeId, EntityTypeEnum.RDBMS_DB.getValue())
        );
    }

    /**
     * 检索该字段的影响性分析
     */
    private void checkColumnChange(AuditLogWithEntityTypeAndDetailPO po, List<String> impactNames, List<MetadataEntityPO> metadataEntityPOS) {
        //获取该字段所属的表的元数据id
        int parentId = po.getParentId();

//        String name = null;
//        try {
//            name = Objects.requireNonNull(metadataEntityPOS.stream()
//                            .filter(metadataEntityPO -> metadataEntityPO.getId() == parentId)
//                            .findFirst()
//                            .orElse(null))
//                    .getName();
//        } catch (Exception e) {
//            log.error("空指针：" + e);
//            return;
//        }
//
//
//        //字段的父表
//        if (!StringUtils.isEmpty(name)) {
//            impactNames.add("【Parent:" + name + "】");
//        }

        //获取表元数据的血缘 向下找
        List<LineageMapRelationPO> list = lineageMapRelationImpl.list(new LambdaQueryWrapper<LineageMapRelationPO>()
                .eq(LineageMapRelationPO::getFromEntityId, parentId));

        //去重获取所有向下影响到的元数据id
        List<Integer> distinctToIds = list.stream().map(LineageMapRelationPO::getToEntityId).distinct().collect(Collectors.toList());

        if (CollectionUtils.isEmpty(distinctToIds)) {
            return;
        }

        //该集合用于循环向下找血缘
        List<Integer> ids = new ArrayList<>(distinctToIds);
        //循环向下获取所有受影响的元数据id
        while (true) {
            List<LineageMapRelationPO> list1 = lineageMapRelationImpl.list(new LambdaQueryWrapper<LineageMapRelationPO>()
                    .in(LineageMapRelationPO::getFromEntityId, ids));
            if (!CollectionUtils.isEmpty(list1)) {
                distinctToIds.addAll(list1.stream().map(LineageMapRelationPO::getToEntityId).distinct().collect(Collectors.toList()));
                ids.clear();
                ids.addAll(list1.stream().map(LineageMapRelationPO::getToEntityId).distinct().collect(Collectors.toList()));
            } else {
                break;
            }
        }

        //获取元数据和元数据关联的目录信息
        List<EntitiesClassificationDTO> dtos = metadataEntityMapper.getMetadataEntitiesWithClassification(distinctToIds);

        if (!CollectionUtils.isEmpty(dtos)) {
            //获取所有跟级业务分类(服务级别)
            List<BusinessClassificationPO> classificationPOS = classificationImpl.list(
                    new LambdaQueryWrapper<BusinessClassificationPO>()
                            .isNull(BusinessClassificationPO::getPid)
                            .select(BusinessClassificationPO::getId, BusinessClassificationPO::getName)
            );

            // id : name
            Map<Integer, String> cin = classificationPOS.stream()
                    .collect(Collectors.toMap(
                            poa -> (int) poa.getId(), // 将long类型的id转换为int类型
                            BusinessClassificationPO::getName
                    ));

            //服务名称：业务分类名称：元数据名称
            dtos.forEach(dto -> {
                impactNames.add("服务-" + cin.get(dto.getPid()) + "-" + dto.getCname() + ":" + dto.getName() + ";");
            });
        }
    }

    /**
     * 将 2024-04-11 转为 2024-04-11T00：00：00 的LocalDateTime
     *
     * @param dateTime 例：2024-04-11
     * @return LocalDateTime 2024-04-11T00：00：00
     */
    private LocalDateTime getLocalDateTime(String dateTime) {
        dateTime = dateTime + " 00:00:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateTime, formatter);
    }

}






