package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsAddDTO;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsDTO;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsListDTO;
import com.fisk.datamodel.dto.derivedindicator.DerivedIndicatorsQueryDTO;
import com.fisk.datamodel.dto.derivedindicatorslimited.DerivedIndicatorsLimitedDTO;
import com.fisk.datamodel.entity.DerivedIndicatorsLimitedPO;
import com.fisk.datamodel.entity.IndicatorsPO;
import com.fisk.datamodel.enums.DerivedIndicatorsEnum;
import com.fisk.datamodel.map.DerivedIndicatorsLimitedMap;
import com.fisk.datamodel.map.DerivedIndicatorsMap;
import com.fisk.datamodel.mapper.DerivedIndicatorsLimitedMapper;
import com.fisk.datamodel.mapper.DerivedIndicatorsMapper;
import com.fisk.datamodel.service.IDerivedIndicators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class DerivedIndicatorsImpl
        extends ServiceImpl<DerivedIndicatorsLimitedMapper,DerivedIndicatorsLimitedPO>
        implements IDerivedIndicators {

    @Resource
    DerivedIndicatorsMapper mapper;
    @Resource
    DerivedIndicatorsLimitedMapper derivedIndicatorsLimitedMapper;
    @Resource
    UserHelper userHelper;

    @Override
    public Page<DerivedIndicatorsListDTO> getDerivedIndicatorsList(DerivedIndicatorsQueryDTO dto)
    {
        return mapper.queryList(dto.dto,dto);
    }

    @Override
    public ResultEnum deleteDerivedIndicators(long id)
    {
        IndicatorsPO po=mapper.selectById(id);
        if (po==null)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.deleteByIdWithFill(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addDerivedIndicators(DerivedIndicatorsDTO dto) {
        //判断是否重复
        QueryWrapper<IndicatorsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(IndicatorsPO::getIndicatorsName, dto.indicatorsName)
                .eq(IndicatorsPO::getBusinessId, dto.businessId);
        IndicatorsPO po = mapper.selectOne(queryWrapper);
        if (po != null) {
            return ResultEnum.DATA_EXISTS;
        }
        //添加派生指标数据，并返回插入id
        DerivedIndicatorsAddDTO poAdd=DerivedIndicatorsMap.INSTANCES.poToPo(dto);
        Date date = new Date(System.currentTimeMillis());
        poAdd.createTime=date;
        poAdd.createUser=userHelper.getLoginUserInfo().id.toString();
        //判断是否为指标公式
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
                QueryWrapper<IndicatorsPO> indicatorsPoQueryWrapper = new QueryWrapper<>();
                indicatorsPoQueryWrapper.lambda().eq(IndicatorsPO::getBusinessId,dto.businessId)
                        .eq(IndicatorsPO::getIndicatorsName,name);
                IndicatorsPO selectById=mapper.selectOne(indicatorsPoQueryWrapper);
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
            poAdd.indicatorsFormula=formula;
            return mapper.insertAndGetId(poAdd)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
        }
        //聚合字段拼接
        poAdd.aggregatedFields=String.join(",", poAdd.attributeId.stream().map(String::valueOf).collect(Collectors.toList()));
        int addId=mapper.insertAndGetId(poAdd);
        //判断insert是否成功，并返回插入主键id是否成功
        if (addId==0 || poAdd.id==0)
        {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //业务限定条件集合
        if (poAdd.limitedList !=null && poAdd.limitedList.size()>0)
        {
            List<DerivedIndicatorsLimitedPO> limitedPoList=new ArrayList<>();
            for (DerivedIndicatorsLimitedDTO item:dto.limitedList)
            {
                item.indicatorsId=poAdd.id;
                limitedPoList.add(DerivedIndicatorsLimitedMap.INSTANCES.dtoToPo(item));
                boolean result = this.saveBatch(limitedPoList);
                if (!result) {
                    throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                }
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public DerivedIndicatorsDTO getDerivedIndicators(long id)
    {
        IndicatorsPO po=mapper.selectById(id);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS, "数据不存在");
        }
        DerivedIndicatorsDTO dto=DerivedIndicatorsMap.INSTANCES.poToDto(po);
        //如果为指标公式，公式中的id替换为指标名称，并直接返回
        if (dto.derivedIndicatorsType==DerivedIndicatorsEnum.BASED_FORMULA.getValue())
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
            dto.indicatorsFormula=formula;
            return  dto;
        }
        //获取业务限定
        QueryWrapper<DerivedIndicatorsLimitedPO> limitedPoQueryWrapper=new QueryWrapper<>();
        limitedPoQueryWrapper.lambda().eq(DerivedIndicatorsLimitedPO::getIndicatorsId,po.id);
        List<DerivedIndicatorsLimitedPO> limitedList=derivedIndicatorsLimitedMapper.selectList(limitedPoQueryWrapper);
        List<DerivedIndicatorsLimitedDTO> dataList=new ArrayList<>();
        for (DerivedIndicatorsLimitedPO item:limitedList)
        {
            dataList.add(DerivedIndicatorsLimitedMap.INSTANCES.poToDto(item));
        }
        dto.limitedList=dataList;
        //获取聚合字段
        List<Integer> list= Arrays.stream(dto.aggregatedFields.split(",")).map(s -> Integer.valueOf(s.trim())).collect(Collectors.toList());
        dto.attributeId =list;
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum updateDerivedIndicators(DerivedIndicatorsDTO dto)
    {
        IndicatorsPO po=mapper.selectById(dto.id);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        po=DerivedIndicatorsMap.INSTANCES.dtoToPo(dto);
        if (po.derivedIndicatorsType==DerivedIndicatorsEnum.BASED_FORMULA.getValue())
        {
            boolean exit=false;
            String formula=po.indicatorsFormula;
            String regex = "\\[(.*?)]";
            //截取中括号
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(formula);
            //循环获取中括号中的值
            while (matcher.find()) {
                System.out.println(matcher.group(1));
                String name=matcher.group(1);
                //根据中括号的名称与业务域获取指标id
                QueryWrapper<IndicatorsPO> indicatorsPoQueryWrapper = new QueryWrapper<>();
                indicatorsPoQueryWrapper.lambda().eq(IndicatorsPO::getBusinessId,dto.businessId)
                        .eq(IndicatorsPO::getIndicatorsName,name);
                IndicatorsPO selectById=mapper.selectOne(indicatorsPoQueryWrapper);
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
            return mapper.updateById(po)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
        }
        //聚合字段拼接
        ////po.aggregatedFields=String.join(",", dto.attributeId.stream().map(String::valueOf).collect(Collectors.toList()));
        //保存派生指标数据
        int flat=mapper.updateById(po);
        if (flat==0)
        {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        //删除派生指标
        QueryWrapper<DerivedIndicatorsLimitedPO> derivedQueryWrapper=new QueryWrapper<>();
        derivedQueryWrapper.lambda().eq(DerivedIndicatorsLimitedPO::getIndicatorsId,dto.id);
        List<DerivedIndicatorsLimitedPO> derivedList=derivedIndicatorsLimitedMapper.selectList(derivedQueryWrapper);
        if (derivedList !=null && derivedList.size()>0)
        {
            boolean res=this.remove(derivedQueryWrapper);
            if (res == false) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        //保存派生指标下业务限定
        if (!CollectionUtils.isEmpty(dto.limitedList))
        {
            List<DerivedIndicatorsLimitedPO> limitedPoList=new ArrayList<>();
            for (DerivedIndicatorsLimitedDTO item:dto.limitedList)
            {
                item.indicatorsId=dto.id;
                limitedPoList.add(DerivedIndicatorsLimitedMap.INSTANCES.dtoToPo(item));
                boolean result = this.saveBatch(limitedPoList);
                if (!result) {
                    throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                }
            }
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public List<String> getIndicatorsList(int businessId)
    {
        QueryWrapper<IndicatorsPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("indicators_name").lambda().eq(IndicatorsPO::getBusinessId,businessId);
        List<Object> list=mapper.selectObjs(queryWrapper.orderByDesc("create_time"));
        List<String> str = (List)list;
        return str;
    }

}
