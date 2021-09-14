package com.fisk.task.service.impl;

import com.fisk.datamodel.dto.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorFactDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.task.entity.OlapDimensionPO;
import com.fisk.task.entity.OlapKpiPO;
import com.fisk.task.service.IOlap;
import com.fisk.task.service.IOlapDimension;
import com.fisk.task.service.IOlapKpi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 建模
 * @author JinXingWang
 */
@Service
@Slf4j
public class OlapImpl implements IOlap {

    @Resource
    IOlapDimension olapDimension;
    @Resource
    IOlapKpi olapKpi;
    /**
     * 生成建模sql(创建指标表sql，创建维度表sql,查询指标表数据sql)
     * @param businessAreaId 业务域id
     * @param dto 业务域维度表以及原子指标
     * @return 生成建模语句
     */
    @Override
    public boolean build(int businessAreaId, BusinessAreaGetDataDTO dto) {
        //删除历史数据
        olapDimension.deleteByBusinessAreaId(businessAreaId);
        olapKpi.deleteByBusinessAreaId(businessAreaId);
        //维度表
        List<OlapDimensionPO> olapDimensionPos =new ArrayList<>();
        dto.dimensionList.forEach(e->{
            OlapDimensionPO olapDimensionPo=new OlapDimensionPO();
            olapDimensionPo.businessAreaId=businessAreaId;
            olapDimensionPo.selectDimensionDataSql="SELECT * FROM "+e.tableName+"";
            olapDimensionPo.dimensionTableName=e.tableName;
            olapDimensionPo.createDimensionTableSql=buildCreateUniqModelSql(e);
            olapDimensionPos.add(olapDimensionPo);
        });
        olapDimension.batchAdd(olapDimensionPos);
        //指标表
        List<OlapKpiPO> olapKpiPoS=new ArrayList<>();
        dto.atomicIndicatorList.forEach(e->{
            OlapKpiPO olapKpiPo =new OlapKpiPO();
            olapKpiPo.businessAreaId=businessAreaId;
            olapKpiPo.kpiTableName=e.factTable;
            olapKpiPo.createKpiTableSql=buildCreateAggregateModelSql(e);
            olapKpiPo.selectKpiDataSql=buildSelectAggregateModelDataSql(e);
            olapKpiPoS.add(olapKpiPo);
        });
        olapKpi.batchAdd(olapKpiPoS);
        return true;
    }

    /**
     * 生成创建主键模型sql
     * @param dto 维度表信息
     * @return sql
     */
    public String buildCreateUniqModelSql(ModelMetaDataDTO dto){
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ");
        sql.append(dto.tableName);
        sql.append("(");
        StringBuilder sqlFiledBuild = new StringBuilder();
        //主键
        String keyName=dto.tableName+"_key";
        String sqlUniqueBuild = "ENGINE=OLAP  UNIQUE KEY(" + keyName + ",";
        String sqlDistributedBuild = "DISTRIBUTED BY HASH(" + keyName + ",";
        sqlFiledBuild.append(keyName + " VARCHAR(50)  comment " + "'" + keyName + "' ,");
        dto.dto.forEach((l) -> sqlFiledBuild.append(l.fieldEnName + " " + l.fieldType + " comment " + "'" + l.fieldCnName + "' ,"));
        sqlFiledBuild.append("fk_doris_increment_code VARCHAR(50) comment '数据批量插入标识' )");
        String sqlFiled = sqlFiledBuild.toString();
        String sqlUnique = sqlUniqueBuild;
        sqlUnique = sqlUnique.substring(0, sqlUnique.lastIndexOf(",")) + ")";
        String sqlDistributed = sqlDistributedBuild;
        sqlDistributed = sqlDistributed.substring(0, sqlDistributed.lastIndexOf(",")) + ") BUCKETS 10";
        sql.append(sqlFiled).append(sqlUnique).append(sqlDistributed).append("\n" + "PROPERTIES(\"replication_num\" = \"1\");");
        return sql.toString();
    }

    /**
     * 生成创建聚合模型sql
     * @param dto 原子指标
     * @return 聚合模型sql
     */
    public String buildCreateAggregateModelSql(AtomicIndicatorFactDTO dto){
        StringBuilder sql=new StringBuilder();
        //聚合key
        List<String> aggregateKeys=new ArrayList<>();
        sql.append("CREATE TABLE ");
        sql.append(dto.factTable);
        sql.append(" ( ");
        //维度字段
        dto.list.stream().filter(e->e.attributeType!=1).forEach(e->{
            sql.append("`"+e.dimensionTableName+"` VARCHAR(50) COMMENT \"\", \n");
            aggregateKeys.add(e.dimensionTableName);
        });
        //聚合字段
        dto.list.stream().filter(e->e.attributeType==1).forEach(e-> sql.append("`"+e.atomicIndicatorName+"` INT "+e.aggregationLogic+" COMMENT \"\", "));

        sql.append(" ) ");
        if (aggregateKeys.size()>0){
            String aggregateKeysSql=aggregateKeys.stream().map(e->"`"+e+"`").collect(Collectors.joining(","));
            //排序字段
            sql.append(" DUPLICATE KEY ("+aggregateKeysSql+") ");
            sql.append(" DISTRIBUTED BY HASH("+aggregateKeysSql+") BUCKETS 10");
            sql.append(" PROPERTIES(\"replication_num\" = \"1\")");
        }
        return sql.toString();
    }

    /**
     * 生成查询聚合模型数据sql
     * @param dto 原子指标
     * @return sql
     */
    public String buildSelectAggregateModelDataSql(AtomicIndicatorFactDTO dto){
        StringBuilder sql=new StringBuilder();
        StringBuilder aggregationFunSql=new StringBuilder();
        StringBuilder groupSql=new StringBuilder();
        dto.list.forEach(e->{
            if(e.attributeType==0){
                aggregationFunSql.append(e.aggregationLogic);
                aggregationFunSql.append("(");
                aggregationFunSql.append(e.aggregatedField);
                aggregationFunSql.append(") AS ");
                aggregationFunSql.append(e.atomicIndicatorName);
                aggregationFunSql.append(" ,");
            }else {
                groupSql.append("`"+e.dimensionTableName+"_key` , ");
                aggregationFunSql.append("`"+e.dimensionTableName+"_key` AS `"+e.dimensionTableName+"` , ");
            }
        });
        if (aggregationFunSql.length()>0){
            aggregationFunSql.deleteCharAt(aggregationFunSql.length()-1);
        }
        if(groupSql.length()>0){
            groupSql.deleteCharAt(groupSql.length()-1);
        }
        sql.append("SELECT ");
        sql.append(aggregationFunSql);
        sql.append(" FROM `");
        sql.append(dto.factTable);
        sql.append("` ");
        if (groupSql.length()>0){
            groupSql.deleteCharAt(groupSql.length()-1);
            sql.append("GROUP BY ");
            sql.append(groupSql);
        }
        return sql.toString();
    }
}
