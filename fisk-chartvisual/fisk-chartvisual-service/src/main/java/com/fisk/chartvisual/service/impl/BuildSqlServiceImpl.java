package com.fisk.chartvisual.service.impl;

import com.fisk.chartvisual.dto.DataDoFieldDTO;
import com.fisk.chartvisual.dto.IndicatorDTO;
import com.fisk.chartvisual.dto.IsDimensionDTO;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.enums.IndicatorTypeEnum;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.service.BuildSqlService;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.atomicindicator.DimensionTimePeriodDTO;
import com.fisk.chartvisual.dto.IndicatorFeignDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.fisk.chartvisual.enums.FieldTypeEnum.COLUMN;
import static com.fisk.chartvisual.enums.IndicatorTypeEnum.ATOMIC_INDICATORS;
import static com.fisk.chartvisual.enums.IndicatorTypeEnum.DERIVED_INDICATORS;
import static com.fisk.chartvisual.util.dbhelper.database.DatabaseConnect.execQueryResultList;
import static com.fisk.chartvisual.util.dbhelper.whitePondbuidsql.AtomicHelper.aliasField;
import static com.fisk.chartvisual.util.dbhelper.whitePondbuidsql.AtomicHelper.*;
import static com.fisk.chartvisual.util.dbhelper.whitePondbuidsql.DeriveHelper.*;

/**
 * @author WangYan
 * @date 2021/12/1 19:51
 */
@Service
public class BuildSqlServiceImpl implements BuildSqlService {

    @Resource
    DataModelClient client;
    @Resource
    DataSourceConMapper dataSourceConMapper;

    public static final String ATOM_BUILDER = "b1";
    public static final String DERIVE_BUILDER = "b2";

    @Override
    public List<Map<String, Object>> query(List<DataDoFieldDTO> apiConfigureFieldList, Integer id) {
        // 创建Sql
        String sql = this.buildSql(apiConfigureFieldList);
        // 获取连接信息
        DataSourceConPO dataSource = dataSourceConMapper.selectById(id);
        // 查询结果集
        List<Map<String, Object>> dataDomainDTOList = execQueryResultList(sql.toLowerCase(),dataSource);
        return dataDomainDTOList;
    }

    public String buildSql(List<DataDoFieldDTO> apiConfigureFieldList) {

        // 转义符
        String[] escapeStr = getEscapeStr();

        // 维度的字段
        String dimColumn = apiConfigureFieldList.stream().filter(e -> e.getDimension() == 1 && e.getFieldType() == COLUMN)
                .map(e -> e.getTableName() + "." + escapeStr[0] + e.getFieldName() + escapeStr[1])
                .collect(Collectors.joining(","));

        // 维度的
        List<DataDoFieldDTO> dimColumnFieldList = apiConfigureFieldList.stream().filter(e -> e.getDimension() == 1 && e.getFieldType() == COLUMN)
                .collect(Collectors.toList());

//        String dimWhere = apiConfigureFieldList.stream().filter(e -> e.getDimension() == 1 && e.getFieldType() == WHERE)
//                .map(e -> e.getTableName() + "." + escapeStr[0] + e.getFieldName() + escapeStr[1] +
//                        escapeStr[2] + e.getWhere() + escapeStr[2] + e.getWhereValue())
//                .collect(Collectors.joining(" AND "));

        // 获取指标
        IndicatorFeignDTO indicatorFeignDTO = new IndicatorFeignDTO();
        indicatorFeignDTO.setIndicatorList(
                apiConfigureFieldList.stream().filter(e -> e.getDimension() == 0)
                        .map(e -> {
                            IndicatorDTO dto = new IndicatorDTO();
                            dto.setId(e.getFieldId());
                            dto.setFieldName(e.getFieldName());
                            dto.setTableName(e.getTableName());
                            return dto;
                        }).collect(Collectors.toList())
        );

        // 获取所有指标
        List<IndicatorDTO> indicatorList = client.getIndicatorsLogic(indicatorFeignDTO).getData();

        StringBuilder str = new StringBuilder();
        // 原子指标数量
        List<IndicatorDTO> count = indicatorList.stream().filter(e -> e != null)
                .filter(e -> e.getType() == ATOMIC_INDICATORS).collect(Collectors.toList());
        // 派生指标数量
        List<IndicatorDTO> deriveCount = indicatorList.stream().filter(e -> e != null)
                .filter(e -> e.getType() == DERIVED_INDICATORS).collect(Collectors.toList());

        // 子查询别名
        AtomicInteger aliasCount = new AtomicInteger(0);
        // 当前第几条子查询,方便后面判断追加 ORDER BY
        AtomicInteger currentNumber = new AtomicInteger(0);

        // 从表根据表名去重
        TreeSet<DataDoFieldDTO> tableNameSet = dimColumnFieldList.stream().collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(c -> c.getTableName()))));
        List<DataDoFieldDTO> tableNameList = tableNameSet.stream().collect(Collectors.toList());

        // 拼接原子指标
        String atom = indicatorList.stream()
                .filter(e -> e.getType() == ATOMIC_INDICATORS)
                .map(e -> {

                    // 判断指标数量是否是大于2,如果大于2存在子查询,不大于2的话不存在
                    Integer isSubQuery = 2;
                    currentNumber.incrementAndGet();
                    StringBuilder stringBuilder = new StringBuilder();
                    if (count.size() >= isSubQuery){
                        stringBuilder.append("(");
                    }

                    // 追加基础原子指标sql
                    StringBuilder stringBuilder1 = this.buildAtomSql(dimColumn, tableNameList, e, escapeStr,e.getType(),null);
                    stringBuilder.append(stringBuilder1);

                    // 存在子查询的情况,进行最外层 SELECT 别名.字段追加 还有分组和排序
                    aliasField(stringBuilder,isSubQuery,count.size(),
                            aliasCount, dimColumnFieldList,
                            escapeStr,dimColumn,currentNumber
                            ,str);
                    return stringBuilder.toString();
                }).collect(Collectors.joining(" JOIN "));

        // 拼接派生指标
        String derive = indicatorList.stream()
                .filter(e -> e.getType() == DERIVED_INDICATORS)
                .map(e -> {

                    // 派生指标别名维度列
                    String deriveDim = apiConfigureFieldList.stream().filter(b -> b.getDimension() == 1 && b.getFieldType() == COLUMN)
                            .map(b -> DERIVE + "." + escapeStr[0] + b.getFieldName() + escapeStr[1])
                            .collect(Collectors.joining(","));

                    // 派生指标sql生成
                    StringBuilder deriveSql = buildDeriveSql(dimColumn, tableNameList, e, e.getId(), apiConfigureFieldList, escapeStr);

                    StringBuilder deriveStr = new StringBuilder();
                    // 判断指标数量是否是大于2,如果大于2存在子查询,不大于2的话不存在
                    Integer isSubQuery = 2;
                    currentNumber.incrementAndGet();
                    if (deriveCount.size() >= isSubQuery){
                        deriveStr.append("(");
                    }

                    deriveStr.append("SELECT ");

                    // 判断是否存在维度列
                    if (StringUtils.isNotBlank(deriveDim)){
                        deriveStr.append(deriveDim + ",");
                    }

                    deriveStr.append( DERIVE + "." + e.getDeriveName());
                    deriveStr.append(" FROM (" + deriveSql + ")" + " AS " + DERIVE);

                    // ORDER BY
                    if (StringUtils.isNotBlank(deriveDim)){
                        deriveStr.append(" WHERE b.id = 1 ");
                        deriveStr.append(" ORDER BY " + deriveDim);
                    }


                    // 存在子查询的情况,进行最外层 SELECT 别名.字段追加 还有分组和排序
                    aliasField(deriveStr,isSubQuery,deriveCount.size(),
                            aliasCount, dimColumnFieldList,
                            escapeStr,dimColumn,currentNumber
                            ,str);
                    return deriveStr;
                }).collect(Collectors.joining(" JOIN "));
        
        return this.splicingIndicatorsSql(apiConfigureFieldList,escapeStr,atom,derive,str);
    }

    /**
     * JOIN ON 字符串拼接
     * @param dimColumnFieldList
     * @param escapeStr
     * @param id
     * @param tableName
     * @return
     */
    public void joinString(List<DataDoFieldDTO> dimColumnFieldList,String[] escapeStr,Integer id,String tableName
            ,StringBuilder stringBuilder
            ,Integer deriveId
            ,IndicatorTypeEnum type){
        // 派生指标一定存在时间周期关系
        if (CollectionUtils.isEmpty(dimColumnFieldList) && type == DERIVED_INDICATORS){
            DimensionTimePeriodDTO data = client.getDimensionDate(deriveId).getData();
            DataDoFieldDTO dto = new DataDoFieldDTO();
            dto.setFieldId((int) data.getFieldId());
            dto.setDimension(1);
            dto.setTableName(data.getDimensionTabName());
            dimColumnFieldList.add(dto);
        }

        String atr = dimColumnFieldList.stream().map(b -> {
            StringBuilder stringBuilder1 = new StringBuilder();
            stringBuilder1.append(b.getTableName() + " ON ");

            IsDimensionDTO dimensionDTO = new IsDimensionDTO();
            dimensionDTO.setDimensionOne(0);
            dimensionDTO.setDimensionTwo(b.getDimension());
            dimensionDTO.setFieldIdOne(id);
            dimensionDTO.setFieldIdTwo(b.getFieldId());
            if (client.isExistAssociate(dimensionDTO).getData()) {
                // 追加字段关联key
                String name = b.getTableName();
                String str1 = name.substring(0, name.indexOf("_"));
                String filedName = name.substring(str1.length() + 1, name.length()) + "key";

                String onSubQuery = b.getTableName() + "." + escapeStr[0] + filedName + escapeStr[1] +
                        "=" + tableName + "." + escapeStr[0] + filedName + escapeStr[1];
                stringBuilder1.append(onSubQuery);
            } else {
                stringBuilder1.append("1 = 1");
            }
            return stringBuilder1;
        }).collect(Collectors.joining(" JOIN "));
        stringBuilder.append(atr);
    }

    /**
     * 根据数据源类型获取转义字符
     *
     * @return 转义字符
     */
    protected static String[] getEscapeStr() {
        String[] arr = new String[3];
        arr[0] = "`";
        arr[1] = "`";
        arr[2] = " ";
        return arr;
    }

    /**
     * 生成基础原子指标sql
     * @param dimColumn
     * @param tableNameList
     * @param dto
     * @param escapeStr
     * @return
     */
    public StringBuilder buildAtomSql(String dimColumn, List<DataDoFieldDTO> tableNameList, IndicatorDTO dto, String[] escapeStr
            , IndicatorTypeEnum type
            , Integer deriveId){
        StringBuilder stringBuilder = new StringBuilder();
        // SELECT
        subQuery(dimColumn,dto,escapeStr,stringBuilder);
        // 查询出子表之间的关系进行JOIN ON
        this.joinString(tableNameList, escapeStr, dto.getId(), dto.getTableName(),stringBuilder,deriveId,type);
        // 筛选器
        filter(stringBuilder,dto,type);
        // 分组和排序
        sqlSort(dimColumn,stringBuilder,type);
        // 判断如果是派生之指标,就变成子查询
        isDerive(stringBuilder,type);
        return stringBuilder;
    }

    /**
     * 派生指标sql生成
     * @param dimColumn
     * @param tableNameList
     * @param indicator
     * @param deriveId
     * @param apiConfigureFieldList
     * @param escapeStr
     * @return
     */
    public StringBuilder buildDeriveSql(String dimColumn,List<DataDoFieldDTO> tableNameList,IndicatorDTO indicator
            ,Integer deriveId
            ,List<DataDoFieldDTO> apiConfigureFieldList
            ,String[] escapeStr){
        DimensionTimePeriodDTO dto = client.getDimensionDate(deriveId).getData();
        StringBuilder deriveStr = new StringBuilder();
        deriveStr.append("SELECT ");

        // 派生指标查询
        StringBuilder deriveInquire = this.deriveInquire(apiConfigureFieldList, deriveId, indicator, escapeStr);

        // 追加基础原子指标sql
        StringBuilder dimColumns = new StringBuilder();
        if (StringUtils.isNotBlank(dimColumn)){
            dimColumns.append(dimColumn + ",");
        }
        dimColumns.append(dto.getDimensionTabName() + "." + dto.getDimensionAttributeField());
        StringBuilder stringBuilder1 = this.buildAtomSql(dimColumns.toString(), tableNameList, indicator, escapeStr,indicator.getType(),deriveId);

        // 追加dateKey
        deriveStr.append(DERIVE_ALIAS + "." + dto.getDimensionAttributeField() + ",");
        deriveStr.append(deriveInquire + " FROM " + stringBuilder1);

        return deriveStr;
    }

    /**
     * 派生指标查询
     * @param apiConfigureFieldList
     * @param deriveId
     * @param indicator
     * @param escapeStr
     */
    public StringBuilder deriveInquire(List<DataDoFieldDTO> apiConfigureFieldList,Integer deriveId,IndicatorDTO indicator,String[] escapeStr){
        // 派生指标别名维度列
        String derive = apiConfigureFieldList.stream().filter(e -> e.getDimension() == 1 && e.getFieldType() == COLUMN)
                .map(e -> DERIVE_ALIAS + "." + escapeStr[0] + e.getFieldName() + escapeStr[1])
                .collect(Collectors.joining(","));

        // 派生指标聚合追加over函数
        String aggregation = indicator.getCalculationLogic() + "(" + DERIVE_ALIAS + "." + escapeStr[0] + indicator.getFieldName() + escapeStr[1] + ")";
        DimensionTimePeriodDTO dto = client.getDimensionDate(deriveId).getData();
        StringBuilder deriveOver = deriveOver(indicator, derive, dto);

        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(derive)){
            stringBuilder.append(derive + ",");
        }
        stringBuilder.append(aggregation + deriveOver);

        return stringBuilder;
    }

    /**
     * 派生和原子指标sql拼接
     * @param apiConfigureFieldList
     * @param escapeStr
     * @param atom
     * @param derive
     * @return
     */
    public String splicingIndicatorsSql(List<DataDoFieldDTO> apiConfigureFieldList,String[] escapeStr
            , String atom,String derive,StringBuilder str){

        // 原子指标和派生指标同时存在
        if (StringUtils.isNotBlank(atom) && StringUtils.isNotBlank(derive)){
            // 原子
            StringBuilder atomBuilder = new StringBuilder(atom);
            atomBuilder.insert(0,"(");
            atomBuilder.insert(atom.length() +1,")" + " AS " + ATOM_BUILDER);
            System.out.println(atomBuilder.toString().toLowerCase());

            // 派生
            StringBuilder deriveBuilder = new StringBuilder(derive);
            deriveBuilder.insert(0,"(");
            deriveBuilder.insert(derive.length() +1,")" + " AS " + DERIVE_BUILDER);
            System.out.println(deriveBuilder.toString().toLowerCase());

            // 维度列
            String collect = apiConfigureFieldList.stream().filter(e -> e.getFieldType() == COLUMN)
                    .map(e -> {
                        String on = ATOM_BUILDER + "." + escapeStr[0] + e.getFieldName() + escapeStr[1] + "=" + DERIVE_BUILDER + "." +
                                escapeStr[2] + e.getFieldName() + escapeStr[2];
                        return on;
                    }).collect(Collectors.joining(" AND "));

            str.append("SELECT * FROM " + atomBuilder + " JOIN " + deriveBuilder);
            if (StringUtils.isNotBlank(collect)){
                str.append(" ON " + collect);
            }
            return str.toString();
        }

        if (StringUtils.isEmpty(atom)){
            str.append(derive);
        }else {
            str.append(atom);
        }

        return str.toString();
    }
}
