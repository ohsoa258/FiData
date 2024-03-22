package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.dto.metaauditlog.MetadataEntityAuditAttributeChangeVO;
import com.fisk.datamanagement.entity.MetadataEntityAuditAttributeChangePO;
import com.fisk.datamanagement.map.MetadataEntityAuditAttributeChangeMap;
import com.fisk.datamanagement.service.IMetadataEntityAuditAttributeChange;
import com.fisk.datamanagement.mapper.MetadataEntityAuditAttributeChangeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author JinXingWang
* @description 针对表【tb_metadata_entity_audit_atrribute_change】的数据库操作Service实现
* @createDate 2024-03-14 11:48:08
*/
@Service
public class MetadataEntityAuditAttributeChangeImpl extends ServiceImpl<MetadataEntityAuditAttributeChangeMapper, MetadataEntityAuditAttributeChangePO>
    implements IMetadataEntityAuditAttributeChange {

    @Override
    public List<MetadataEntityAuditAttributeChangeVO> getAttributeChange(Integer auditId){
        QueryWrapper<MetadataEntityAuditAttributeChangePO> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.lambda().eq(MetadataEntityAuditAttributeChangePO::getAuditId, auditId);
        List<MetadataEntityAuditAttributeChangePO> list1 = this.list(queryWrapper2);
        return MetadataEntityAuditAttributeChangeMap.INSTANCES.poToVoList(list1);
    }


}




