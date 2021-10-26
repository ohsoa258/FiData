package com.fisk.dataservice.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.SlicerDTO;
import com.fisk.dataservice.dto.InputParameterDTO;
import com.fisk.dataservice.dto.OutParameterDTO;
import com.fisk.dataservice.dto.TableDataDTO;
import com.fisk.dataservice.dto.DataDoFieldDTO;
import com.fisk.dataservice.entity.DimensionAttributePO;
import com.fisk.dataservice.entity.DimensionPO;
import com.fisk.dataservice.entity.FactPO;
import com.fisk.dataservice.entity.IndicatorsPO;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.DataDomainService;
import com.fisk.dataservice.service.ITableName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.fisk.common.constants.AliasConstants.*;
import static com.fisk.dataservice.enums.DataDoFieldTypeEnum.*;
import static com.fisk.dataservice.utils.mysql.MysqlConnect.executeSql;
import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.joining;

/**
 * @author WangYan
 * @date 2021/8/23 16:38
 */
@Slf4j
@Service
public class DataDomainServiceImpl implements DataDomainService {

    @Resource
    private ITableName iTableName;
    @Resource
    private IndicatorsMapper indicatorsMapper;
    @Resource
    private FactMapper factMapper;
    @Resource
    private DataDomainMapper domainMapper;
    @Resource
    private DimensionAttributeMapper attributeMapper;
    @Resource
    private DimensionMapper dimensionMapper;
    @Resource
    private DataDomainService domainService;

    @Override
    public Object query(List<DataDoFieldDTO> apiConfigureFieldList, Integer currentPage, Integer pageSize) {

        try {
            // 转义符
            String[] escapeStr = getEscapeStr();

            // 非维度数据
            List<TableDataDTO> noTableData = apiConfigureFieldList.stream()
                    .filter(e -> e.getDimension() == 0)
                    .map(e -> iTableName.getTableName(e.getFieldId(), e.getFieldType(), e.getFieldName(), e.dimension).getData())
                    .collect(toList());

            // 所有维度的data
            List<TableDataDTO> existTableData = apiConfigureFieldList.stream()
                    .filter(e -> e.getDimension() == 1)
                    .map(e -> iTableName.getTableName(e.getFieldId(), e.getFieldType(), e.getFieldName(), e.dimension).getData())
                    .collect(toList());

            // 维度
            StringBuilder str = new StringBuilder();
            str.append("SELECT ");

            // select
            String queryField = existTableData.stream()
                    .filter(e -> e.getType() == COLUMN)
                    .map(e -> e.getTableName() + "." + escapeStr[0] + e.getTableField() + escapeStr[1])
                    .collect(joining(","));

            // 根据 TableNameKey 进行去重
            ArrayList<TableDataDTO> tableKey = existTableData.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getTableNameKey()))
                    .collect(collectingAndThen(toCollection(() -> new TreeSet<>(Comparator.comparing(U -> U.getTableNameKey()))), ArrayList::new));

            String queryKey = tableKey.stream()
                    .filter(e -> e.getType() == COLUMN)
                    .map(e -> e.getTableName() + "." + escapeStr[0] + e.getTableNameKey() + escapeStr[1])
                    .collect(joining(","));

            // 追加select
            str.append(queryField);

            if (StringUtils.isNotBlank(queryField) && StringUtils.isNotBlank(queryKey)){
                str.append(",");
            }

            // 追加表名_key
            str.append(queryKey);

            // 从表去重表名
            ArrayList<TableDataDTO> joinTableDataList = existTableData.stream()
                    .collect(collectingAndThen
                            (toCollection(() -> new TreeSet<>(Comparator.comparing(TableDataDTO::getTableName))), ArrayList::new));

            // 主表
            str.append(" FROM " + joinTableDataList.get(0).tableName);
            joinTableDataList.remove(0);

            if (joinTableDataList.size() >= 1){
                str.append(" INNER JOIN ");
            }

            String collect = joinTableDataList.stream()
                    .map(e -> e.tableName + " ON 1=1")
                    .collect(joining(" INNER JOIN "));

            str.append(collect);

            String whereField = apiConfigureFieldList.stream()
                    .filter(e -> e.getDimension() == 1 && e.getFieldType() == WHERE)
                    .map(e -> iTableName.getTableName(e.getFieldId(), e.getFieldType(), e.getFieldName(), e.dimension).getData().getTableName()
                            + "." + escapeStr[0] + e.getFieldName() + escapeStr[1] + e.getWhere() + "'" + e.getWhereValue() + "'")
                    .collect(joining());

            // WHERE
            if (StringUtils.isNotBlank(whereField)){
                str.append(" WHERE "+whereField);
            }

            // 只有维度的的情况
            System.err.println("---------------------------------- \n"+str);
            log.info("****************************SQL******************************"+str);

            // 存在非维度
            if (CollectionUtils.isNotEmpty(noTableData)) {
                System.err.println(this.noDimension(noTableData, apiConfigureFieldList, str));
                return this.noDimension(noTableData, apiConfigureFieldList, str);
            }
            System.err.println(executeSql(str.toString(), apiConfigureFieldList).data);
            return executeSql(str.toString(), apiConfigureFieldList);
        }catch (Exception e){
            return ResultEntityBuild.build(ResultEnum.SQL_ERROR);
        }
    }

    /**
     * 存在非维度
     *
     * @param noTableData           非维度数据
     * @param apiConfigureFieldList 拖动字段集合
     * @param str                   维度sql
     */
    public Object noDimension(List<TableDataDTO> noTableData, List<DataDoFieldDTO> apiConfigureFieldList, StringBuilder str) {

        try {
            // 全局sql
            StringBuilder wholeStr = new StringBuilder();

            // 转义符
            String[] escapeStr = getEscapeStr();

            // 所有维度的data
            List<TableDataDTO> tableDataList = apiConfigureFieldList.stream()
                    .filter(e -> e.getDimension() == 1)
                    .map(e -> iTableName.getTableName(e.getFieldId(), e.getFieldType(), e.getFieldName(), e.dimension).getData())
                    .collect(toList());

            String existTableField = tableDataList.stream()
                    .filter(e -> e.getType() == COLUMN)
                    .map(e -> escapeStr[0] + e.getTableField() + escapeStr[1])
                    .collect(joining("," + DIMENSION_ALL_ALIAS_NAME + "."));

            // 非维度名称
            List<TableDataDTO> collect3 = noTableData.stream()
                    .filter(e -> e.getRelationId() != null)
                    .map(e -> {
                        e.setDimensionName(iTableName.getDimensionName(e.getRelationId()).data);
                        return e;
                    }).collect(toList());

            List<Integer> columnFieldIds = apiConfigureFieldList.stream()
                    .filter(e -> e.getFieldType() != VALUE)
                    .map(e -> e.getFieldId())
                    .collect(toList());

            List<Integer> valueFieldIds = apiConfigureFieldList.stream()
                    .filter(e -> e.getFieldType() == VALUE)
                    .map(e -> e.getFieldId())
                    .collect(toList());

            InputParameterDTO inputParameterDTO = new InputParameterDTO();
            inputParameterDTO.setIds(columnFieldIds);
            inputParameterDTO.setIndicatorsIds(valueFieldIds);

            List<OutParameterDTO> relationShip = iTableName.getRelationShip(inputParameterDTO);

            AtomicInteger numll = new AtomicInteger();
            String collect = relationShip.stream()
                    .filter(e -> e.getWhether() == 1)
                    .map(e -> "LEFT JOIN (SELECT * FROM " + e.getFactName() + ") AS " + NO_DIMENSION_ALIAS_NAME + numll.incrementAndGet()
                            + " ON " + DIMENSION_ALL_ALIAS_NAME + "." + e.getDimensionName() + "_key"
                            + "=" + NO_DIMENSION_ALIAS_NAME + numll + "." + e.getDimensionName() + "_key ")
                    .collect(joining(" "));

            String collect5 = relationShip.stream()
                    .filter(e -> e.getWhether() == 0)
                    .map(e -> "LEFT JOIN (SELECT * FROM " + e.getFactName() + ") AS " + NO_DIMENSION_ALIAS_NAME + numll.incrementAndGet()
                            + " ON " + "1 = 1 ")
                    .collect(joining(" "));
            System.out.println(collect);
            System.out.println(collect5);

            // LEFT JOIN 去重表名
            ArrayList<TableDataDTO> leftJoinList = collect3.stream()
                    .collect(collectingAndThen
                            (toCollection(() -> new TreeSet<>(Comparator.comparing(TableDataDTO::getTableName))), ArrayList::new));

            AtomicInteger num = new AtomicInteger();
            String leftJoin = leftJoinList.stream()
                    .map(e -> "LEFT JOIN (SELECT * FROM " + e.getTableName() + ") AS " + NO_DIMENSION_ALIAS_NAME + num.incrementAndGet()
                            + " ON " + "1 = 1 "
                    )
                    .collect(joining(" "));

            String collect1 = noTableData.stream()
                    .filter(e -> e.getRelationId() == null)
                    .map(e -> "LEFT JOIN (SELECT * FROM " + e.getTableName() + ") AS " + NO_DIMENSION_ALIAS_NAME + num.incrementAndGet()
                            + " ON " + "1 = 1 ")
                    .collect(joining(" "));

            int num1 = 0;
            for (TableDataDTO noTableDatum : noTableData) {
                noTableDatum.setAlias(NO_DIMENSION_ALIAS_NAME + ++num1);
            }

            String collect4 = noTableData.stream()
                    .filter(e -> e.getType() == COLUMN)
                    .map(e -> e.getAlias() + "." + escapeStr[0] + e.getTableField() + escapeStr[1])
                    .collect(joining(","));

            // 获取聚合条件
            String aggregation = noTableData.stream()
                    .filter(e -> e.getType() == VALUE)
                    .map(e -> iTableName.getAggregation(e.getId()).getData()
                            + "(" + e.getAlias() + "." + escapeStr[0] + e.getTableField() + escapeStr[1] + ")")
                    .collect(joining(","));

            // SELECT字段
            wholeStr.append("SELECT ");
            this.queryField(collect4,existTableField,wholeStr);

            if (StringUtils.isNotBlank(existTableField) && StringUtils.isNotBlank(aggregation)){
                wholeStr.append(",");
            }

            // 追加聚合条件
            if (StringUtils.isNotBlank(aggregation)){
                wholeStr.append(aggregation);
            }

            wholeStr.append("FROM (" + str + ")" + " AS " + DIMENSION_ALL_ALIAS_NAME + " ");

            // 判断追加 LEFT JOIN 还是笛卡尔积
            if (StringUtils.isBlank(leftJoin)) {
                wholeStr.append(collect1);
            } else {
                wholeStr.append(leftJoin);
            }

            // WHERE
            wholeStr.append("WHERE ");
            String collect2 = noTableData.stream()
                    .map(e -> e.alias + "." + e.tableField + " IS NOT NULL")
                    .collect(joining(" AND "));
            wholeStr.append(collect2);

            // GROUP BY
            if (StringUtils.isNotBlank(aggregation)){
                wholeStr.append(" GROUP BY ");
                wholeStr.append(DIMENSION_ALL_ALIAS_NAME + "." + existTableField);
            }
            System.err.println(wholeStr);
            System.err.println("---------------------------------- \n"+wholeStr);
            log.info("****************************SQL******************************"+wholeStr);

            return executeSql(wholeStr.toString(), apiConfigureFieldList,aggregation);
        }catch (Exception e){
            return ResultEntityBuild.build(ResultEnum.SQL_ERROR);
        }
    }

    /**
     * select字段
     * @param collect4
     * @param existTableField select字段
     * @param wholeStr
     */
    public void queryField(String collect4,String existTableField,StringBuilder wholeStr){
        if (StringUtils.isBlank(collect4)) {
            wholeStr.append(DIMENSION_ALL_ALIAS_NAME + "." + existTableField);
        } else {
            wholeStr.append(DIMENSION_ALL_ALIAS_NAME + "." + existTableField + "," + collect4);
        }
    }

    /**
     * 根据数据源类型获取转义字符
     *
     * @return 转义字符
     */
    protected static String[] getEscapeStr() {
        String[] arr = new String[2];
        arr[0] = "`";
        arr[1] = "`";
        return arr;
    }

    @DS("datamodel")
    @Override
    public Object getSlicer(SlicerDTO dto) {
        // 非维度
        if (dto.getIsDimension() == 0){
            return this.queryNoDimension(dto.getFiledId(), dto.getFiledName());
        }else if (dto.getIsDimension() == 1){
            // 维度
            return this.queryDimension(dto.getFiledId(), dto.getFiledName());
        }

        return null;
    }

    /**
     * 查询非维度数据
     * @param fieldId
     * @param filedName
     * @return
     */
    public Object queryNoDimension(Integer fieldId,String filedName){
        IndicatorsPO indicators = indicatorsMapper.selectById(fieldId);
        if (indicators == null) {
            return null;
        }

        FactPO fact = factMapper.selectById(indicators.getFactId());
        return domainService.executeToSql(filedName,fact.getFactTableEnName());
    }

    @DS("master")
    @Override
    public Object executeToSql(String filedName,String tableName){
        Set<Object> collect = domainMapper.queryData(filedName, tableName).stream().collect(toSet());
        return collect;
    }

    /**
     * 查询维度数据
     * @param fieldId
     * @param filedName
     * @return
     */
    public Object queryDimension(Integer fieldId,String filedName){
        DimensionAttributePO po = attributeMapper.selectById(fieldId);
        if (po == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        DimensionPO dimension = dimensionMapper.selectById(po.getDimensionId());
        return domainService.executeToSql(filedName,dimension.getDimensionTabName());
    }
}
