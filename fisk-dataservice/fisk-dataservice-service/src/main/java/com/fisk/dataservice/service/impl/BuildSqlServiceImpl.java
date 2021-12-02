package com.fisk.dataservice.service.impl;

import com.fisk.datamodel.client.DataModelClient;
import com.fisk.dataservice.dto.*;
import com.fisk.dataservice.service.BuildSqlService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fisk.dataservice.enums.DataDoFieldTypeEnum.COLUMN;
import static com.fisk.dataservice.enums.DataDoFieldTypeEnum.WHERE;
import static com.fisk.dataservice.enums.IndicatorTypeEnum.ATOMIC_INDICATORS;
import static com.fisk.dataservice.utils.database.DatabaseConnect.execQueryResultList;

/**
 * @author WangYan
 * @date 2021/12/1 19:51
 */
@Service
public class BuildSqlServiceImpl implements BuildSqlService {

    @Resource
    DataModelClient client;

    @Override
    public Object query(List<DataDoFieldDTO> apiConfigureFieldList) {
        // 创建Sql
        String sql = this.buildSql(apiConfigureFieldList);
        // 查询结果集
        List<Map<String, Object>> dataDomainDTOList = execQueryResultList(sql);
        return dataDomainDTOList;
    }

    public static final String ATOM_ALIAS = "a";

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

        // 拼接原子指标
        List<IndicatorDTO> indicatorList = client.getIndicatorsLogic(indicatorFeignDTO).getData();
        String atom = indicatorList.stream()
                .filter(e -> e.getType() == ATOMIC_INDICATORS)
                .map(e -> {
                    StringBuilder stringBuilder = new StringBuilder();
                    String atr = "SELECT " + dimColumn + ","
                            + e.getCalculationLogic() + "(" + e.getTableName() + "." + escapeStr[0] + e.getFieldName() + escapeStr[1] + ")"
                            + " FROM " + e.getTableName() + " JOIN ";

                    String arr = dimColumnFieldList.stream().map(b -> {
                        StringBuilder stringBuilder1 = new StringBuilder();
                        stringBuilder1.append(b.getTableName() + " ON ");

                        isDimensionDTO dimensionDTO = new isDimensionDTO();
                        dimensionDTO.setDimensionOne(0);
                        dimensionDTO.setDimensionTwo(b.getDimension());
                        dimensionDTO.setFieldIdOne(e.getId());
                        dimensionDTO.setFieldIdTwo(b.getFieldId());
                        if (client.isExistAssociate(dimensionDTO).getData()){
                            // 追加字段关联key
                            String name = b.getTableName();
                            String str1 = name.substring(0, name.indexOf("_"));
                            String filedName = name.substring(str1.length()+1, name.length()) + "_key";

                            String onSubQuery= b.getTableName() + "." + escapeStr[0] + filedName + escapeStr[1] +
                                    "=" + e.getTableName() + "." + escapeStr[0] + filedName + escapeStr[1];
                            stringBuilder1.append(onSubQuery);
                        }else{
                            stringBuilder1.append("1 = 1");
                        }
                        return stringBuilder1;
                    }).collect(Collectors.joining(" JOIN "));

                    stringBuilder.append(atr);
                    stringBuilder.append(arr);
                    stringBuilder.append(" GROUP BY " + dimColumn);
                    return stringBuilder.toString();
                }).collect(Collectors.joining(" JOIN "));



        // 拼接派生指标
//        indicatorList.stream()
//                .filter(e -> e.getType() == DERIVED_INDICATORS)
//                .map(e -> {
//
//                });

        StringBuilder stringBuilder = new StringBuilder();
        return atom;
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
