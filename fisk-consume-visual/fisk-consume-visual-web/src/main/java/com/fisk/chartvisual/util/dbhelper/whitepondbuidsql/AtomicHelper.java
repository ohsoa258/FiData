package com.fisk.chartvisual.util.dbhelper.whitepondbuidsql;

import com.fisk.chartvisual.dto.DataDoFieldDTO;
import com.fisk.chartvisual.dto.IndicatorDTO;
import com.fisk.chartvisual.enums.IndicatorTypeEnum;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.fisk.chartvisual.enums.IndicatorTypeEnum.ATOMIC_INDICATORS;

/**
 * @author WangYan
 * @date 2021/12/23 10:27
 */
public class AtomicHelper {

    public static final String ATOM_ALIAS = "a";

    /**
     * 原子指标拼接 SELECT 查询
     * @param dimColumn
     * @param dto
     * @param escapeStr
     * @return
     */
    public static void subQuery(String dimColumn, IndicatorDTO dto, String[] escapeStr, StringBuilder stringBuilder){
        stringBuilder.append("SELECT ");
        if (!StringUtils.isEmpty(dimColumn)) {
            stringBuilder.append(dimColumn + ",");
        }
        String atr = dto.getCalculationLogic() + "(" + dto.getTableName() + "." + escapeStr[0] + dto.getFieldName() + escapeStr[1] + ")"
                + " AS " + dto.getFieldName()
                + " FROM " + dto.getTableName();
        stringBuilder.append(atr);
        // 没有维度字段,不存在关系,不需要JOIN
        if (!StringUtils.isEmpty(dimColumn)){
            stringBuilder.append(" JOIN ");
        }
    }


    /**
     * sql进行排序和分组
     */
    public static void sqlSort(String dimColumn, StringBuilder stringBuilder, IndicatorTypeEnum type){
        if (!StringUtils.isEmpty(dimColumn)){
            stringBuilder.append(" GROUP BY " + dimColumn);
            if (type == ATOMIC_INDICATORS){
                stringBuilder.append(" ORDER BY " + dimColumn);
            }
        }
    }

    /**
     * 子查询 AS 的时候也在select前面追加 别名.字段
     * @param stringBuilder
     * @param isSubQuery
     * @param count
     * @param aliasCount
     * @param dimColumnFieldList
     * @param escapeStr
     * @param currentNumber
     */
    public static void aliasField(StringBuilder stringBuilder, Integer isSubQuery, Integer count,
                                  AtomicInteger aliasCount, List<DataDoFieldDTO> dimColumnFieldList,
                                  String[] escapeStr, String dimColumn, AtomicInteger currentNumber,StringBuilder str){
        if (count >= isSubQuery){
            stringBuilder.append(")");
            stringBuilder.append(" AS ");
            String atomAlias = ATOM_ALIAS + aliasCount.incrementAndGet();
            stringBuilder.append(atomAlias);

            // 两个子表的JOIN ON条件
            if (aliasCount.intValue() > 1){
                // 维度列不存在,两个子查询不需要 ON 条件
                if (!StringUtils.isEmpty(dimColumn)){
                    stringBuilder.append(" ON ");
                }

                int aliasDec = aliasCount.decrementAndGet();
                int aliasInc = aliasCount.incrementAndGet();
                String aliasOn = dimColumnFieldList.stream().map(d -> {
                    String collect = ATOM_ALIAS + aliasDec  + "." + escapeStr[0] + d.getFieldName() + escapeStr[1] + "=" +
                            ATOM_ALIAS + aliasInc + "." + escapeStr[0] + d.getFieldName() + escapeStr[1];
                    return collect;
                }).collect(Collectors.joining(" AND "));

                stringBuilder.append(aliasOn);

                // sql最外层进行ORDER BY
                if (count.equals(currentNumber.intValue())){
                    dimeSort(dimColumnFieldList,aliasDec,stringBuilder,escapeStr);
                    // sql最外层追加SELECT
                    if (count >= isSubQuery) {
                        str.insert(0,"SELECT * FROM ");
                    }
                }
            }
        }
    }

    /**
     * sql存在子查询的时候,在SELECT最外层追加需要查询的字段
     * @param dimColumnFieldList
     * @param indicatorList
     * @param aliasDec
     * @param str
     * @param escapeStr
     */
    public static void queryAlias(List<DataDoFieldDTO> dimColumnFieldList, List<IndicatorDTO> indicatorList,int aliasDec,
                           StringBuilder str,String[] escapeStr){
        // SELECT 最外层
        String dimensionField = dimColumnFieldList.stream().map(d -> {
            String aliasOn = ATOM_ALIAS + aliasDec + "." + escapeStr[0] + d.getFieldName() + escapeStr[0];
            return aliasOn;
        }).collect(Collectors.joining(","));

        AtomicInteger aliasDec1 = new AtomicInteger();
        String aggregationField = indicatorList.stream()
                .filter(c -> c.getType() == ATOMIC_INDICATORS)
                .map(c -> ATOM_ALIAS + aliasDec1.incrementAndGet() + "." + escapeStr[0] + c.getFieldName() + escapeStr[1])
                .collect(Collectors.joining(","));

        StringBuilder atr = new StringBuilder();
        atr.append("SELECT ");
        if (!StringUtils.isEmpty(dimensionField)){
            atr.append(dimensionField);
        }
        if (!StringUtils.isEmpty(dimensionField) && !StringUtils.isEmpty(aggregationField)){
            atr.append(",");
        }
        if (!StringUtils.isEmpty(aggregationField)){
            atr.append(aggregationField);
        }

        atr.append(" FROM ");
        str.insert(0,atr);
    }

    /**
     * 当存在两个原子指标时,最外层ORDER BY.
     * 根据维度列排序
     * @param dimColumnFieldList
     * @param stringBuilder
     */
    public static void dimeSort(List<DataDoFieldDTO> dimColumnFieldList,int aliasDec,
                         StringBuilder stringBuilder,
                         String[] escapeStr){
        String dimeLines = dimColumnFieldList.stream().map(e -> {
            String dimeLine = ATOM_ALIAS + aliasDec + "." + escapeStr[0] + e.getFieldName() + escapeStr[1];
            return dimeLine;
        }).collect(Collectors.joining(","));
        if (!StringUtils.isEmpty(dimeLines)){
            stringBuilder.append(" ORDER BY " + dimeLines);
        }
    }
}
