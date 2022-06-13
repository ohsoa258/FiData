package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.taskschedule.ComponentIdDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.datafactory.dto.components.ChannelDataChildDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.DimensionPO;
import com.fisk.datamodel.entity.FactPO;
import com.fisk.datamodel.entity.WideTableConfigPO;
import com.fisk.datamodel.enums.DataFactoryEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.mapper.DimensionMapper;
import com.fisk.datamodel.mapper.FactMapper;
import com.fisk.datamodel.mapper.WideTableMapper;
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
    @Resource
    WideTableMapper wideTableMapper;

    @Override
    public List<ChannelDataDTO> getTableIds(NifiComponentsDTO dto)
    {
        List<ChannelDataDTO> data=new ArrayList<>();
        QueryWrapper<BusinessAreaPO> queryWrapper=new QueryWrapper<>();
        List<BusinessAreaPO> businessAreaPoList=businessAreaMapper.selectList(queryWrapper);
        if (businessAreaPoList==null || businessAreaPoList.size()==0)
        {
            return data;
        }
        //查询维度
        QueryWrapper<DimensionPO> dimensionPoQueryWrapper=new QueryWrapper<>();
        List<DimensionPO> dimensionPoList=dimensionMapper.selectList(dimensionPoQueryWrapper);
        //查询事实
        QueryWrapper<FactPO> factPoQueryWrapper=new QueryWrapper<>();
        List<FactPO> factPoList=factMapper.selectList(factPoQueryWrapper);
        //查询宽表
        QueryWrapper<WideTableConfigPO> wideTableConfigPoQueryWrapper=new QueryWrapper<>();
        List<WideTableConfigPO> wideTableConfigPoList=wideTableMapper.selectList(wideTableConfigPoQueryWrapper);
        //获取枚举类型
        DataFactoryEnum dataFactoryEnum=DataFactoryEnum.getValue((int) dto.id);
        for (BusinessAreaPO item:businessAreaPoList)
        {
            ChannelDataDTO dataDTO=new ChannelDataDTO();
            dataDTO.id=item.getId();
            dataDTO.businessName =item.getBusinessName();
            switch (dataFactoryEnum)
            {
                case NUMBER_DIMENSION:
                    List<DimensionPO> dimensionPo=dimensionPoList.stream()
                            .filter(e->e.businessId==item.id && (e.isPublish== PublicStatusEnum.PUBLIC_SUCCESS.getValue() || e.isPublish==PublicStatusEnum.PUBLIC_ING.getValue()))
                            .collect(Collectors.toList());
                    dataDTO.list=getChannelDimensionData(dimensionPo);
                    dataDTO.type=ChannelDataEnum.DW_DIMENSION_TASK.getName();
                    break;
                case ANALYSIS_DIMENSION:
                    List<DimensionPO> dimensionPoStreamList=dimensionPoList.stream()
                            .filter(e->e.businessId==item.id && (e.dorisPublish==PublicStatusEnum.PUBLIC_SUCCESS.getValue() || e.dorisPublish==PublicStatusEnum.PUBLIC_ING.getValue()))
                            .collect(Collectors.toList());
                    dataDTO.list=getChannelDimensionData(dimensionPoStreamList);
                    dataDTO.type=ChannelDataEnum.OLAP_DIMENSION_TASK.getName();
                    break;
                case NUMBER_FACT:
                    List<FactPO> factPo=factPoList.stream()
                            .filter(e->e.businessId==item.id && (e.isPublish==PublicStatusEnum.PUBLIC_SUCCESS.getValue() || e.isPublish==PublicStatusEnum.PUBLIC_ING.getValue()))
                            .collect(Collectors.toList());
                    dataDTO.list=getChannelFactData(factPo);
                    dataDTO.type=ChannelDataEnum.DW_FACT_TASK.getName();
                    break;
                case ANALYSIS_FACT:
                    List<FactPO> factPoStreamList=factPoList.stream()
                            .filter(e->e.businessId==item.id && (e.dorisPublish==PublicStatusEnum.PUBLIC_SUCCESS.getValue() || e.dorisPublish==PublicStatusEnum.PUBLIC_ING.getValue()))
                            .collect(Collectors.toList());
                    dataDTO.list=getChannelFactData(factPoStreamList);
                    dataDTO.type=ChannelDataEnum.OLAP_FACT_TASK.getName();
                    break;
                case WIDE_TABLE:
                    List<WideTableConfigPO> wideTableConfigPoStreamList=wideTableConfigPoList.stream()
                            .filter(e->e.businessId==item.id && (e.dorisPublish==PublicStatusEnum.PUBLIC_SUCCESS.getValue() || e.dorisPublish==PublicStatusEnum.PUBLIC_ING.getValue()))
                            .collect(Collectors.toList());
                    dataDTO.list=getChannelWideTableData(wideTableConfigPoStreamList);
                    dataDTO.type=ChannelDataEnum.OLAP_WIDETABLE_TASK.getName();
                    break;
                default:
            }
            data.add(dataDTO);
        }
        // 反转倒序
        Collections.reverse(data);
        return data;
    }

    /**
     * 获取发布成功/正在发布维度表List
     * @param dimensionPo
     * @return
     */
    private List<ChannelDataChildDTO> getChannelDimensionData(List<DimensionPO> dimensionPo)
    {
        List<ChannelDataChildDTO> data=new ArrayList<>();
        if (!CollectionUtils.isEmpty(dimensionPo))
        {
            for (DimensionPO dimPo:dimensionPo)
            {
                ChannelDataChildDTO child=new ChannelDataChildDTO();
                child.id=dimPo.id;
                child.tableName=dimPo.dimensionTabName;
                data.add(child);
            }
        }
        return data;
    }

    /**
     * 获取发布成功/正在发布事实表List
     * @param factPo
     * @return
     */
    private List<ChannelDataChildDTO> getChannelFactData(List<FactPO> factPo)
    {
        List<ChannelDataChildDTO> data=new ArrayList<>();
        if (!CollectionUtils.isEmpty(factPo))
        {
            for (FactPO fact:factPo)
            {
                ChannelDataChildDTO child=new ChannelDataChildDTO();
                child.id=fact.id;
                child.tableName=fact.factTabName;
                data.add(child);
            }
        }
        return data;
    }

    private List<ChannelDataChildDTO> getChannelWideTableData(List<WideTableConfigPO> wideTableConfigPo)
    {
        List<ChannelDataChildDTO> data=new ArrayList<>();
        if (!CollectionUtils.isEmpty(wideTableConfigPo))
        {
            for (WideTableConfigPO wide:wideTableConfigPo)
            {
                ChannelDataChildDTO child=new ChannelDataChildDTO();
                child.id=wide.id;
                child.tableName=wide.name;
                data.add(child);
            }
        }
        return data;
    }

    @Override
    public ResultEntity<ComponentIdDTO> getBusinessAreaNameAndTableName(DataAccessIdsDTO dto)
    {
        ComponentIdDTO componentIdDTO=new ComponentIdDTO();
        BusinessAreaPO businessAreaPo=businessAreaMapper.selectById(dto.appId);
        componentIdDTO.appName=businessAreaPo==null?"":businessAreaPo.getBusinessName();
        if (dto.flag==DataFactoryEnum.NUMBER_DIMENSION.getValue() || dto.flag==DataFactoryEnum.ANALYSIS_DIMENSION.getValue())
        {
            DimensionPO dimensionPo=dimensionMapper.selectById(dto.tableId);
            componentIdDTO.tableName=dimensionPo==null?"":dimensionPo.dimensionTabName;
        }else if (dto.flag==DataFactoryEnum.NUMBER_FACT.getValue() || dto.flag==DataFactoryEnum.ANALYSIS_FACT.getValue()){
            FactPO factPo=factMapper.selectById(dto.tableId);
            componentIdDTO.tableName=factPo==null?"":factPo.factTabName;
        }
        else if (dto.flag==DataFactoryEnum.WIDE_TABLE.getValue())
        {
            WideTableConfigPO po=wideTableMapper.selectById(dto.tableId);
            componentIdDTO.tableName=po==null?"":po.name;
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, componentIdDTO);
    }

}
