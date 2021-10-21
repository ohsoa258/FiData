package com.fisk.chartvisual.util.dbhelper.buildmdx;

import com.fisk.chartvisual.vo.DataServiceResult;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.Position;
import org.olap4j.metadata.Member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 矩阵
 * @author JinXingWang
 */
public class BuildMatrixMdx  extends  BaseBuildMdx{

    /**
     * 解析 cellSet
     * @param cellSet 单元格集合
     * @return map集合
     */
    @Override
    public DataServiceResult getDataByAnalyticalCellSet(CellSet cellSet){
        DataServiceResult rs=new DataServiceResult();
        List<Map<String,Object>> mapList=new ArrayList<>();
        for (Position row :cellSet.getAxes().get(1)){
            Map<String,Object> map=new HashMap<>();
            //行标签
            List<Member> rowMembers=row.getMembers();
            for (int i=0;i<rowMembers.size();i++){
                Member member=rowMembers.get(0);
                member.getName();
            }
            for (Position column:cellSet.getAxes().get(0)){
                final Cell cell=cellSet.getCell(column,row);
                List<Member> columnMembers=column.getMembers();
                for (int i=0;i<columnMembers.size();i++){
                    Member member=columnMembers.get(0);
                    member.getName();
                }
            }
            mapList.add(map);
        }
        rs.data=mapList;
        return rs;
    }
}
