package com.fisk.datamodel.service.impl;

import com.fisk.datamodel.dto.factattribute.FactAttributeListDTO;
import com.fisk.datamodel.mapper.FactAttributeMapper;
import com.fisk.datamodel.service.IFactAttribute;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class FactAttributeImpl implements IFactAttribute {

    @Resource
    FactAttributeMapper mapper;

    @Override
    public List<FactAttributeListDTO> getFactAttributeList(int factId)
    {
        return mapper.getFactAttributeList(factId);
    }

}
