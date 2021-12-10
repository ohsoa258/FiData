package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.taskschedule.ComponentIdDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
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
import java.util.Collections;
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
            // 数仓维度
            case 4:
                list = getModelDataList(4);
                break;
            //分析维度
            case 6:
                list = getModelDataList(6);
                break;
            // 数仓事实
            case 5:
                list = getModelDataList(5);
                break;
            // 分析事实
            case 7:
                list = getModelDataList(7);
                break;
            case 1:
            case 2:
            case 3:
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
            List<ChannelDataChildDTO> field=new ArrayList<>();
            switch (type)
            {
                case 4:
                    List<DimensionPO> dimensionPO=dimensionPOList.stream()
                            .filter(e->e.businessId==item.id && e.isPublish==3)
                            .collect(Collectors.toList());
                    if (dimensionPO !=null && dimensionPO.size()>0)
                    {
                        for (DimensionPO dimPO:dimensionPO)
                        {
                            ChannelDataChildDTO child=new ChannelDataChildDTO();
                            child.id=dimPO.id;
                            child.tableName=dimPO.dimensionTabName;
                            field.add(child);
                        }
                    }
                    break;
                case 6:
                    List<DimensionPO> dimensionPO6=dimensionPOList.stream()
                            .filter(e->e.businessId==item.id && e.dorisPublish==3)
                            .collect(Collectors.toList());
                    if (dimensionPO6 !=null && dimensionPO6.size()>0)
                    {
                        for (DimensionPO dimPO:dimensionPO6)
                        {
                            ChannelDataChildDTO child=new ChannelDataChildDTO();
                            child.id=dimPO.id;
                            child.tableName=dimPO.dimensionTabName;
                            field.add(child);
                        }
                    }
                    break;
                case 5:
                    List<FactPO> factPO=factPOList.stream()
                            .filter(e->e.businessId==item.id && e.isPublish==3)
                            .collect(Collectors.toList());
                    if (factPO !=null && factPO.size()>0)
                    {
                        for (FactPO fact:factPO)
                        {
                            ChannelDataChildDTO child=new ChannelDataChildDTO();
                            child.id=fact.id;
                            child.tableName=fact.factTabName;
                            field.add(child);
                        }
                    }
                    break;
                case 7:
                    List<FactPO> factPO7=factPOList.stream()
                            .filter(e->e.businessId==item.id && e.dorisPublish==3)
                            .collect(Collectors.toList());
                    if (factPO7 !=null && factPO7.size()>0)
                    {
                        for (FactPO fact:factPO7)
                        {
                            ChannelDataChildDTO child=new ChannelDataChildDTO();
                            child.id=fact.id;
                            child.tableName=fact.factTabName;
                            field.add(child);
                        }
                    }
                    break;
            }
            dto.list=field;
            data.add(dto);
        }
        // 反转倒序
        Collections.reverse(data);
        return data;
    }

    @Override
    public ResultEntity<ComponentIdDTO> getBusinessAreaNameAndTableName(DataAccessIdsDTO dto)
    {
        ComponentIdDTO componentIdDTO=new ComponentIdDTO();
        BusinessAreaPO businessAreaPO=businessAreaMapper.selectById(dto.appId);
        componentIdDTO.appName=businessAreaPO==null?"":businessAreaPO.getBusinessName();
        if (dto.flag==4 || dto.flag==6)
        {
            DimensionPO dimensionPO=dimensionMapper.selectById(dto.tableId);
            componentIdDTO.tableName=dimensionPO==null?"":dimensionPO.dimensionTabName;
        }else {
            FactPO factPO=factMapper.selectById(dto.tableId);
            componentIdDTO.tableName=factPO==null?"":factPO.factTabName;
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, componentIdDTO);
    }

}
