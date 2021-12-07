package com.fisk.dataservice.service.impl;

import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.atomicindicator.DimensionTimePeriodDTO;
import com.fisk.dataservice.dto.*;
import com.fisk.dataservice.service.BuildSqlService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.fisk.dataservice.enums.DataDoFieldTypeEnum.COLUMN;
import static com.fisk.dataservice.enums.DataDoFieldTypeEnum.WHERE;
import static com.fisk.dataservice.enums.IndicatorTypeEnum.ATOMIC_INDICATORS;
import static com.fisk.dataservice.enums.IndicatorTypeEnum.DERIVED_INDICATORS;
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
        List<Map<String, Object>> dataDomainDTOList = execQueryResultList(sql);
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
        List<IndicatorDTO> count = indicatorList.stream().filter(e -> e.getType() == ATOMIC_INDICATORS).collect(Collectors.toList());
        AtomicInteger aliasCount = new AtomicInteger(0);
        // 拼接原子指标
        String atom = indicatorList.stream()
                .filter(e -> e.getType() == ATOMIC_INDICATORS)
                .map(e -> {

                    // 判断指标数量是否是子查询
                    StringBuilder stringBuilder = new StringBuilder();
                    if (count.size() >= 2){
                        stringBuilder.append("(");
                    }

                    String atr = "SELECT " + dimColumn + ","
                            + e.getCalculationLogic() + "(" + e.getTableName() + "." + escapeStr[0] + e.getFieldName() + escapeStr[1] + ")"
                            + " AS " + e.getFieldName()
                            + " FROM " + e.getTableName() + " JOIN ";

                    String arr = this.joinString(dimColumnFieldList, escapeStr, e.getId(), e.getTableName());

                    // GROUP BY
                    stringBuilder.append(atr);
                    stringBuilder.append(arr);
                    stringBuilder.append(" GROUP BY " + dimColumn);

                    // 子查询 AS
                    if (count.size() >= 2){
                        stringBuilder.append(")");
                        stringBuilder.append(" AS ");
                        String atomAlias = ATOM_ALIAS + aliasCount.incrementAndGet();
                        stringBuilder.append(atomAlias);

                        // 两个子表的JOIN ON条件
                        if (aliasCount.intValue()%2!=1){
                            stringBuilder.append(" ON ");
                            int aliasDec = aliasCount.decrementAndGet();
                            int aliasInc = aliasCount.incrementAndGet();
                            String collect = dimColumnFieldList.stream().map(d -> {
                                String aliasOn = ATOM_ALIAS + aliasDec  + "." + escapeStr[0] + d.getFieldName() + escapeStr[1] + "=" +
                                        ATOM_ALIAS + aliasInc + "." + escapeStr[0] + d.getFieldName() + escapeStr[1];
                                return aliasOn;
                            }).collect(Collectors.joining(" AND "));

                            stringBuilder.append(collect);

                            // SELECT 最外层
                            String collect1 = dimColumnFieldList.stream().map(d -> {
                                String aliasOn = ATOM_ALIAS + aliasDec + "." + escapeStr[0] + d.getFieldName() + escapeStr[0];
                                return aliasOn;
                            }).collect(Collectors.joining(","));

                            AtomicInteger aliasDec1 = new AtomicInteger();
                            String collect2 = indicatorList.stream()
                                    .filter(c -> c.getType() == ATOMIC_INDICATORS)
                                    .map(c -> ATOM_ALIAS + aliasDec1.incrementAndGet() + "." + escapeStr[0] + c.getFieldName() + escapeStr[1])
                                    .collect(Collectors.joining(","));
                            String s = "SELECT " + collect1 + "," + collect2 + " FROM ";
                            str.insert(0,s);
                        }
                    }
                    return stringBuilder.toString();
                }).collect(Collectors.joining(" JOIN "));

        // 拼接派生指标
        String derive = indicatorList.stream()
                .filter(e -> e.getType() == DERIVED_INDICATORS)
                .map(e -> {

                    StringBuilder str1 = new StringBuilder();

                    // 子查询
                    StringBuilder stringBuilder = new StringBuilder();
                    int frequency = 0;

                    // SELECT时间周期
                    DimensionTimePeriodDTO dto = client.getDimensionDate(e.getId()).getData();
                    String atr = "(SELECT " + dimColumn + ","
                            + e.getCalculationLogic() + "(" + e.getTableName() + "." + escapeStr[0] + e.getFieldName() + escapeStr[1] + ")"
                            + " AS " + e.getFieldName()
                            + "," + dto.getDimensionTabName() + "." + dto.getDimensionAttributeField()
                            + " FROM " + e.getTableName() + " JOIN ";

                    String arr = this.joinString(dimColumnFieldList, escapeStr, e.getId(), e.getTableName());

                    // SELECT
                    stringBuilder.append(atr);
                    stringBuilder.append(arr);

                    // WHERE
                    if (!StringUtils.isEmpty(e.getWhereTimeLogic())){
                        stringBuilder.append(" WHERE " + e.getWhereTimeLogic());
                    }

                    // GROUP BY
                    stringBuilder.append(" GROUP BY " + dimColumn + "," + dto.getDimensionTabName() + "." + dto.getDimensionAttributeField() + ")");

                    String alias = stringBuilder + " AS " + ATOM_ALIAS;
                    // 第一个子查询别名
                    int i = ++frequency;
                    int i1 = ++frequency;
                    str1.append(" FROM " + alias + i);
                    str1.append(" JOIN " + alias + i1);
                    str1.append(" ON ");

                    // 判断时间周期
                    String timePeriod = null;
                    if (e.getTimePeriod().equals("YTD")){
                        timePeriod = "'%y'";
                    }else if (e.getTimePeriod().equals("MTD")){
                        timePeriod = "'%m'";
                    }else if (e.getTimePeriod().equals("QTD")){
                        timePeriod = "'%q'";
                    }

                    str1.append("DATE_FORMAT("+ ATOM_ALIAS + i + "." + dto.getDimensionAttributeField() + "," + timePeriod +" )>=");
                    str1.append("DATE_FORMAT("+ ATOM_ALIAS + i1 + "." + dto.getDimensionAttributeField()+ "," + timePeriod +")" + " AND ");
                    str1.append(dimColumnFieldList.stream().map(d -> {
                        String aliasOn = ATOM_ALIAS + i + "." + escapeStr[0] + d.getFieldName() + escapeStr[0] + "="
                                + ATOM_ALIAS + i1 + "." + escapeStr[0] + d.getFieldName() + escapeStr[0];
                        return aliasOn;
                    }).collect(Collectors.joining(" AND ")));

                    // SELECT 最外层
                    String collect1 = dimColumnFieldList.stream().map(d -> {
                        String aliasOn = ATOM_ALIAS + i + "." + escapeStr[0] + d.getFieldName() + escapeStr[0];
                        return aliasOn;
                    }).collect(Collectors.joining(","));

                    str1.insert(0,"SELECT " + collect1 + ","
                            + e.getCalculationLogic() + "(" + ATOM_ALIAS + frequency + "." + escapeStr[0] + e.getFieldName() + escapeStr[1] + ")"
                            + " AS " + e.getDeriveName());

                    // GROUP BY a1.a1.`year`,a1.`product_class
                    str1.append(" GROUP BY " + collect1);

                    // ORDER BY
                    str1.append(" ORDER BY " + collect1);
                    return str1.toString();
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
    public String joinString(List<DataDoFieldDTO> dimColumnFieldList,String[] escapeStr,Integer id,String tableName){
        return dimColumnFieldList.stream().map(b -> {
            StringBuilder stringBuilder1 = new StringBuilder();
            stringBuilder1.append(b.getTableName() + " ON ");

            isDimensionDTO dimensionDTO = new isDimensionDTO();
            dimensionDTO.setDimensionOne(0);
            dimensionDTO.setDimensionTwo(b.getDimension());
            dimensionDTO.setFieldIdOne(id);
            dimensionDTO.setFieldIdTwo(b.getFieldId());
            if (client.isExistAssociate(dimensionDTO).getData()){
                // 追加字段关联key
                String name = b.getTableName();
                String str1 = name.substring(0, name.indexOf("_"));
                String filedName = name.substring(str1.length()+1, name.length()) + "_key";

                String onSubQuery= b.getTableName() + "." + escapeStr[0] + filedName + escapeStr[1] +
                        "=" + tableName + "." + escapeStr[0] + filedName + escapeStr[1];
                stringBuilder1.append(onSubQuery);
            }else{
                stringBuilder1.append("1 = 1");
            }
            return stringBuilder1;
        }).collect(Collectors.joining(" JOIN "));
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
}
