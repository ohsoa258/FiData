package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datafactory.dto.components.ChannelDataChildDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.DimensionPO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.mapper.FactMapper;
import com.fisk.datamodel.service.IDataFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @Resource
    BusinessAreaMapper businessAreaMapper;

    @Override
    public List<ChannelDataDTO> getTableIds(NifiComponentsDTO dto) {
        List<ChannelDataDTO> list = new ArrayList<>();
        switch ((int) dto.id) {
            // 数据湖
            case 3:

                if (client.getTableId().code == 0) {
                    list = client.getTableId().data;
                }
                break;
            // 维度表
            case 4:
            case 6:
                list = getModelDataList(1);
                break;
            // 事实表
            case 5:
            case 7:
                list = getModelDataList(2);
                break;
            case 1:
            case 2:
            default:
                break;
        }
        return list;
    }

    private List<ChannelDataDTO> getModelDataList(int type)
    {
        List<ChannelDataDTO> data=new ArrayList<>();
        QueryWrapper<BusinessAreaPO> queryWrapper=new QueryWrapper<>();
        List<BusinessAreaPO> businessAreaPOList=businessAreaMapper.selectList(queryWrapper);
        if (businessAreaPOList==null || businessAreaPOList.size()==0)
        {
            return data;
        }
        //查询维度
        QueryWrapper<DimensionPO> dimensionPOQueryWrapper=new QueryWrapper<>();
        List<DimensionPO> dimensionPOList=dimensionMapper.selectList(dimensionPOQueryWrapper);
        //查询事实
        QueryWrapper<FactPO> factPOQueryWrapper=new QueryWrapper<>();
        List<FactPO> factPOList=factMapper.selectList(factPOQueryWrapper);
        for (BusinessAreaPO item:businessAreaPOList)
        {
            ChannelDataDTO dto=new ChannelDataDTO();
            dto.id=item.getId();
            dto.businessName =item.getBusinessName();
            //查询维度
            if (type==1)
            {
                List<DimensionPO> dimensionPO=dimensionPOList.stream()
                        .filter(e->e.businessId==item.id)
                        .collect(Collectors.toList());
                if (dimensionPO !=null && dimensionPO.size()>0)
                {
                    List<ChannelDataChildDTO> field=new ArrayList<>();
                    for (DimensionPO dimPO:dimensionPO)
                    {
                        ChannelDataChildDTO child=new ChannelDataChildDTO();
                        child.id=dimPO.id;
                        child.tableName=dimPO.dimensionTabName;
                        field.add(child);
                    }
                    dto.list=field;
                }
            }else {
                List<FactPO> factPO=factPOList.stream()
                        .filter(e->e.businessId==item.id)
                        .collect(Collectors.toList());
                if (factPO !=null && factPO.size()>0)
                {
                    List<ChannelDataChildDTO> field=new ArrayList<>();
                    for (FactPO fact:factPO)
                    {
                        ChannelDataChildDTO child=new ChannelDataChildDTO();
                        child.id=fact.id;
                        child.tableName=fact.factTabName;
                        field.add(child);
                    }
                    dto.list=field;
                }
            }
            data.add(dto);
        }
        return data;
    }

}
