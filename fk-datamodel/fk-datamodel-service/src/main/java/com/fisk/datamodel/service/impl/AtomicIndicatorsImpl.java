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
import com.fisk.datamodel.mapper.*;
import com.fisk.datamodel.service.IAtomicIndicators;
import com.fisk.datamodel.vo.DataIndicatorVO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import org.springframework.stereotype.Service;

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
    BusinessAreaMapper businessAreaMapper;
    @Resource
    BusinessProcessMapper businessProcessMapper;

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
        int flat=mapper.deleteByIdWithFill(po);

        return flat>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
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
    public List<AtomicIndicatorFactDTO> atomicIndicatorPush(int businessAreaId)
    {
        List<AtomicIndicatorFactDTO> list=new ArrayList<>();
        //判断业务域是否存在
        BusinessAreaPO businessAreaPO=businessAreaMapper.selectById(businessAreaId);
        if (businessAreaPO==null)
        {
            return list;
        }
        //获取业务域下的所有业务过程
        QueryWrapper<BusinessProcessPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessProcessPO::getBusinessId,businessAreaId);
        List<BusinessProcessPO> businessProcessPOList=businessProcessMapper.selectList(queryWrapper);
        if (businessProcessPOList==null || businessProcessPOList.size()==0)
        {
            return list;
        }
        for (BusinessProcessPO item:businessProcessPOList)
        {
            QueryWrapper<FactPO> factPOQueryWrapper=new QueryWrapper<>();
            factPOQueryWrapper.lambda().eq(FactPO::getBusinessProcessId,item.id);
            List<FactPO> factPOList=factMapper.selectList(factPOQueryWrapper);
            if (factPOList==null || factPOList.size()==0)
            {
                break;
            }
            for (FactPO factPO:factPOList)
            {
                AtomicIndicatorFactDTO data=new AtomicIndicatorFactDTO();
                data.factId=factPO.id;
                data.factTable=factPO.factTabName;
                List<AtomicIndicatorPushDTO> atomicIndicator=getAtomicIndicator((int)factPO.id);
                if (atomicIndicator!=null)
                {
                    data.list=atomicIndicator;
                    list.add(data);
                }
            }
        }
        return list;
    }

    /**
     * 根据事实表获取所有原子指标
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
                //dto.attributeType=FactAttributeEnum.ASSOCIATED_DIMENSION.getValue();
                dto.dimensionTableName=item.dimensionTabName;
                data.add(dto);
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
            dto.atomicIndicatorName=item.indicatorsName;
            dto.aggregationLogic=item.calculationLogic;
            //获取聚合字段
            FactAttributePO factAttributePO=factAttributeMapper.selectById(item.factAttributeId);
            dto.aggregatedField=factAttributePO==null?"":factAttributePO.factFieldEnName;
            data.add(dto);
        }
        return data;
    }

}
