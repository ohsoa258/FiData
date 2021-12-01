package com.fisk.datamodel.service.impl;

import com.fisk.datamodel.entity.DimensionAttributePO;
import com.fisk.datamodel.mapper.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.service.IDataService;
import com.fisk.dataservice.dto.isDimensionDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class DataServiceImpl implements IDataService {

    @Resource
    DimensionMapper dimensionMapper;
    @Resource
    DimensionAttributeMapper dimensionAttributeMapper;

    @Override
    public boolean isExistAssociate(isDimensionDTO dto)
    {
        //是维度字段
        if (dto.dimensionOne==1)
        {
            DimensionAttributePO dimensionAttributePO=dimensionAttributeMapper.selectById(dto.fieldIdOne);
            if (dimensionAttributePO==null)
            {
                return false;
            }

        }else {

        }

        return true;
    }
}
