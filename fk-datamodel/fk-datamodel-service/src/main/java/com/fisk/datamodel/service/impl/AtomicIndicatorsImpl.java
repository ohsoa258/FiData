package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.atomicindicator.*;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.enums.DerivedIndicatorsEnum;
import com.fisk.datamodel.enums.FactAttributeEnum;
import com.fisk.datamodel.enums.IndicatorsTypeEnum;
import com.fisk.datamodel.map.AtomicIndicatorsMap;
import com.fisk.datamodel.map.FactAttributeMap;
import com.fisk.datamodel.mapper.*;
import com.fisk.datamodel.service.IAtomicIndicators;
import com.fisk.datamodel.vo.DataIndicatorVO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class AtomicIndicatorsImpl
        extends ServiceImpl<AtomicIndicatorsMapper,IndicatorsPO>
        implements IAtomicIndicators {

    @Resource
    AtomicIndicatorsMapper mapper;
    @Resource
    FactAttributeMapper factAttributeMapper;
    @Resource
    DimensionMapper dimensionMapper;
    @Resource
    IndicatorsMapper indicatorsMapper;
    @Resource
    FactMapper factMapper;
    @Resource
    BusinessLimitedAttributeMapper businessLimitedAttributeMapper;

    @Override
    public ResultEnum addAtomicIndicators(List<AtomicIndicatorsDTO> dto)
    {
        //查询原子指标数据
        QueryWrapper<IndicatorsPO> queryWrapper=new QueryWrapper<>();
        boolean repeat=false;
        for (AtomicIndicatorsDTO item: dto)
        {
            queryWrapper.lambda().eq(IndicatorsPO::getBusinessId,item.businessId)
                    .eq(IndicatorsPO::getIndicatorsName,item.indicatorsName);
            IndicatorsPO po=mapper.selectOne(queryWrapper);
            //判断是否重复
            if (po !=null)
            {
                repeat=true;
                break;
            }
        }
        //判断是否重复
        if (repeat)
        {
            return ResultEnum.DATA_EXISTS;
        }
        return this.saveBatch(AtomicIndicatorsMap.INSTANCES.dtoToPo(dto))?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteAtomicIndicators(int id)
    {
        IndicatorsPO po=mapper.selectById(id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.deleteByIdWithFill(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public AtomicIndicatorsDetailDTO getAtomicIndicatorDetails(int id)
    {
        IndicatorsPO po=mapper.selectById(id);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        AtomicIndicatorsDetailDTO data= AtomicIndicatorsMap.INSTANCES.poToDto(po);
        //判断是否为公式指标
        if (data.derivedIndicatorsType==DerivedIndicatorsEnum.BASED_FORMULA.getValue())
        {
            boolean exit=false;
            String formula=data.indicatorsFormula;
            String regex = "\\[(.*?)]";
            //截取中括号
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(formula);
            //循环获取中括号中的值
            while (matcher.find()) {
                System.out.println(matcher.group(1));
                String derivedId=matcher.group(1);
                //根据中括号的id获取指标名称
                IndicatorsPO selectByName=mapper.selectById(Integer.parseInt(derivedId));
                if (selectByName==null)
                {
                    exit=true;
                    break;
                }
                //替换中括号中的值
                formula=formula.replace(derivedId,String.valueOf(selectByName.indicatorsName));
            }
            data.indicatorsFormula=formula;
        }
        return data;
    }

    @Override
    public ResultEnum updateAtomicIndicatorDetails(AtomicIndicatorsDTO dto)
    {
        IndicatorsPO po=mapper.selectById(dto.id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<IndicatorsPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(IndicatorsPO::getBusinessId,dto.businessId)
                .eq(IndicatorsPO::getIndicatorsName,dto.indicatorsName);
        IndicatorsPO model=mapper.selectOne(queryWrapper);
        if (model !=null && model.id !=dto.id)
        {
            return ResultEnum.DATA_EXISTS;
        }
        po.indicatorsName=dto.indicatorsName;
        po.indicatorsDes=dto.indicatorsDes;
        po.calculationLogic=dto.calculationLogic;
        po.indicatorsCnName=dto.indicatorsCnName;
        po.factAttributeId=dto.factAttributeId;
        po.atomicId=dto.atomicId;
        po.businessLimitedId=dto.businessLimitedId;
        po.timePeriod=dto.timePeriod;
        po.derivedIndicatorsType=dto.derivedIndicatorsType;
        //判断是否为公式指标
        if (dto.derivedIndicatorsType== DerivedIndicatorsEnum.BASED_FORMULA.getValue())
        {
            boolean exit=false;
            String formula=dto.indicatorsFormula;
            String regex = "\\[(.*?)]";
            //截取中括号
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(formula);
            //循环获取中括号中的值
            while (matcher.find()) {
                System.out.println(matcher.group(1));
                String name=matcher.group(1);
                //根据中括号的名称与业务域获取指标id
                QueryWrapper<IndicatorsPO> indicatorsPOQueryWrapper = new QueryWrapper<>();
                indicatorsPOQueryWrapper.lambda().eq(IndicatorsPO::getBusinessId,po.businessId)
                        .eq(IndicatorsPO::getIndicatorsName,name);
                IndicatorsPO selectById=mapper.selectOne(indicatorsPOQueryWrapper);
                if (selectById==null)
                {
                    exit=true;
                    break;
                }
                //替换中括号中的值
                formula=formula.replace(name,String.valueOf(selectById.id));
            }
            if (exit)
            {
                return  ResultEnum.PARAMTER_ERROR;
            }
            po.indicatorsFormula=formula;
        }
        return mapper.updateById(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public Page<AtomicIndicatorsResultDTO> getAtomicIndicatorList(AtomicIndicatorsQueryDTO dto)
    {
        return mapper.queryList(dto.page,dto);
    }

    @Override
    public List<AtomicIndicatorDropListDTO> atomicIndicatorDropList(int businessId)
    {
        QueryWrapper<IndicatorsPO> queryWrapper=new QueryWrapper<>();
        if (businessId !=0)
        {
            queryWrapper.orderByDesc("create_time").lambda()
                    .eq(IndicatorsPO::getIndicatorsType,IndicatorsTypeEnum.ATOMIC_INDICATORS.getValue())
                    .eq(IndicatorsPO::getBusinessId,businessId);
        }
        return AtomicIndicatorsMap.INSTANCES.poToDtoList(mapper.selectList(queryWrapper));
    }

    @Override
    public List<AtomicIndicatorFactDTO> atomicIndicatorPush(List<Integer> factIds)
    {
        List<AtomicIndicatorFactDTO> list=new ArrayList<>();
        for (Integer id:factIds)
        {
            AtomicIndicatorFactDTO dto=new AtomicIndicatorFactDTO();
            FactPO po=factMapper.selectById(id);
            if (po==null)
            {
                continue;
            }
            dto.factId=po.id;
            dto.factTable=po.factTabName;
            dto.businessAreaId=po.businessId;
            dto.list=getAtomicIndicator(id);
            dto.factAttributeDTOList=getFactAttributeList(id);
            list.add(dto);
        }
        return list;
    }

    /**
     * 根据事实表获取所有原子指标/退化维度字段/关联维度表名称
     * @param factId
     * @return
     */
    public List<AtomicIndicatorPushDTO> getAtomicIndicator(int factId)
    {
        List<AtomicIndicatorPushDTO> data=new ArrayList<>();
        //获取事实表关联的维度
        QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("associate_dimension_id").lambda().eq(FactAttributePO::getFactId,factId);
        List<Object> list=factAttributeMapper.selectObjs(queryWrapper);
        List<Integer> ids= (List<Integer>)(List)list.stream().distinct().collect(Collectors.toList());
        if (ids!=null && ids.size()>0)
        {
            QueryWrapper<DimensionPO> dimensionQueryWrapper=new QueryWrapper<>();
            dimensionQueryWrapper.in("id",ids);
            List<DimensionPO> dimensionPOList=dimensionMapper.selectList(dimensionQueryWrapper);
            for (DimensionPO item:dimensionPOList)
            {
                AtomicIndicatorPushDTO dto=new AtomicIndicatorPushDTO();
                dto.attributeType=2;
                dto.dimensionTableName=item.dimensionTabName;
                data.add(dto);
            }
        }
        //获取事实表中的退化维度
        QueryWrapper<FactAttributePO> factAttributePOQueryWrapper=new QueryWrapper<>();
        factAttributePOQueryWrapper.select("id").lambda()
                .eq(FactAttributePO::getAttributeType,FactAttributeEnum.DEGENERATION_DIMENSION)
                .eq(FactAttributePO::getFactId,factId);
        List<Integer> factAttributeIds=(List)factAttributeMapper.selectObjs(factAttributePOQueryWrapper);
        if (factAttributeIds !=null && factAttributeIds.size()>0)
        {
            //存在退化维度,则查询该退化维度在业务限定中是否用到
            QueryWrapper<BusinessLimitedAttributePO> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.select("fact_attribute_id").in("fact_attribute_id",factAttributeIds);
            List<Integer> factAttributeIds1=(List)businessLimitedAttributeMapper.selectObjs(queryWrapper1);
            if (factAttributeIds1 !=null && factAttributeIds1.size()>0)
            {
                //退化在业务限定中用到,则字段进入Doris
                QueryWrapper<FactAttributePO> factAttributePOQueryWrapper1=new QueryWrapper<>();
                factAttributePOQueryWrapper1.in("id",factAttributeIds1);
                List<FactAttributePO> factAttributePOS=factAttributeMapper.selectList(factAttributePOQueryWrapper1);
                if (factAttributePOS !=null && factAttributePOS.size()>0)
                {
                    for (FactAttributePO po:factAttributePOS)
                    {
                        AtomicIndicatorPushDTO dto=new AtomicIndicatorPushDTO();
                        dto.attributeType=1;
                        dto.factFieldName=po.factFieldEnName;
                        dto.factFieldType=po.factFieldType;
                        dto.factFieldLength=po.factFieldLength;
                        data.add(dto);
                    }
                }
            }

        }

        //获取事实表下所有原子指标
        QueryWrapper<IndicatorsPO> indicatorsQueryWrapper=new QueryWrapper<>();
        indicatorsQueryWrapper.lambda().eq(IndicatorsPO::getFactId,factId)
                .eq(IndicatorsPO::getIndicatorsType, IndicatorsTypeEnum.ATOMIC_INDICATORS.getValue());
        List<IndicatorsPO> indicatorsPO=indicatorsMapper.selectList(indicatorsQueryWrapper);
        for (IndicatorsPO item:indicatorsPO)
        {
            AtomicIndicatorPushDTO dto=new AtomicIndicatorPushDTO();
            dto.attributeType=3;
            dto.atomicIndicatorName=item.indicatorsName;
            dto.aggregationLogic=item.calculationLogic;
            //获取聚合字段
            FactAttributePO factAttributePO=factAttributeMapper.selectById(item.factAttributeId);
            if (factAttributePO==null)
            {
                continue;
            }
            dto.aggregatedField=factAttributePO.factFieldEnName;
            dto.factFieldType=factAttributePO.factFieldType;
            dto.factFieldLength=factAttributePO.factFieldLength;
            data.add(dto);
        }
        return data;
    }

    /**
     * 根据事实表id,获取该事实表下所有字段
     * @param factId
     * @return
     */
    public List<AtomicIndicatorFactAttributeDTO> getFactAttributeList(int factId)
    {
        List<AtomicIndicatorFactAttributeDTO> data=new ArrayList<>();
        QueryWrapper<FactAttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(FactAttributePO::getFactId,factId);
        List<FactAttributePO> factAttributePOList=factAttributeMapper.selectList(queryWrapper);
        data= FactAttributeMap.INSTANCES.attributePoToDto(factAttributePOList);
        //获取关联维度表
        queryWrapper.select("associate_dimension_id");
        List<Integer> dimensionIds=(List)factAttributeMapper.selectObjs(queryWrapper);
        dimensionIds.stream().distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dimensionIds))
        {
            QueryWrapper<DimensionPO> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.in("id",dimensionIds);
            List<DimensionPO> dimensionPOList=dimensionMapper.selectList(queryWrapper1);
            if (!CollectionUtils.isEmpty(dimensionPOList))
            {
                for (DimensionPO item:dimensionPOList)
                {
                    AtomicIndicatorFactAttributeDTO dto=new AtomicIndicatorFactAttributeDTO();
                    dto.associateDimensionTable=item.dimensionTabName;
                    dto.attributeType=1;
                    data.add(dto);
                }
            }
        }
        return data;
    }

}
