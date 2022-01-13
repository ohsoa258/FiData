package com.fisk.dataservice.utils.buildmdx;
import com.fisk.dataservice.dto.datasource.ColumnDetailsSsas;
import com.fisk.dataservice.vo.datasource.DataServiceResult;
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
 * @author dick
 */
public class BuildTableMdx extends BaseBuildMdx {
    /**
     * 生成列mdx语句
     * @param columns 列
     * @param values 值
     * @return mdx语句
     */
    @Override
    public  String buildColumnMdx(List<ColumnDetailsSsas> columns,List<ColumnDetailsSsas> values) {
        return "  NON EMPTY { " +
                values.stream().map(e -> e.uniqueName).collect(Collectors.joining(" , ")) +
                " } ON COLUMNS ";
    }

    /**
     * 获取行mdx语句
     * @param rows 列
     * @return mdx语句
     */
    @Override
    public  String buildRowMdx(List<ColumnDetailsSsas> rows,List<ColumnDetailsSsas> columns){
        return ", NON EMPTY {( " +
                columns.stream().map(e -> e.uniqueName + ".[" + e.name + "].ALLMEMBERS").collect(Collectors.joining(" * ")) +
                " )} DIMENSION PROPERTIES MEMBER_CAPTION, MEMBER_UNIQUE_NAME ON ROWS ";
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
            //行成员
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
            //列成员
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
