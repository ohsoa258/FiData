package com.fisk.chartvisual.util.dbhelper.buildmdx;

import com.fisk.chartvisual.dto.ChartQueryFilter;
import com.fisk.chartvisual.dto.ColumnDetailsSsas;
import com.fisk.chartvisual.vo.DataServiceResult;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.Position;
import org.olap4j.metadata.Member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 生成表格图形mdx语句
 * @author JinXingWang
 */
public class BuildTableMdx extends BaseBuildMdx {
    @Override
    public String buildWhereMdx(List<ColumnDetailsSsas> values, List<ChartQueryFilter> wheres){
        //where条件中只能包含元组
        if (wheres.size()>0|| values.size()==1){
            StringBuilder whereMdxSb=new StringBuilder();
            whereMdxSb.append("WHERE(");
            if (wheres.size()>0){
                for(ChartQueryFilter where: wheres){
                    String valuesStr= where.value.stream().map(e-> where.columnName+".&["+e+"]").collect(Collectors.joining(" , "," { "," } "));
                    whereMdxSb.append(valuesStr);
                    whereMdxSb.append(",");
                }
            }
            if (values.size()==1){
                whereMdxSb.append(values.stream().map(e->e.uniqueName).collect(Collectors.joining(",")));
            }
            whereMdxSb.append(")");
            return  whereMdxSb.toString();
        }else {
            return "";
        }
    }
    @Override
    public DataServiceResult getDataByAnalyticalCellSet(CellSet cellSet){
        DataServiceResult rs=new DataServiceResult();
        //数据
        List<Map<String,Object>> dataList=new ArrayList<>();
        //列头
        List<Map<String,Object>> columnList=new ArrayList<>();
        Map<String,Object> data=new HashMap<>();
        for (Position column:cellSet.getAxes().get(0)){
            Map<String,Object> columns=new HashMap<>();
            final Cell cell=cellSet.getCell(column);
            StringBuilder columnName=new StringBuilder();
            for (Member member:column.getMembers()){
                columnName.append(member.getName());
                columnName.append("-");
            }
            columnName.deleteCharAt(columnName.length()-1);
            columns.put("name",columnName.toString());
            data.put(columnName.toString(),cell.getValue());
        }
        dataList.add(data);
        rs.data=dataList;
        rs.tableColumn=columnList;
        return rs;
    }
}
