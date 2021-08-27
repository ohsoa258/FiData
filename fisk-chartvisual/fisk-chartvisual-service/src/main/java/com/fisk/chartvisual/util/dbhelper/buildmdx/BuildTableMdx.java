package com.fisk.chartvisual.util.dbhelper.buildmdx;

import com.fisk.chartvisual.dto.ChartQueryFilter;
import com.fisk.chartvisual.dto.ColumnDetailsSsas;
import com.fisk.chartvisual.enums.MatrixElemTypeEnum;
import com.fisk.chartvisual.vo.DataServiceResult;
import org.apache.commons.lang.StringUtils;
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
 * 表格
 * @author JinXingWang
 */
public class BuildTableMdx extends BaseBuildMdx {
    @Override
    /**
     * 生成列mdx语句
     * @param columns 列
     * @param values 值
     * @return mdx语句
     */
    public  String buildColumnMdx(List<ColumnDetailsSsas> columns,List<ColumnDetailsSsas> values) {
        StringBuilder mdxColumn=new StringBuilder();
        mdxColumn.append("  NON EMPTY { ");
        mdxColumn.append(values.stream().map(e-> e.uniqueName).collect(Collectors.joining(" , ")));
        mdxColumn.append(" } ON COLUMNS ");
        return mdxColumn.toString();
    }

    @Override
    /**
     * 获取行mdx语句
     * @param rows 列
     * @return mdx语句
     */
    public  String buildRowMdx(List<ColumnDetailsSsas> rows,List<ColumnDetailsSsas> columns){
        StringBuilder mdxRow=new StringBuilder();
        mdxRow.append(", NON EMPTY {( ");
        mdxRow.append(columns.stream().map(e-> e.uniqueName+".["+e.name+"].ALLMEMBERS").collect(Collectors.joining(" * ")));
        mdxRow.append(" )} DIMENSION PROPERTIES MEMBER_CAPTION, MEMBER_UNIQUE_NAME ON ROWS ");
        return mdxRow.toString();
    }


    @Override
    public DataServiceResult getDataByAnalyticalCellSet(CellSet cellSet){
        DataServiceResult rs=new DataServiceResult();
        //数据
        List<Map<String,Object>> dataList=new ArrayList<>();
        //列头
        List<Map<String,Object>> columnList=new ArrayList<>();
        int index=0;
        for (Position row :cellSet.getAxes().get(1)){
            Map<String,Object> map=new HashMap<>();
            for (Member member : row.getMembers()) {
                String columnName= member.getHierarchy().getName();
                String value=member.getName();
                //添加表头
                if (index==0){
                    Map<String,Object> columnHead=new HashMap<>();
                    columnHead.put("name",columnName);
                    columnList.add(columnHead);
                }
                map.put(columnName,value);
            }
            for (Position column:cellSet.getAxes().get(0)){
                final Cell cell=cellSet.getCell(column,row);
                String columnName="";
                for (Member member:column.getMembers()){
                     columnName=member.getName();
                }
                //添加表头
                if (index==0){
                    Map<String,Object> columnHead=new HashMap<>();
                    columnHead.put("name",columnName);
                    columnList.add(columnHead);
                }
                map.put(columnName,cell.getValue());
            }
            index++;
            dataList.add(map);
        }
        rs.tableColumn=columnList;
        rs.data=dataList;
        return rs;
    }
}
