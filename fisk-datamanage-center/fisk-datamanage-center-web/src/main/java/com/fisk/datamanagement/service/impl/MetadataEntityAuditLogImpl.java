package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.ObjectInfoUtils;
import com.fisk.datamanagement.entity.MetadataAttributePO;
import com.fisk.datamanagement.entity.MetadataEntityAuditAttributeChangePO;
import com.fisk.datamanagement.entity.MetadataEntityAuditLogPO;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.enums.MetadataAuditOperationTypeEnum;
import com.fisk.datamanagement.service.IMetadataEntityAuditLog;
import com.fisk.datamanagement.mapper.MetadataEntityAuditLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    @Override
    public ResultEnum setMetadataAuditLog(Object object, Integer entityId, MetadataAuditOperationTypeEnum operationType, String rdbmsType) {
        List<MetadataAttributePO> afterMetadataAttributeList = getMetadataAttributeByObj(object);
        MetadataEntityAuditLogPO metadataEntityAuditLogPO = new MetadataEntityAuditLogPO();
        metadataEntityAuditLogPO.setEntityId(entityId);
        metadataEntityAuditLogPO.setOperationType(operationType);
        List<MetadataEntityAuditAttributeChangePO> auditAttributeChangePOList = new ArrayList<>();
        metadataEntityAuditLogPO.setEntityId(entityId);
        if (rdbmsType.equals(EntityTypeEnum.RDBMS_COLUMN.getName())) {
            String[] auditAttribute = {"dataType", "length"};
            if (operationType.equals(MetadataAuditOperationTypeEnum.EDIT)) {
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
                if (auditAttributeChangePOList.stream().count()>0){
                    this.save(metadataEntityAuditLogPO);
                    Integer auditId = (int)metadataEntityAuditLogPO.getId();
                    auditAttributeChangePOList.forEach(e -> e.setAuditId(auditId));
                    metadataEntityAuditAttributeChange.saveBatch(auditAttributeChangePOList);
                }
            }else {
                //新增元数据，只添加审计记录，不添加元数据属性详细变更记录
                this.save(metadataEntityAuditLogPO);
            }
        }
        return  ResultEnum.SUCCESS;
    }

    public List<MetadataAttributePO> getMetadataAttributeByObj(Object object) {
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




