package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.datamodel.TableQueryDTO;
import com.fisk.dataaccess.dto.taskschedule.ComponentIdDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.datafactory.dto.components.ChannelDataChildDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.dimension.DimensionPO;
import com.fisk.datamodel.entity.fact.FactPO;
import com.fisk.datamodel.entity.widetable.WideTableConfigPO;
import com.fisk.datamodel.enums.DataFactoryEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.mapper.BusinessAreaMapper;
import com.fisk.datamodel.mapper.dimension.DimensionMapper;
import com.fisk.datamodel.mapper.fact.FactMapper;
import com.fisk.datamodel.mapper.widetable.WideTableMapper;
import com.fisk.datamodel.service.IDataFactory;
import com.fisk.task.enums.OlapTableEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
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
    public List<ChannelDataDTO> getTableIds(NifiComponentsDTO dto) {
        List<ChannelDataDTO> data = new ArrayList<>();
        QueryWrapper<BusinessAreaPO> queryWrapper = new QueryWrapper<>();
        List<BusinessAreaPO> businessAreaPoList = businessAreaMapper.selectList(queryWrapper);
        if (businessAreaPoList == null || businessAreaPoList.size() == 0) {
            return data;
        }
        //查询维度
        QueryWrapper<DimensionPO> dimensionPoQueryWrapper = new QueryWrapper<>();
        dimensionPoQueryWrapper.lambda().eq(DimensionPO::getTimeTable, false);
        List<DimensionPO> dimensionPoList = dimensionMapper.selectList(dimensionPoQueryWrapper);
        //查询事实
        QueryWrapper<FactPO> factPoQueryWrapper = new QueryWrapper<>();
        List<FactPO> factPoList = factMapper.selectList(factPoQueryWrapper);
        //查询宽表
        QueryWrapper<WideTableConfigPO> wideTableConfigPoQueryWrapper = new QueryWrapper<>();
        List<WideTableConfigPO> wideTableConfigPoList = wideTableMapper.selectList(wideTableConfigPoQueryWrapper);
        //获取枚举类型
        DataFactoryEnum dataFactoryEnum = DataFactoryEnum.getValue((int) dto.id);
        for (BusinessAreaPO item : businessAreaPoList) {
            ChannelDataDTO dataDTO = new ChannelDataDTO();
            dataDTO.id = item.getId();
            dataDTO.businessName = item.getBusinessName();
            switch (dataFactoryEnum) {
                case NUMBER_DIMENSION:
                    List<DimensionPO> dimensionPo = dimensionPoList.stream()
                            .filter(e -> e.businessId == item.id && e.isPublish == PublicStatusEnum.PUBLIC_SUCCESS.getValue())
                            .collect(Collectors.toList());
                    dataDTO.list = getChannelDimensionData(dimensionPo, TableBusinessTypeEnum.DW_DIMENSION);
                    dataDTO.type = ChannelDataEnum.DW_DIMENSION_TASK.getName();
                    break;
                case ANALYSIS_DIMENSION:
                    List<DimensionPO> dimensionPoStreamList = dimensionPoList.stream()
                            .filter(e -> e.businessId == item.id && e.dorisPublish == PublicStatusEnum.PUBLIC_SUCCESS.getValue())
                            .collect(Collectors.toList());
                    dataDTO.list = getChannelDimensionData(dimensionPoStreamList, TableBusinessTypeEnum.NONE);
                    dataDTO.type = ChannelDataEnum.OLAP_DIMENSION_TASK.getName();
                    break;
                case NUMBER_FACT:
                    List<FactPO> factPo = factPoList.stream()
                            .filter(e -> e.businessId == item.id && (e.isPublish == PublicStatusEnum.PUBLIC_SUCCESS.getValue() || e.isPublish == PublicStatusEnum.PUBLIC_ING.getValue()))
                            .collect(Collectors.toList());
                    dataDTO.list = getChannelFactData(factPo, TableBusinessTypeEnum.DW_FACT);
                    dataDTO.type = ChannelDataEnum.DW_FACT_TASK.getName();
                    break;
                case ANALYSIS_FACT:
                    List<FactPO> factPoStreamList = factPoList.stream()
                            .filter(e -> e.businessId == item.id && (e.dorisPublish == PublicStatusEnum.PUBLIC_SUCCESS.getValue() || e.dorisPublish == PublicStatusEnum.PUBLIC_ING.getValue()))
                            .collect(Collectors.toList());
                    dataDTO.list = getChannelFactData(factPoStreamList, TableBusinessTypeEnum.NONE);
                    dataDTO.type = ChannelDataEnum.OLAP_FACT_TASK.getName();
                    break;
                case WIDE_TABLE:
                    List<WideTableConfigPO> wideTableConfigPoStreamList = wideTableConfigPoList.stream()
                            .filter(e -> e.businessId == item.id && (e.dorisPublish == PublicStatusEnum.PUBLIC_SUCCESS.getValue() || e.dorisPublish == PublicStatusEnum.PUBLIC_ING.getValue()))
                            .collect(Collectors.toList());
                    dataDTO.list = getChannelWideTableData(wideTableConfigPoStreamList);
                    dataDTO.type = ChannelDataEnum.OLAP_WIDETABLE_TASK.getName();
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
     *
     * @param dimensionPo
     * @return
     */
    private List<ChannelDataChildDTO> getChannelDimensionData(List<DimensionPO> dimensionPo, TableBusinessTypeEnum tableBusinessTypeEnum) {
        List<ChannelDataChildDTO> data = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dimensionPo)) {
            for (DimensionPO dimPo : dimensionPo) {
                ChannelDataChildDTO child = new ChannelDataChildDTO();
                child.id = dimPo.id;
                child.tableName = dimPo.dimensionTabName;
                child.tableBusinessType = tableBusinessTypeEnum.getValue();
                data.add(child);
            }
        }
        return data;
    }

    /**
     * 获取发布成功/正在发布事实表List
     *
     * @param factPo
     * @return
     */
    private List<ChannelDataChildDTO> getChannelFactData(List<FactPO> factPo, TableBusinessTypeEnum tableBusinessTypeEnum) {
        List<ChannelDataChildDTO> data = new ArrayList<>();
        if (!CollectionUtils.isEmpty(factPo)) {
            for (FactPO fact : factPo) {
                ChannelDataChildDTO child = new ChannelDataChildDTO();
                child.id = fact.id;
                child.tableName = fact.factTabName;
                child.tableBusinessType = tableBusinessTypeEnum.getValue();
                data.add(child);
            }
        }
        return data;
    }

    private List<ChannelDataChildDTO> getChannelWideTableData(List<WideTableConfigPO> wideTableConfigPo) {
        List<ChannelDataChildDTO> data = new ArrayList<>();
        if (!CollectionUtils.isEmpty(wideTableConfigPo)) {
            for (WideTableConfigPO wide : wideTableConfigPo) {
                ChannelDataChildDTO child = new ChannelDataChildDTO();
                child.id = wide.id;
                child.tableName = wide.name;
                data.add(child);
            }
        }
        return data;
    }

    @Override
    public ResultEntity<ComponentIdDTO> getBusinessAreaNameAndTableName(DataAccessIdsDTO dto) {
        ComponentIdDTO componentIdDTO = new ComponentIdDTO();
        BusinessAreaPO businessAreaPo = businessAreaMapper.selectById(dto.appId);
        componentIdDTO.appName = businessAreaPo == null ? "" : businessAreaPo.getBusinessName();
        if (dto.tableId != null) {
            if (dto.flag == DataFactoryEnum.NUMBER_DIMENSION.getValue()
                    || dto.flag == DataFactoryEnum.ANALYSIS_DIMENSION.getValue()) {
                DimensionPO dimensionPo = dimensionMapper.selectById(dto.tableId);
                componentIdDTO.tableName = dimensionPo == null ? "" : dimensionPo.dimensionTabName;
            } else if (dto.flag == DataFactoryEnum.NUMBER_FACT.getValue()
                    || dto.flag == DataFactoryEnum.ANALYSIS_FACT.getValue()) {
                FactPO factPo = factMapper.selectById(dto.tableId);
                componentIdDTO.tableName = factPo == null ? "" : factPo.factTabName;
            } else if (dto.flag == DataFactoryEnum.WIDE_TABLE.getValue()) {
                WideTableConfigPO po = wideTableMapper.selectById(dto.tableId);
                componentIdDTO.tableName = po == null ? "" : po.name;
            }
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, componentIdDTO);
    }

    @Override
    public Map<Integer, String> getTableNames(TableQueryDTO dto) {
        Map<Integer, String> map = new HashMap<>();
        List<BusinessAreaPO> businessAreaPOS = businessAreaMapper.selectList(
                new LambdaQueryWrapper<BusinessAreaPO>()
                        .select(BusinessAreaPO::getId, BusinessAreaPO::getBusinessName)
        );

        Map<Long, String> kvMap = businessAreaPOS.stream().collect(Collectors.toMap(BusinessAreaPO::getId, BusinessAreaPO::getBusinessName));


        switch (Objects.requireNonNull(OlapTableEnum.getNameByValue(dto.getType()))) {
            case DIMENSION:
                //查询维度
                QueryWrapper<DimensionPO> dimensionPoQueryWrapper = new QueryWrapper<>();
                dimensionPoQueryWrapper.lambda().in(DimensionPO::getId, dto.getIds());
                List<DimensionPO> dimensionPoList = dimensionMapper.selectList(dimensionPoQueryWrapper);
                for (DimensionPO dimensionPO : dimensionPoList) {
                    //表名
                    map.put((int) dimensionPO.getId(), dimensionPO.getDimensionTabName());
                    //业务域id
                    map.put(-100, dimensionPO.getBusinessId() + "");
                    //业务域名称
                    map.put(-200, kvMap.get((long) dimensionPO.getBusinessId()));
                }
                break;
            case FACT:
                //查询事实
                QueryWrapper<FactPO> factPoQueryWrapper = new QueryWrapper<>();
                factPoQueryWrapper.lambda().in(FactPO::getId, dto.getIds());
                List<FactPO> factPoList = factMapper.selectList(factPoQueryWrapper);
                for (FactPO factPO : factPoList) {
                    //表名
                    map.put((int) factPO.getId(), factPO.getFactTabName());
                    //业务域id
                    map.put(-100, factPO.getBusinessId() + "");
                    //业务域名称
                    map.put(-200, kvMap.get((long)factPO.getBusinessId()));
                }
                break;
            case WIDETABLE:
                //查询宽表
                QueryWrapper<WideTableConfigPO> wideTableConfigPoQueryWrapper = new QueryWrapper<>();
                wideTableConfigPoQueryWrapper.lambda().in(WideTableConfigPO::getId, dto.getIds());
                List<WideTableConfigPO> wideTableConfigPoList = wideTableMapper.selectList(wideTableConfigPoQueryWrapper);
                for (WideTableConfigPO wideTableConfigPO : wideTableConfigPoList) {
                    map.put((int) wideTableConfigPO.getId(), wideTableConfigPO.getName());
                }
                break;
            default:
                break;
        }
        return map;
    }
}
