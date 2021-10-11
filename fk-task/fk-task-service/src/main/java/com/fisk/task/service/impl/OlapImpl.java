package com.fisk.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamodel.dto.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorFactDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.task.entity.OlapPO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.mapper.OlapMapper;
import com.fisk.task.service.IOlap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 建模
 * @author JinXingWang
 */
@Service
@Slf4j
public class OlapImpl extends ServiceImpl<OlapMapper, OlapPO> implements IOlap {

    @Resource
    OlapMapper mapper;
    /**
     * 生成建模sql(创建指标表sql，创建维度表sql,查询指标表数据sql)
     * @param businessAreaId 业务域id
     * @param dto 业务域维度表以及原子指标
     * @return 生成建模语句
     */
    @Override
    public List<OlapPO> build(int businessAreaId, BusinessAreaGetDataDTO dto) {
        //删除历史数据
        mapper.deleteByBusinessId(businessAreaId);
        //维度表
        List<OlapPO> poList =new ArrayList<>();
        dto.dimensionList.forEach(e->{
            e.tableName=e.tableName.toLowerCase();
            List<String> fileds=e.dto.stream().map(d->" "+d.fieldEnName.toLowerCase()+" ").collect(Collectors.toList());
            fileds.add(" "+e.tableName+"_pk ,fk_doris_increment_code,");
            OlapPO po=new OlapPO();
            po.businessAreaId=businessAreaId;
            po.tableId=e.id;
            po.selectDataSql="SELECT "+fileds.stream().collect(Collectors.joining(","))+" FROM "+e.tableName+"";
            po.tableName=e.tableName;
            po.createTableSql=buildCreateUniqModelSql(e);
            po.type= OlapTableEnum.DIMENSION;
            poList.add(po);
        });
        //指标表
        dto.atomicIndicatorList.forEach(e->{
            OlapPO po =new OlapPO();
            e.factTable=e.factTable.toLowerCase();
            po.businessAreaId=businessAreaId;
            po.tableId=e.factId;
            po.tableName=e.factTable;
            po.createTableSql=buildCreateAggregateModelSql(e);
            po.selectDataSql=buildSelectAggregateModelDataSql(e);
            po.type=OlapTableEnum.KPI;
            poList.add(po);
        });
        saveBatch(poList);
        return poList;
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
        String keyName=dto.tableName+"_pk";
        String sqlUniqueBuild = "ENGINE=OLAP  UNIQUE KEY(`" + keyName + "`,";
        String sqlDistributedBuild = "DISTRIBUTED BY HASH(`" + keyName + "`,";
        sqlFiledBuild.append("`"+keyName + "` VARCHAR(50)  comment " + "'" + keyName + "' ,");
        dto.dto.forEach((l) -> sqlFiledBuild.append("`"+l.fieldEnName.toLowerCase() + "` " + l.fieldType +"("+l.fieldLength+ ") comment " + "'" + l.fieldCnName + "' ,"));
        sqlFiledBuild.append("fk_doris_increment_code VARCHAR(50) comment '数据批量插入标识' )");
        String sqlFiled = sqlFiledBuild.toString();
        String sqlUnique = sqlUniqueBuild;
        sqlUnique = sqlUnique.substring(0, sqlUnique.lastIndexOf(",")) + ") ";
        String sqlDistributed = sqlDistributedBuild;
        sqlDistributed = sqlDistributed.substring(0, sqlDistributed.lastIndexOf(",")) + ") BUCKETS 10 ";
        sql.append(sqlFiled).append(sqlUnique).append(sqlDistributed).append("\n" + " PROPERTIES(\"replication_num\" = \"1\");");
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
        String tableName=dto.factTable.toLowerCase();
        sql.append("CREATE TABLE ");
        sql.append(tableName);
        sql.append(" ( ");
        String keyName=(dto.factTable+"_pk").toLowerCase();
        sql.append("`"+keyName + "` VARCHAR(50)  comment " + "'" + keyName + "' , ");
        aggregateKeys.add(keyName);
        //维度字段
        dto.list.stream().filter(e->e.attributeType==1).forEach(e->{
            sql.append("`"+e.dimensionTableName+"` VARCHAR(50) COMMENT \"\" , ");
            aggregateKeys.add(e.dimensionTableName);
        });
        //聚合字段
        dto.list.stream().filter(e->e.attributeType!=1).forEach(e-> sql.append("`"+e.atomicIndicatorName+"` BIGINT "+e.aggregationLogic+" COMMENT \"\", "));
        sql.deleteCharAt(sql.lastIndexOf(","));
        sql.append(" ) ");
        sql.append(" ENGINE=OLAP ");
        if (aggregateKeys.size()>0){
            String aggregateKeysSql=aggregateKeys.stream().map(e->"`"+e+"`").collect(Collectors.joining(","));
            //排序字段
            sql.append(" AGGREGATE  KEY ("+aggregateKeysSql+") ");
            sql.append(" DISTRIBUTED BY HASH(`"+keyName+"`) BUCKETS 16");
        }

        sql.append(" PROPERTIES(\"replication_num\" = \"1\")");
        return sql.toString();
    }

    /**
     * 生成查询数据sql
     * @param dto 原子指标
     * @return sql
     */
    public String buildSelectAggregateModelDataSql(AtomicIndicatorFactDTO dto){
        StringBuilder sql=new StringBuilder();
        StringBuilder aggregationFunSql=new StringBuilder();
        StringBuilder groupSql=new StringBuilder();
        String tableName=dto.factTable;
        String keyName=tableName+"_pk";
        aggregationFunSql.append(keyName+" , ");
        groupSql.append(keyName+" ,");
        dto.list.forEach(e->{
            if(e.attributeType==0){
                aggregationFunSql.append("COALESCE(");
                aggregationFunSql.append(e.aggregationLogic);
                aggregationFunSql.append("(");
                aggregationFunSql.append(e.aggregatedField.toLowerCase());
                aggregationFunSql.append(") ,0)AS ");
                aggregationFunSql.append(e.atomicIndicatorName.toLowerCase());
                aggregationFunSql.append(" , ");
            }else {
                groupSql.append(""+e.dimensionTableName+"_pk , ");
                aggregationFunSql.append("COALESCE("+e.dimensionTableName+"_pk,'') AS "+e.dimensionTableName.toLowerCase()+" , ");
            }
        });
        if (aggregationFunSql.length()>0){
            aggregationFunSql.deleteCharAt(aggregationFunSql.length()-2);
        }
        if(groupSql.length()>0){
            groupSql.deleteCharAt(groupSql.length()-2);
        }
        sql.append("SELECT ");
        sql.append(aggregationFunSql);
        sql.append(" FROM ");
        sql.append(tableName);
        sql.append(" ");
        if (groupSql.length()>0){
            groupSql.deleteCharAt(groupSql.length()-1);
            sql.append("GROUP BY ");
            sql.append(groupSql);
        }
        return sql.toString();
    }
}
