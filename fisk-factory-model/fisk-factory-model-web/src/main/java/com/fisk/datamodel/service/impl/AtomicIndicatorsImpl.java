package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamodel.dto.atomicindicator.*;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.enums.DerivedIndicatorsEnum;
import com.fisk.datamodel.enums.FactAttributeEnum;
import com.fisk.datamodel.enums.IndicatorsTypeEnum;
import com.fisk.datamodel.map.AtomicIndicatorsMap;
import com.fisk.datamodel.map.FactAttributeMap;
import com.fisk.datamodel.mapper.*;
import com.fisk.datamodel.service.IAtomicIndicators;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class AtomicIndicatorsImpl
        extends ServiceImpl<AtomicIndicatorsMapper, IndicatorsPO>
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
    public ResultEnum addAtomicIndicators(List<AtomicIndicatorsDTO> dto) {
        //查询原子指标数据
        QueryWrapper<IndicatorsPO> queryWrapper = new QueryWrapper<>();
        boolean repeat = false;
        for (AtomicIndicatorsDTO item : dto) {
            item.indicatorsName = item.indicatorsName.toLowerCase();
            queryWrapper.lambda().eq(IndicatorsPO::getBusinessId, item.businessId)
                    .eq(IndicatorsPO::getIndicatorsName, item.indicatorsName);
            IndicatorsPO po = mapper.selectOne(queryWrapper);
            //判断是否重复
            if (po != null) {
                repeat = true;
                break;
            }
        }
        //判断是否重复
        if (repeat) {
            return ResultEnum.DATA_EXISTS;
        }
        return this.saveBatch(AtomicIndicatorsMap.INSTANCES.dtoToPo(dto)) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteAtomicIndicators(int id) {
        IndicatorsPO po = mapper.selectById(id);
        if (po == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return mapper.deleteByIdWithFill(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public AtomicIndicatorsDetailDTO getAtomicIndicatorDetails(int id) {
        IndicatorsPO po = mapper.selectById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        AtomicIndicatorsDetailDTO data = AtomicIndicatorsMap.INSTANCES.poToDto(po);
        //判断是否为公式指标
        if (data.derivedIndicatorsType == DerivedIndicatorsEnum.BASED_FORMULA.getValue()) {
            boolean exit = false;
            String formula = data.indicatorsFormula;
            String regex = "\\[(.*?)]";
            //截取中括号
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(formula);
            //循环获取中括号中的值
            while (matcher.find()) {
                System.out.println(matcher.group(1));
                String derivedId = matcher.group(1);
                String name = derivedId.replace("@", "");
                //根据中括号的id获取指标名称
                IndicatorsPO selectByName = mapper.selectById(Integer.parseInt(name));
                if (selectByName == null) {
                    exit = true;
                    break;
                }
                //替换中括号中的值
                formula = formula.replace(derivedId, "@" + String.valueOf(selectByName.indicatorsName));
            }
            data.indicatorsFormula = formula;
        }
        return data;
    }

    @Override
    public ResultEnum updateAtomicIndicatorDetails(AtomicIndicatorsDTO dto) {
        IndicatorsPO po = mapper.selectById(dto.id);
        if (po == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<IndicatorsPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(IndicatorsPO::getBusinessId, dto.businessId)
                .eq(IndicatorsPO::getIndicatorsName, dto.indicatorsName);
        IndicatorsPO model = mapper.selectOne(queryWrapper);
        if (model != null && model.id != dto.id) {
            return ResultEnum.DATA_EXISTS;
        }
        po.indicatorsName = dto.indicatorsName.toLowerCase();
        po.indicatorsDes = dto.indicatorsDes;
        po.calculationLogic = dto.calculationLogic;
        po.indicatorsCnName = dto.indicatorsCnName;
        po.factAttributeId = dto.factAttributeId;
        po.atomicId = dto.atomicId;
        po.businessLimitedId = dto.businessLimitedId;
        po.timePeriod = dto.timePeriod;
        po.derivedIndicatorsType = dto.derivedIndicatorsType;
        po.indicatorsFormula = dto.indicatorsFormula;
        //判断是否为公式指标
        if (dto.derivedIndicatorsType == DerivedIndicatorsEnum.BASED_FORMULA.getValue()) {
            boolean exit = false;
            String formula = dto.indicatorsFormula;
            String regex = "\\[(.*?)]";
            //截取中括号
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(formula);
            //循环获取中括号中的值
            while (matcher.find()) {
                System.out.println(matcher.group(1));
                String name = matcher.group(1);
                String newName = name.replace("@", "");
                //根据中括号的名称与业务域获取指标id
                QueryWrapper<IndicatorsPO> indicatorsPoQueryWrapper = new QueryWrapper<>();
                indicatorsPoQueryWrapper.lambda().eq(IndicatorsPO::getBusinessId, po.businessId)
                        .eq(IndicatorsPO::getIndicatorsName, newName);
                IndicatorsPO selectById = mapper.selectOne(indicatorsPoQueryWrapper);
                if (selectById == null) {
                    exit = true;
                    break;
                }
                //替换中括号中的值
                formula = formula.replace(name, "@" + String.valueOf(selectById.id));
            }
            if (exit) {
                return ResultEnum.PARAMTER_ERROR;
            }
            po.indicatorsFormula = formula;
        }
        return mapper.updateById(po) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public Page<AtomicIndicatorsResultDTO> getAtomicIndicatorList(AtomicIndicatorsQueryDTO dto) {
        return mapper.queryList(dto.page, dto);
    }

    @Override
    public List<AtomicIndicatorDropListDTO> atomicIndicatorDropList(int businessId) {
        QueryWrapper<IndicatorsPO> queryWrapper = new QueryWrapper<>();
        if (businessId != 0) {
            queryWrapper.orderByDesc("create_time").lambda()
                    .eq(IndicatorsPO::getIndicatorsType, IndicatorsTypeEnum.ATOMIC_INDICATORS.getValue())
                    .eq(IndicatorsPO::getBusinessId, businessId);
        }
        return AtomicIndicatorsMap.INSTANCES.poToDtoList(mapper.selectList(queryWrapper));
    }

    @Override
    public List<AtomicIndicatorFactDTO> atomicIndicatorPush(List<Integer> factIds) {
        List<AtomicIndicatorFactDTO> list = new ArrayList<>();
        for (Integer id : factIds) {
            AtomicIndicatorFactDTO dto = new AtomicIndicatorFactDTO();
            FactPO po = factMapper.selectById(id);
            if (po == null) {
                continue;
            }
            dto.factId = po.id;
            dto.factTable = po.factTabName;
            dto.businessAreaId = po.businessId;
            dto.list = getAtomicIndicator(id);
            dto.factAttributeDTOList = getFactAttributeList(id);
            list.add(dto);
        }
        return list;
    }

    /**
     * 根据事实表获取所有原子指标/退化维度字段/关联维度表名称
     *
     * @param factId
     * @return
     */
    public List<AtomicIndicatorPushDTO> getAtomicIndicator(int factId) {
        List<AtomicIndicatorPushDTO> data = new ArrayList<>();
        //获取事实表关联的维度
        QueryWrapper<FactAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("associate_dimension_id").lambda().eq(FactAttributePO::getFactId, factId);
        List<Object> list = factAttributeMapper.selectObjs(queryWrapper);
        List<Integer> ids = (List<Integer>) (List) list.stream().distinct().collect(Collectors.toList());
        if (ids != null && ids.size() > 0) {
            QueryWrapper<DimensionPO> dimensionQueryWrapper = new QueryWrapper<>();
            dimensionQueryWrapper.in("id", ids);
            List<DimensionPO> dimensionPoList = dimensionMapper.selectList(dimensionQueryWrapper);
            for (DimensionPO item : dimensionPoList) {
                AtomicIndicatorPushDTO dto = new AtomicIndicatorPushDTO();
                dto.attributeType = FactAttributeEnum.DIMENSION_KEY.getValue();
                dto.dimensionTableName = item.dimensionTabName;
                dto.id = item.id;
                data.add(dto);
            }
        }

        //获取事实表中的退化维度
        QueryWrapper<FactAttributePO> factAttributePoQueryWrapper = new QueryWrapper<>();
        factAttributePoQueryWrapper.select("id").lambda()
                .eq(FactAttributePO::getAttributeType, FactAttributeEnum.DEGENERATION_DIMENSION)
                .eq(FactAttributePO::getFactId, factId);
        List<Integer> factAttributeIds = (List) factAttributeMapper.selectObjs(factAttributePoQueryWrapper);
        if (factAttributeIds != null && factAttributeIds.size() > 0) {
            //存在退化维度,则查询该退化维度在业务限定中是否用到
            QueryWrapper<BusinessLimitedAttributePO> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.select("fact_attribute_id").in("fact_attribute_id", factAttributeIds);
            List<Integer> factAttributeIds1 = (List) businessLimitedAttributeMapper.selectObjs(queryWrapper1);
            if (factAttributeIds1 != null && factAttributeIds1.size() > 0) {
                //退化在业务限定中用到,则字段进入Doris
                QueryWrapper<FactAttributePO> factAttributePoQueryWrapper1 = new QueryWrapper<>();
                factAttributePoQueryWrapper1.in("id", factAttributeIds1);
                List<FactAttributePO> factAttributePoList = factAttributeMapper.selectList(factAttributePoQueryWrapper1);
                if (factAttributePoList != null && factAttributePoList.size() > 0) {
                    for (FactAttributePO po : factAttributePoList) {
                        AtomicIndicatorPushDTO dto = new AtomicIndicatorPushDTO();
                        dto.attributeType = FactAttributeEnum.DEGENERATION_DIMENSION.getValue();
                        dto.factFieldName = po.factFieldEnName;
                        dto.factFieldType = po.factFieldType;
                        dto.factFieldLength = po.factFieldLength;
                        dto.id = po.id;
                        data.add(dto);
                    }
                }
            }

        }

        //获取事实表下所有原子指标
        QueryWrapper<IndicatorsPO> indicatorsQueryWrapper = new QueryWrapper<>();
        indicatorsQueryWrapper.lambda().eq(IndicatorsPO::getFactId, factId)
                .eq(IndicatorsPO::getIndicatorsType, IndicatorsTypeEnum.ATOMIC_INDICATORS.getValue());
        List<IndicatorsPO> indicatorsPo = indicatorsMapper.selectList(indicatorsQueryWrapper);
        for (IndicatorsPO item : indicatorsPo) {
            AtomicIndicatorPushDTO dto = new AtomicIndicatorPushDTO();
            dto.attributeType = FactAttributeEnum.MEASURE.getValue();
            dto.atomicIndicatorName = item.indicatorsName;
            dto.aggregationLogic = item.calculationLogic;
            //获取聚合字段
            FactAttributePO factAttributePo = factAttributeMapper.selectById(item.factAttributeId);
            if (factAttributePo == null) {
                continue;
            }
            dto.aggregatedField = factAttributePo.factFieldEnName;
            dto.factFieldType = factAttributePo.factFieldType;
            dto.factFieldLength = factAttributePo.factFieldLength;
            dto.id = item.id;
            data.add(dto);
        }
        return data;
    }

    /**
     * 根据事实表id,获取派生指标
     *
     * @param factId
     * @return
     */
    public List<AtomicIndicatorPushDTO> getDerivedIndicators(int factId) {
        List<AtomicIndicatorPushDTO> data = new ArrayList<>();
        QueryWrapper<IndicatorsPO> indicatorsQueryWrapper = new QueryWrapper<>();
        indicatorsQueryWrapper.lambda().eq(IndicatorsPO::getFactId, factId)
                .eq(IndicatorsPO::getIndicatorsType, IndicatorsTypeEnum.DERIVED_INDICATORS.getValue());
        List<IndicatorsPO> indicatorsPo = indicatorsMapper.selectList(indicatorsQueryWrapper);
        for (IndicatorsPO item : indicatorsPo) {
            AtomicIndicatorPushDTO dto = new AtomicIndicatorPushDTO();
            dto.id = item.id;
            dto.attributeType = 3;
            dto.atomicIndicatorName = item.indicatorsName;
            //时间周期
            dto.aggregationLogic = item.timePeriod;
            dto.atomicId = item.atomicId;
            data.add(dto);
        }
        return data;
    }

    /**
     * 根据事实表id,获取该事实表下所有字段
     *
     * @param factId
     * @return
     */
    public List<AtomicIndicatorFactAttributeDTO> getFactAttributeList(int factId) {
        List<AtomicIndicatorFactAttributeDTO> data = new ArrayList<>();
        QueryWrapper<FactAttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FactAttributePO::getFactId, factId);
        List<FactAttributePO> factAttributePoList = factAttributeMapper.selectList(queryWrapper);
        data = FactAttributeMap.INSTANCES.attributePoToDto(factAttributePoList);
        //获取关联维度表
        queryWrapper.select("associate_dimension_id");
        List<Integer> dimensionIds = (List) factAttributeMapper.selectObjs(queryWrapper);
        dimensionIds.stream().distinct().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dimensionIds)) {
            QueryWrapper<DimensionPO> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.in("id", dimensionIds);
            List<DimensionPO> dimensionPoList = dimensionMapper.selectList(queryWrapper1);
            if (!CollectionUtils.isEmpty(dimensionPoList)) {
                for (DimensionPO item : dimensionPoList) {
                    AtomicIndicatorFactAttributeDTO dto = new AtomicIndicatorFactAttributeDTO();
                    dto.associateDimensionTable = item.dimensionTabName;
                    dto.attributeType = 1;
                    data.add(dto);
                }
            }
        }
        return data;
    }

    /**
     * 获取分析指标SQL
     *
     * @param factId 事实表id
     * @return SQL
     */
    @Override
    public ResultEntity<String> getAnalysisIndexSql(int factId) {
        StringBuilder stringBuilder = new StringBuilder();
        if (factId <= 0)
        {
            return ResultEntityBuild.buildData(ResultEnum.PARAMTER_ERROR, stringBuilder.toString());
        }
        FactPO factPo = factMapper.selectById(factId);
        if (factPo == null)
        {
            return ResultEntityBuild.buildData(ResultEnum.DATA_NOTEXISTS, stringBuilder.toString());
        }
        if (factPo.isPublish != 1)
        {
            return ResultEntityBuild.buildData(ResultEnum.PUBLISH_NOTSUCCESS, stringBuilder.toString());
        }
        // 第一步：查询原子指标 & 派生指标
        QueryWrapper<IndicatorsPO> indicatorsQueryWrapper = new QueryWrapper<>();
        indicatorsQueryWrapper.lambda()
                .eq(IndicatorsPO::getFactId, factId)
                .eq(IndicatorsPO::getDelFlag, 1);
        List<IndicatorsPO> indicatorsPoList = mapper.selectList(indicatorsQueryWrapper);
        if (CollectionUtils.isEmpty(indicatorsPoList))
        {
            return ResultEntityBuild.buildData(ResultEnum.DATA_NOTEXISTS, stringBuilder.toString());
        }
        // 第二步：查询

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, stringBuilder.toString());
    }

    /**
     * YTD、QTD、MTD指标分析
     *
     * @param timePeriodType 指标类型
     * @param dimensionFiled 维度字段
     * @return SQL
     */
    public String getDerivedIndex(String timePeriodType, String dimensionFiled) {
        StringBuilder stringBuilder = new StringBuilder();
        switch (timePeriodType) {
            case "YTD":
                stringBuilder.append(String.format("SELECT\n" +
                        "\tfulldatekey,\n" +
                        "\tSUM( sum_amount ) over ( PARTITION BY YEAR ( fulldatekey ), YEAR ( fulldatekey ) ORDER BY fulldatekey rows BETWEEN UNBOUNDED preceding AND CURRENT ROW ) AS YTD \n" +
                        "FROM\n" +
                        "\t(\n" +
                        "\tSELECT\n" +
                        "\t\tdim_date.fulldatekey,\n" +
                        "\t\tsum( sum_amount ) AS sum_amount \n" +
                        "\tFROM\n" +
                        "\t\tfact_internetsales\n" +
                        "\t\tJOIN dim_date ON dim_date.`datekey` = fact_internetsales.`datekey` \n" +
                        "\tGROUP BY\n" +
                        "\t\tdim_date.fulldatekey \n" +
                        "\t) a \n" +
                        "ORDER BY\n" +
                        "\tfulldatekey"));
                break;
            case "QTD":
                stringBuilder.append("SELECT\n" +
                        "\tfulldatekey,\n" +
                        "\tSUM( sum_amount ) over ( PARTITION BY YEAR ( fulldatekey ), QUARTER ( fulldatekey ) ORDER BY fulldatekey rows BETWEEN UNBOUNDED preceding AND CURRENT ROW ) AS QTD \n" +
                        "FROM\n" +
                        "\t(\n" +
                        "\tSELECT\n" +
                        "\t\tdim_date.fulldatekey,\n" +
                        "\t\tsum( sum_amount ) AS sum_amount \n" +
                        "\tFROM\n" +
                        "\t\tfact_internetsales\n" +
                        "\t\tJOIN dim_date ON dim_date.`datekey` = fact_internetsales.`datekey` \n" +
                        "\tGROUP BY\n" +
                        "\t\tdim_date.fulldatekey \n" +
                        "\t) a \n" +
                        "ORDER BY\n" +
                        "\tfulldatekey");
                break;
            case "MTD":
                stringBuilder.append("SELECT\n" +
                        "\tfulldatekey,\n" +
                        "\tSUM( sum_amount ) over ( PARTITION BY YEAR ( fulldatekey ), MONTH ( fulldatekey ) ORDER BY fulldatekey rows BETWEEN UNBOUNDED preceding AND CURRENT ROW ) AS MTD \n" +
                        "FROM\n" +
                        "\t(\n" +
                        "\tSELECT\n" +
                        "\t\tdim_date.fulldatekey,\n" +
                        "\t\tsum( sum_amount ) AS sum_amount \n" +
                        "\tFROM\n" +
                        "\t\tfact_internetsales\n" +
                        "\t\tJOIN dim_date ON dim_date.`datekey` = fact_internetsales.`datekey` \n" +
                        "\tGROUP BY\n" +
                        "\t\tdim_date.fulldatekey \n" +
                        "\t) a \n" +
                        "ORDER BY\n" +
                        "\tfulldatekey");
                break;
            default:
                break;
        }
        return stringBuilder.toString();
    }

    /**
     * YTD/QTD/MTD 上一期指标分析
     *
     * @param timePeriodType 指标类型
     * @param dimensionFiled 维度字段
     * @return SQL
     */
    public String getDerivedIndexPreviousIssue(String timePeriodType, String dimensionFiled) {
        StringBuilder stringBuilder = new StringBuilder();
        switch (timePeriodType) {
            case "YTD":
                stringBuilder.append("SELECT\n" +
                        "\tfulldatekey,\n" +
                        "\tytd,\n" +
                        "\tlag( ytd, 1, 0 ) over ( ORDER BY fulldatekey ) AS \"ytd _next\" \n" +
                        "FROM\n" +
                        "\t(\n" +
                        "\tSELECT\n" +
                        "\t\tfulldatekey,\n" +
                        "\t\tSUM( sum_amount ) over ( PARTITION BY YEAR ( fulldatekey ), YEAR ( fulldatekey ) ORDER BY fulldatekey rows BETWEEN UNBOUNDED preceding AND CURRENT ROW ) AS ytd \n" +
                        "\tFROM\n" +
                        "\t\t(\n" +
                        "\t\tSELECT\n" +
                        "\t\t\tdim_date.fulldatekey,\n" +
                        "\t\t\tsum( sum_amount ) AS sum_amount \n" +
                        "\t\tFROM\n" +
                        "\t\t\tfact_internetsales\n" +
                        "\t\t\tJOIN dim_date ON dim_date.`datekey` = fact_internetsales.`datekey` \n" +
                        "\t\tGROUP BY\n" +
                        "\t\t\tdim_date.fulldatekey \n" +
                        "\t\t) a \n" +
                        "\tORDER BY\n" +
                        "\tfulldatekey \n" +
                        "\t) t");
                break;
            case "QTD":
                stringBuilder.append("SELECT\n" +
                        "\tfulldatekey,\n" +
                        "\tqtd,\n" +
                        "\tlag( qtd, 1, 0 ) over ( ORDER BY fulldatekey ) AS \"qtd_next\" \n" +
                        "FROM\n" +
                        "\t(\n" +
                        "\tSELECT\n" +
                        "\t\tfulldatekey,\n" +
                        "\t\tSUM( sum_amount ) over ( PARTITION BY YEAR ( fulldatekey ), QUARTER ( fulldatekey ) ORDER BY fulldatekey rows BETWEEN UNBOUNDED preceding AND CURRENT ROW ) AS qtd \n" +
                        "\tFROM\n" +
                        "\t\t(\n" +
                        "\t\tSELECT\n" +
                        "\t\t\tdim_date.fulldatekey,\n" +
                        "\t\t\tsum( sum_amount ) AS sum_amount \n" +
                        "\t\tFROM\n" +
                        "\t\t\tfact_internetsales\n" +
                        "\t\t\tJOIN dim_date ON dim_date.`datekey` = fact_internetsales.`datekey` \n" +
                        "\t\tGROUP BY\n" +
                        "\t\t\tdim_date.fulldatekey \n" +
                        "\t\t) a \n" +
                        "\tORDER BY\n" +
                        "\tfulldatekey \n" +
                        "\t) t");
                break;
            case "MTD":
                stringBuilder.append("SELECT\n" +
                        "\tfulldatekey,\n" +
                        "\tmtd,\n" +
                        "\tlag( mtd, 1, 0 ) over ( ORDER BY fulldatekey ) AS \"mtd _next\" \n" +
                        "FROM\n" +
                        "\t(\n" +
                        "\tSELECT\n" +
                        "\t\tfulldatekey,\n" +
                        "\t\tSUM( sum_amount ) over ( PARTITION BY YEAR ( fulldatekey ), MONTH ( fulldatekey ) ORDER BY fulldatekey rows BETWEEN UNBOUNDED preceding AND CURRENT ROW ) AS mtd \n" +
                        "\tFROM\n" +
                        "\t\t(\n" +
                        "\t\tSELECT\n" +
                        "\t\t\tdim_date.fulldatekey,\n" +
                        "\t\t\tsum( sum_amount ) AS sum_amount \n" +
                        "\t\tFROM\n" +
                        "\t\t\tfact_internetsales\n" +
                        "\t\t\tJOIN dim_date ON dim_date.`datekey` = fact_internetsales.`datekey` \n" +
                        "\t\tGROUP BY\n" +
                        "\t\t\tdim_date.fulldatekey \n" +
                        "\t\t) a \n" +
                        "\tORDER BY\n" +
                        "\tfulldatekey \n" +
                        "\t) t");
                break;
            default:
                break;
        }
        return stringBuilder.toString();
    }

    /**
     * YTD/QTD/MTD 去年同期指标分析
     *
     * @param timePeriodType 指标类型
     * @param dimensionFiled 维度字段
     * @return SQL
     */
    public String getDerivedIndexSynchronism(String timePeriodType, String dimensionFiled) {
        StringBuilder stringBuilder = new StringBuilder();
        switch (timePeriodType) {
            case "YTD":
                stringBuilder.append("SELECT\n" +
                        "\tnq.fulldatekey,\n" +
                        "\tnq.YTD AS ytd,\n" +
                        "\ttq.YTD AS 'last_ytd' \n" +
                        "FROM\n" +
                        "\t(\n" +
                        "\tSELECT\n" +
                        "\t\tfulldatekey,\n" +
                        "\t\tSUM( sum_amount ) over ( PARTITION BY YEAR ( fulldatekey ), YEAR ( fulldatekey ) ORDER BY fulldatekey rows BETWEEN UNBOUNDED preceding AND CURRENT ROW ) AS YTD \n" +
                        "\tFROM\n" +
                        "\t\t(\n" +
                        "\t\tSELECT\n" +
                        "\t\t\tdim_date.fulldatekey,\n" +
                        "\t\t\tsum( sum_amount ) AS sum_amount \n" +
                        "\t\tFROM\n" +
                        "\t\t\tfact_internetsales\n" +
                        "\t\t\tJOIN dim_date ON dim_date.`datekey` = fact_internetsales.`datekey` \n" +
                        "\t\tGROUP BY\n" +
                        "\t\t\tdim_date.fulldatekey \n" +
                        "\t\t) a \n" +
                        "\tORDER BY\n" +
                        "\t\tfulldatekey \n" +
                        "\t) nq\n" +
                        "\tLEFT JOIN (\n" +
                        "\tSELECT\n" +
                        "\t\tfulldatekey,\n" +
                        "\t\tSUM( sum_amount ) over ( PARTITION BY YEAR ( fulldatekey ), YEAR ( fulldatekey ) ORDER BY fulldatekey rows BETWEEN UNBOUNDED preceding AND CURRENT ROW ) AS YTD \n" +
                        "\tFROM\n" +
                        "\t\t(\n" +
                        "\t\tSELECT\n" +
                        "\t\t\tdim_date.fulldatekey,\n" +
                        "\t\t\tsum( sum_amount ) AS sum_amount \n" +
                        "\t\tFROM\n" +
                        "\t\t\tfact_internetsales\n" +
                        "\t\t\tJOIN dim_date ON dim_date.`datekey` = fact_internetsales.`datekey` \n" +
                        "\t\tGROUP BY\n" +
                        "\t\t\tdim_date.fulldatekey \n" +
                        "\t\t) a \n" +
                        "\tORDER BY\n" +
                        "\t\tfulldatekey \n" +
                        "\t\t) tq ON tq.fulldatekey = DATE_FORMAT(\n" +
                        "\t\tsubdate( nq.fulldatekey, INTERVAL 1 YEAR ),\n" +
                        "\t\"%Y%m%d\" \n" +
                        "\t)");
                break;
            case "QTD":
                stringBuilder.append("SELECT\n" +
                        "\tnq.fulldatekey,\n" +
                        "\tnq.qtd AS qtd,\n" +
                        "\ttq.qtd AS 'last_qtd' \n" +
                        "FROM\n" +
                        "\t(\n" +
                        "\tSELECT\n" +
                        "\t\tfulldatekey,\n" +
                        "\t\tSUM( sum_amount ) over ( PARTITION BY YEAR ( fulldatekey ), QUARTER ( fulldatekey ) ORDER BY fulldatekey rows BETWEEN UNBOUNDED preceding AND CURRENT ROW ) AS qtd \n" +
                        "\tFROM\n" +
                        "\t\t(\n" +
                        "\t\tSELECT\n" +
                        "\t\t\tdim_date.fulldatekey,\n" +
                        "\t\t\tsum( sum_amount ) AS sum_amount \n" +
                        "\t\tFROM\n" +
                        "\t\t\tfact_internetsales\n" +
                        "\t\t\tJOIN dim_date ON dim_date.`datekey` = fact_internetsales.`datekey` \n" +
                        "\t\tGROUP BY\n" +
                        "\t\t\tdim_date.fulldatekey \n" +
                        "\t\t) a \n" +
                        "\tORDER BY\n" +
                        "\t\tfulldatekey \n" +
                        "\t) nq\n" +
                        "\tLEFT JOIN (\n" +
                        "\tSELECT\n" +
                        "\t\tfulldatekey,\n" +
                        "\t\tSUM( sum_amount ) over ( PARTITION BY YEAR ( fulldatekey ), QUARTER ( fulldatekey ) ORDER BY fulldatekey rows BETWEEN UNBOUNDED preceding AND CURRENT ROW ) AS qtd \n" +
                        "\tFROM\n" +
                        "\t\t(\n" +
                        "\t\tSELECT\n" +
                        "\t\t\tdim_date.fulldatekey,\n" +
                        "\t\t\tsum( sum_amount ) AS sum_amount \n" +
                        "\t\tFROM\n" +
                        "\t\t\tfact_internetsales\n" +
                        "\t\t\tJOIN dim_date ON dim_date.`datekey` = fact_internetsales.`datekey` \n" +
                        "\t\tGROUP BY\n" +
                        "\t\t\tdim_date.fulldatekey \n" +
                        "\t\t) a \n" +
                        "\tORDER BY\n" +
                        "\t\tfulldatekey \n" +
                        "\t\t) tq ON tq.fulldatekey = DATE_FORMAT(\n" +
                        "\t\tsubdate( nq.fulldatekey, INTERVAL 1 YEAR ),\n" +
                        "\t\"%Y%m%d\" \n" +
                        "\t)");
                break;
            case "MTD":
                stringBuilder.append("SELECT\n" +
                        "\tnq.fulldatekey,\n" +
                        "\tnq.mtd AS mtd,\n" +
                        "\ttq.mtd AS 'last_mtd' \n" +
                        "FROM\n" +
                        "\t(\n" +
                        "\tSELECT\n" +
                        "\t\tfulldatekey,\n" +
                        "\t\tSUM( sum_amount ) over ( PARTITION BY YEAR ( fulldatekey ), MONTH ( fulldatekey ) ORDER BY fulldatekey rows BETWEEN UNBOUNDED preceding AND CURRENT ROW ) AS mtd \n" +
                        "\tFROM\n" +
                        "\t\t(\n" +
                        "\t\tSELECT\n" +
                        "\t\t\tdim_date.fulldatekey,\n" +
                        "\t\t\tsum( sum_amount ) AS sum_amount \n" +
                        "\t\tFROM\n" +
                        "\t\t\tfact_internetsales\n" +
                        "\t\t\tJOIN dim_date ON dim_date.`datekey` = fact_internetsales.`datekey` \n" +
                        "\t\tGROUP BY\n" +
                        "\t\t\tdim_date.fulldatekey \n" +
                        "\t\t) a \n" +
                        "\tORDER BY\n" +
                        "\t\tfulldatekey \n" +
                        "\t) nq\n" +
                        "\tLEFT JOIN (\n" +
                        "\tSELECT\n" +
                        "\t\tfulldatekey,\n" +
                        "\t\tSUM( sum_amount ) over ( PARTITION BY YEAR ( fulldatekey ), MONTH ( fulldatekey ) ORDER BY fulldatekey rows BETWEEN UNBOUNDED preceding AND CURRENT ROW ) AS mtd \n" +
                        "\tFROM\n" +
                        "\t\t(\n" +
                        "\t\tSELECT\n" +
                        "\t\t\tdim_date.fulldatekey,\n" +
                        "\t\t\tsum( sum_amount ) AS sum_amount \n" +
                        "\t\tFROM\n" +
                        "\t\t\tfact_internetsales\n" +
                        "\t\t\tJOIN dim_date ON dim_date.`datekey` = fact_internetsales.`datekey` \n" +
                        "\t\tGROUP BY\n" +
                        "\t\t\tdim_date.fulldatekey \n" +
                        "\t\t) a \n" +
                        "\tORDER BY\n" +
                        "\t\tfulldatekey \n" +
                        "\t\t) tq ON tq.fulldatekey = DATE_FORMAT(\n" +
                        "\t\tsubdate( nq.fulldatekey, INTERVAL 1 YEAR ),\n" +
                        "\t\"%Y%m%d\" \n" +
                        "\t)");
                break;
            default:
                break;
        }
        return stringBuilder.toString();
    }

}
