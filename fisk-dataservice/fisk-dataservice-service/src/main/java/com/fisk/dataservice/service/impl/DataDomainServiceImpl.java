package com.fisk.dataservice.service.impl;

import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.client.DimensionClient;
import com.fisk.datamodel.dto.table.TableDataDTO;
import com.fisk.dataservice.dto.DataDoFieldDTO;
import com.fisk.dataservice.service.DataDomainService;
import com.fisk.dataservice.utils.mysql.CreateMysqlUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.fisk.dataservice.enums.DataDoFieldTypeEnum.*;
import static com.fisk.dataservice.enums.DataDoFieldTypeEnum.COLUMN;
import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.joining;

/**
 * @author WangYan
 * @date 2021/8/23 16:38
 */
@Service
public class DataDomainServiceImpl implements DataDomainService {

    @Resource
    private DimensionClient dimensionClient;

    @Override
    public List<Map> query(List<DataDoFieldDTO> apiConfigureFieldList, Integer currentPage, Integer pageSize) {
        // 转义符
        String[] escapeStr = getEscapeStr();

        // 所有列的表名field集合
        List<DataDoFieldDTO> fieldList = apiConfigureFieldList.stream()
                .filter(e -> e.getFieldType().equals(COLUMN))
                .collect(toList());


        ResultEntity<TableDataDTO> resultEntity = dimensionClient.getTableName(155, COLUMN, "year");

        // k fileds v 表名  (列)
        Map<Integer, String> map = apiConfigureFieldList.stream()
                .filter(e -> e.getFieldType().equals(COLUMN))
                .collect(toMap(DataDoFieldDTO::getFieldId,
                        e -> dimensionClient.getTableName(e.getFieldId(), COLUMN, e.getFieldName()).getData().tableName));

        String queryFieldList = apiConfigureFieldList.stream()
                // 查询字段: select列
                // 日期.年,日期.月,维度.金额
                // `date.year`,`date.month`,`dimension.money`
                .filter(e -> e.getFieldType().equals(COLUMN))
                .map(e -> escapeStr[0] + dimensionClient.getTableName(e.getFieldId(),e.getFieldType(),e.getFieldName()) + "." + e.getFieldName()
                        + escapeStr[1])
                .collect(joining(","));

        String conditionList = apiConfigureFieldList.stream()
                // 权限控制字段: where条件
                // `date.year`=`2021` and `date.month`=`3`
                .filter(e -> e.getFieldType().equals(WHERE))
                .map(e -> escapeStr[0] + dimensionClient.getTableName(e.getFieldId(), e.getFieldType(),e.getFieldName()).getData().tableName
                        + "."+ e.getFieldName() + escapeStr[1] + e.getWhere() + escapeStr[0] + e.getWhereValue() + escapeStr[1])
                .collect(joining("AND "));

        String aggregationList = apiConfigureFieldList.stream()
                // 聚合字段: avg()  sum()  max()  min()  count()
                // 如维度金额求和:  sum(`money`)
                .filter(e -> e.getFieldType().equals(VALUE))
                .map(e -> dimensionClient.getAggregation(e.getFieldId()).getData()
                        + "(" + escapeStr[0] + e.getFieldName() + escapeStr[1] + ")")
                .collect(joining(","));

        String groupingList = apiConfigureFieldList.stream()
                // 分组字段: group by
                // 根据年,月,产维度金额分组: `date.year`,`date.month`,`dimension.money`
                .filter(e -> e.getFieldType().equals(COLUMN))
                .map(e -> escapeStr[0] + dimensionClient.getTableName(e.getFieldId(),e.getFieldType(),e.getFieldName())
                        + e.getFieldName() + escapeStr[1])
                .collect(joining(","));
        CreateMysqlUtil createMysqlUtil  = new CreateMysqlUtil();
        List<Map> maps = createMysqlUtil.filterData(apiConfigureFieldList, currentPage, pageSize);
        return maps;
    }

    protected static String[] getEscapeStr() {
        String[] arr = new String[2];
        arr[0] = "`";
        arr[1] = "`";
        return arr;
    }
}
