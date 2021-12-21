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
import com.fisk.datamodel.enums.DataFactoryEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.mapper.FactMapper;
import com.fisk.datamodel.service.IDataFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    BusinessAreaMapper businessAreaMapper;

    @Override
    public List<ChannelDataDTO> getTableIds(NifiComponentsDTO dto)
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
        //获取枚举类型
        DataFactoryEnum dataFactoryEnum=DataFactoryEnum.getValue((int) dto.id);
        for (BusinessAreaPO item:businessAreaPOList)
        {
            ChannelDataDTO dataDTO=new ChannelDataDTO();
            dataDTO.id=item.getId();
            dataDTO.businessName =item.getBusinessName();
            switch (dataFactoryEnum)
            {
                case NUMBER_DIMENSION:
                    List<DimensionPO> dimensionPO=dimensionPOList.stream()
                            .filter(e->e.businessId==item.id && (e.isPublish== PublicStatusEnum.PUBLIC_SUCCESS.getValue() || e.isPublish==PublicStatusEnum.PUBLIC_ING.getValue()))
                            .collect(Collectors.toList());
                    dataDTO.list=getChannelDimensionData(dimensionPO);
                    break;
                case ANALYSIS_DIMENSION:
                    List<DimensionPO> dimensionPOS=dimensionPOList.stream()
                            .filter(e->e.businessId==item.id && (e.dorisPublish==PublicStatusEnum.PUBLIC_SUCCESS.getValue() || e.dorisPublish==PublicStatusEnum.PUBLIC_ING.getValue()))
                            .collect(Collectors.toList());
                    dataDTO.list=getChannelDimensionData(dimensionPOS);
                    break;
                case NUMBER_FACT:
                    List<FactPO> factPO=factPOList.stream()
                            .filter(e->e.businessId==item.id && (e.isPublish==PublicStatusEnum.PUBLIC_SUCCESS.getValue() || e.isPublish==PublicStatusEnum.PUBLIC_ING.getValue()))
                            .collect(Collectors.toList());
                    dataDTO.list=getChannelFactData(factPO);
                    break;
                case ANALYSIS_FACT:
                    List<FactPO> factPOS=factPOList.stream()
                            .filter(e->e.businessId==item.id && (e.dorisPublish==PublicStatusEnum.PUBLIC_SUCCESS.getValue() || e.dorisPublish==PublicStatusEnum.PUBLIC_ING.getValue()))
                            .collect(Collectors.toList());
                    dataDTO.list=getChannelFactData(factPOS);
                    break;
            }
            data.add(dataDTO);
        }
        // 反转倒序
        Collections.reverse(data);
        return data;
    }

    /**
     * 获取发布成功/正在发布维度表List
     * @param dimensionPO
     * @return
     */
    private List<ChannelDataChildDTO> getChannelDimensionData(List<DimensionPO> dimensionPO)
    {
        List<ChannelDataChildDTO> data=new ArrayList<>();
        if (!CollectionUtils.isEmpty(dimensionPO))
        {
            for (DimensionPO dimPO:dimensionPO)
            {
                ChannelDataChildDTO child=new ChannelDataChildDTO();
                child.id=dimPO.id;
                child.tableName=dimPO.dimensionTabName;
                data.add(child);
            }
        }
        return data;
    }

    /**
     * 获取发布成功/正在发布事实表List
     * @param factPO
     * @return
     */
    private List<ChannelDataChildDTO> getChannelFactData(List<FactPO> factPO)
    {
        List<ChannelDataChildDTO> data=new ArrayList<>();
        if (!CollectionUtils.isEmpty(factPO))
        {
            for (FactPO fact:factPO)
            {
                ChannelDataChildDTO child=new ChannelDataChildDTO();
                child.id=fact.id;
                child.tableName=fact.factTabName;
                data.add(child);
            }
        }
        return data;
    }

    @Override
    public ResultEntity<ComponentIdDTO> getBusinessAreaNameAndTableName(DataAccessIdsDTO dto)
    {
        ComponentIdDTO componentIdDTO=new ComponentIdDTO();
        BusinessAreaPO businessAreaPO=businessAreaMapper.selectById(dto.appId);
        componentIdDTO.appName=businessAreaPO==null?"":businessAreaPO.getBusinessName();
        if (dto.flag==DataFactoryEnum.NUMBER_DIMENSION.getValue() || dto.flag==DataFactoryEnum.ANALYSIS_DIMENSION.getValue())
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
