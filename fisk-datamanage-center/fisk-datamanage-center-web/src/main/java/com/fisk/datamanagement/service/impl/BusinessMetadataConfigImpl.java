package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.datamanagement.dto.businessmetadataconfig.BusinessMetadataConfigDTO;
import com.fisk.datamanagement.entity.BusinessMetadataConfigPO;
import com.fisk.datamanagement.map.BusinessMetadataConfigMap;
import com.fisk.datamanagement.mapper.BusinessMetadataConfigMapper;
import com.fisk.datamanagement.service.IBusinessMetadataConfig;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class BusinessMetadataConfigImpl implements IBusinessMetadataConfig {

    @Resource
    BusinessMetadataConfigMapper businessMetadataConfigMapper;

    @Override
    public List<BusinessMetadataConfigDTO> getBusinessMetadataConfigList() {
        QueryWrapper<BusinessMetadataConfigPO> queryWrapper = new QueryWrapper<>();
        List<BusinessMetadataConfigPO> list = businessMetadataConfigMapper.selectList(queryWrapper);
        return BusinessMetadataConfigMap.INSTANCES.poListToDtoList(list);
    }

}
