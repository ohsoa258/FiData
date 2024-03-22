package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.ObjectInfoUtils;
import com.fisk.datamanagement.dto.metaauditlog.AuditAnalysisAllChangeTotalVO;
import com.fisk.datamanagement.dto.metaauditlog.AuditAnalysisDayChangeTotalVO;
import com.fisk.datamanagement.dto.metaauditlog.MetadataEntityAuditAttributeChangeVO;
import com.fisk.datamanagement.dto.metaauditlog.MetadataEntityAuditLogVO;
import com.fisk.datamanagement.entity.MetadataAttributePO;
import com.fisk.datamanagement.entity.MetadataEntityAuditAttributeChangePO;
import com.fisk.datamanagement.entity.MetadataEntityAuditLogPO;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.enums.MetadataAuditOperationTypeEnum;
import com.fisk.datamanagement.map.MetadataEntityAuditLogMap;
import com.fisk.datamanagement.service.IMetadataEntityAuditLog;
import com.fisk.datamanagement.mapper.MetadataEntityAuditLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    @Override
    public ResultEnum setMetadataAuditLog(Object object, Integer entityId, MetadataAuditOperationTypeEnum operationType, String rdbmsType) {

        MetadataEntityAuditLogPO metadataEntityAuditLogPO = new MetadataEntityAuditLogPO();
        metadataEntityAuditLogPO.setEntityId(entityId);
        metadataEntityAuditLogPO.setOperationType(operationType);

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
                if (auditAttributeChangePOList.stream().count() > 0) {
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
                List<MetadataEntityAuditAttributeChangeVO> metadataEntityAttributeChangeVOS = metadataEntityAuditAttributeChange.getAttributeChange((int)e.getId());
                metadataEntityAuditLogVO.attribute = metadataEntityAttributeChangeVOS;
            });
        }
        return metadataEntityAuditLogVOS;
    }

    @Override
    public AuditAnalysisAllChangeTotalVO analysisAllChangeTotal() {
        QueryWrapper<MetadataEntityAuditLogPO>  queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(MetadataEntityAuditLogPO::getOperationType,MetadataAuditOperationTypeEnum.ADD);
        int addCount = this.count(queryWrapper);

        QueryWrapper<MetadataEntityAuditLogPO>  queryWrapper2=new QueryWrapper<>();
        queryWrapper2.lambda().eq(MetadataEntityAuditLogPO::getOperationType,MetadataAuditOperationTypeEnum.EDIT);
        int editCount = this.count(queryWrapper2);

        QueryWrapper<MetadataEntityAuditLogPO>  queryWrapper3=new QueryWrapper<>();
        queryWrapper3.lambda().eq(MetadataEntityAuditLogPO::getOperationType,MetadataAuditOperationTypeEnum.DELETE);
        int deleteCount = this.count(queryWrapper3);

        AuditAnalysisAllChangeTotalVO auditAnalysisChangeTotalVO=new AuditAnalysisAllChangeTotalVO();
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
}




