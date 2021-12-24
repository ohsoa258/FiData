package com.fisk.dataservice.service.impl;

import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.atomicindicator.DimensionTimePeriodDTO;
import com.fisk.dataservice.dto.*;
import com.fisk.dataservice.enums.IndicatorTypeEnum;
import com.fisk.dataservice.service.BuildSqlService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.fisk.dataservice.enums.DataDoFieldTypeEnum.*;
import static com.fisk.dataservice.enums.IndicatorTypeEnum.ATOMIC_INDICATORS;
import static com.fisk.dataservice.enums.IndicatorTypeEnum.DERIVED_INDICATORS;
import static com.fisk.dataservice.utils.buidsql.AtomicHelper.*;
import static com.fisk.dataservice.utils.buidsql.DeriveHelper.*;
import static com.fisk.dataservice.utils.database.DatabaseConnect.execQueryResultList;

/**
 * @author WangYan
 * @date 2021/12/1 19:51
 */
@Service
public class BuildSqlServiceImpl implements BuildSqlService {

    @Resource
    DataModelClient client;

    public static final String ATOM_ALIAS = "a";

    @Override
    public Object query(List<DataDoFieldDTO> apiConfigureFieldList) {
        // 创建Sql
        String sql = this.buildSql(apiConfigureFieldList);
        // 查询结果集
        List<Map<String, Object>> dataDomainDTOList = execQueryResultList(sql.toLowerCase());
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

        String dimWhere = apiConfigureFieldList.stream().filter(e -> e.getDimension() == 1 && e.getFieldType() == WHERE)
                .map(e -> e.getTableName() + "." + escapeStr[0] + e.getFieldName() + escapeStr[1] +
                        escapeStr[2] + e.getWhere() + escapeStr[2] + e.getWhereValue())
                .collect(Collectors.joining(" AND "));

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
        List<IndicatorDTO> count = indicatorList.stream().filter(e -> e != null)
                .filter(e -> e.getType() == ATOMIC_INDICATORS).collect(Collectors.toList());
        AtomicInteger aliasCount = new AtomicInteger(0);

        // 从表根据表名去重
        TreeSet<DataDoFieldDTO> tableNameSet = dimColumnFieldList.stream().collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(c -> c.getTableName()))));
        List<DataDoFieldDTO> tableNameList = tableNameSet.stream().collect(Collectors.toList());

        // 拼接原子指标
        String atom = indicatorList.stream()
                .filter(e -> e.getType() == ATOMIC_INDICATORS)
                .map(e -> {

                    // 判断指标数量是否是大于2,如果大于2存在子查询,不大于2的话不存在
                    Integer isSubQuery = 2;
                    StringBuilder stringBuilder = new StringBuilder();
                    if (count.size() >= isSubQuery){
                        stringBuilder.append("(");
                    }

                    // 追加基础原子指标sql
                    StringBuilder stringBuilder1 = this.buildAtomSql(dimColumn, tableNameList, e, escapeStr,e.getType());
                    stringBuilder.append(stringBuilder1);

                    // 存在子查询的情况,进行最外层 SELECT 别名.字段追加 还有分组和排序
                    aliasField(stringBuilder,isSubQuery,count.size(),
                            aliasCount, dimColumnFieldList, indicatorList,
                            str,escapeStr,dimColumn);
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
                    deriveStr.append("SELECT ");
                    deriveStr.append(deriveDim + "," + DERIVE + "." + e.getDeriveName());
                    deriveStr.append(" FROM (" + deriveSql + ")" + " AS " + DERIVE);
                    deriveStr.append(" WHERE b.id = 1 ");
                    // ORDER BY
                    deriveStr.append(" ORDER BY " + deriveDim);
                    return deriveStr;
                }).collect(Collectors.joining(","));

        if (StringUtils.isEmpty(atom)){
            str.append(derive);
        }else {
            str.append(atom);
        }
        
        return str.toString();
    }

    /**
     * JOIN ON 字符串拼接
     * @param dimColumnFieldList
     * @param escapeStr
     * @param id
     * @param tableName
     * @return
     */
    public void joinString(List<DataDoFieldDTO> dimColumnFieldList,String[] escapeStr,Integer id,String tableName,StringBuilder stringBuilder){
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
    public StringBuilder buildAtomSql(String dimColumn, List<DataDoFieldDTO> tableNameList, IndicatorDTO dto, String[] escapeStr, IndicatorTypeEnum type){
        StringBuilder stringBuilder = new StringBuilder();
        // SELECT
        subQuery(dimColumn,dto,escapeStr,stringBuilder);
        // 查询出子表之间的关系进行JOIN ON
        this.joinString(tableNameList, escapeStr, dto.getId(), dto.getTableName(),stringBuilder);
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
        String dimColumns = dimColumn + "," + dto.getDimensionTabName() + "." + dto.getDimensionAttributeField();
        StringBuilder stringBuilder1 = this.buildAtomSql(dimColumns, tableNameList, indicator, escapeStr,indicator.getType());

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
}
