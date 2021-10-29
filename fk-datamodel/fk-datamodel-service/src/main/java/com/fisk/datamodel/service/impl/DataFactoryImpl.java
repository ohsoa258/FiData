package com.fisk.datamodel.service.impl;

import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datamodel.map.DataFactoryMap;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.mapper.FactMapper;
import com.fisk.datamodel.service.IDataFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class DataFactoryImpl implements IDataFactory {

    @Resource
    DimensionMapper dimensionMapper;
    @Resource
    FactMapper factMapper;
    @Resource
    DataAccessClient client;

    @Override
    public List<ChannelDataDTO> getTableIds(NifiComponentsDTO dto) {
        List<ChannelDataDTO> list = new ArrayList<>();
        switch ((int) dto.id) {
            case 1:
            case 2:
                break;
            case 3:

                if (client.getTableId().code == 0) {
                    list = client.getTableId().data;
//                    list = JSON.parseArray(JSON.toJSONString(client.getTableId().data), ChannelDataDTO.class);
                }
                break;
            case 4:
            case 6:
                list = DataFactoryMap.INSTANCES.tableDtosToPos(dimensionMapper.getDimensionTabList());
                break;
            case 5:
            case 7:
                list = DataFactoryMap.INSTANCES.tablesDtosToPos(factMapper.getFactTabList());
                break;
            default:
                break;
        }
        return list;
    }
}
