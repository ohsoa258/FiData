package com.fisk.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamodel.dto.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorFactAttributeDTO;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorFactDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.ModelAttributeMetaDataDTO;
import com.fisk.task.entity.OlapPO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.mapper.OlapMapper;
import com.fisk.task.service.IDorisBuild;
import com.fisk.task.service.IOlap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
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
    @Resource
    IDorisBuild doris;
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
            List<String> correlationFileds=e.dto.stream().map(d->" "+d.associationTable+" ").collect(Collectors.toList());
            correlationFileds.removeAll(Collections.singleton(" null "));
            correlationFileds.stream().distinct();
            fileds.add(" "+e.tableName.substring(4)+"key ,");
            OlapPO po=new OlapPO();
            po.businessAreaId=businessAreaId;
            String selectSql="SELECT "+fileds.stream().collect(Collectors.joining(","));
            selectSql+=correlationFileds.stream().collect(Collectors.joining(","));
            selectSql=selectSql.substring(0,selectSql.length()-1);
            po.selectDataSql=selectSql+" FROM external_"+e.tableName+"";
            po.tableName=e.tableName;
            po.createTableSql=buildCreateUniqModelSql(e);
            po.type= OlapTableEnum.DIMENSION;
            po.tableId=e.id;
            poList.add(po);
        });
        //指标表
        dto.atomicIndicatorList.forEach(e->{
            OlapPO po =new OlapPO();
            e.factTable=e.factTable.toLowerCase();
            po.businessAreaId=businessAreaId;
            po.tableName=e.factTable;
            po.createTableSql=buildCreateAggregateModelSql(e);
            po.selectDataSql=buildSelectAggregateModelDataSql(e);
            po.type=OlapTableEnum.KPI;
            po.tableId=e.factId;
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
        createExternalTable2(dto);
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ");
        sql.append(dto.tableName);
        sql.append("(");
        StringBuilder sqlFiledBuild = new StringBuilder();
        //主键
        String keyName=dto.tableName.substring(4)+"key";
        String sqlUniqueBuild = "ENGINE=OLAP  UNIQUE KEY(`" + keyName + "`,";
        String sqlDistributedBuild = "DISTRIBUTED BY HASH(`" + keyName + "`,";
        sqlFiledBuild.append("`"+keyName + "` VARCHAR(50)  comment " + "'" + keyName + "' ,");
        dto.dto.forEach((l) -> sqlFiledBuild.append("`"+l.fieldEnName.toLowerCase() + "` " + l.fieldType +"("+l.fieldLength+ ") comment " + "'" + l.fieldCnName + "' ,"));
        //TODO  问题一
        List<ModelAttributeMetaDataDTO> dto1 = dto.dto;
        List<String> fileds=dto1.stream().map(d->" "+d.associationTable+" ").collect(Collectors.toList());
        fileds.removeAll(Collections.singleton(" null "));
        fileds.stream().distinct();
        fileds.forEach((l) -> sqlFiledBuild.append("`"+l.toLowerCase() + "' varchar(50) ,"));
        String sqlFiled = sqlFiledBuild.toString();
        sqlFiled=sqlFiled.substring(0,sqlFiled.length()-1)+")";
        String sqlUnique = sqlUniqueBuild;
        sqlUnique = sqlUnique.substring(0, sqlUnique.lastIndexOf(",")) + ") ";
        String sqlDistributed = sqlDistributedBuild;
        sqlDistributed = sqlDistributed.substring(0, sqlDistributed.lastIndexOf(",")) + ") BUCKETS 10 ";
        sql.append(sqlFiled).append(sqlUnique).append(sqlDistributed).append("\n" + " PROPERTIES(\"replication_num\" = \"1\");");
        return sql.toString();
    }

    public String createExternalTable2(ModelMetaDataDTO dto){
        List<ModelAttributeMetaDataDTO> dto1 = dto.dto;
        String tableName="external_"+dto.tableName;
        String sql="drop table if exists "+tableName+";\n";
        sql+="CREATE EXTERNAL TABLE "+tableName+" ( "+dto.tableName.substring(4)+"key varchar(50),";
        for (ModelAttributeMetaDataDTO modelAttributeMetaDataDTO:dto1) {
            sql+=modelAttributeMetaDataDTO.fieldEnName+" "+modelAttributeMetaDataDTO.fieldType+",";
        }
        //TODO 问题二
        List<String> associationKeys = dto1.stream().map(d -> " " + d.associationTable + " ").collect(Collectors.toList());
        associationKeys.removeAll(Collections.singleton(" null "));
        associationKeys.stream().distinct();
        for (String associationKey:associationKeys) {
            sql+=associationKey.substring(5,associationKey.length()-1)+"key varchar(50),";
        }

        sql=sql.substring(0,sql.length()-1)+")\n" + "ENGINE=ODBC\nPROPERTIES\n";
        sql+="(\"host\" = \"192.168.1.250\",\"port\" = \"5432\",\"user\" = \"postgres\",\"password\" = \"Password01!\",\"database\" = \"dmp_dw\",\"table\" = \"KKKKK\",\"driver\" = \"PostgreSQL\",\"odbc_type\" = \"postgresql\");";
        sql=sql.replace("KKKKK",dto.tableName);
        log.info("维度外部表建表语句:"+sql);
        doris.dorisBuildTable(sql);
        return tableName;
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
        String keyName=(dto.factTable+"key").toLowerCase();
        //维度字段
        dto.list.stream().filter(e->e.attributeType==2).forEach(e->{
            sql.append("`"+e.dimensionTableName.substring(4)+"key` VARCHAR(50) COMMENT \"\" , ");
            aggregateKeys.add(e.dimensionTableName);
        });
        //退化维度
        dto.list.stream().filter(e->e.attributeType==1).forEach(e-> sql.append("`"+e.factFieldName+"` "+e.factFieldType+"("+e.factFieldLength+") COMMENT \"\", "));
        //聚合字段
        dto.list.stream().filter(e->e.attributeType==3).forEach(e-> sql.append("`"+e.atomicIndicatorName+"` BIGINT "+e.aggregationLogic+" COMMENT \"\", "));
        sql.deleteCharAt(sql.lastIndexOf(","));
        sql.append(" ) ");
        if (aggregateKeys.size()>0){
            String aggregateKeysSql=aggregateKeys.stream().map(e->"`"+e.substring(4)+"key`").collect(Collectors.joining(","));
            //排序字段
            sql.append(" DISTRIBUTED BY HASH("+aggregateKeysSql+") BUCKETS 16");
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
        createExternalTable(dto);
        StringBuilder sql=new StringBuilder();
        StringBuilder aggregationFunSql=new StringBuilder();
        StringBuilder groupSql=new StringBuilder();
        String tableName=dto.factTable;
        dto.list.forEach(e->{
            if(e.attributeType==3){
                aggregationFunSql.append("COALESCE(");
                aggregationFunSql.append(e.aggregationLogic);
                aggregationFunSql.append("(");
                aggregationFunSql.append(e.aggregatedField.toLowerCase());
                aggregationFunSql.append(") ,0)AS ");
                aggregationFunSql.append(e.atomicIndicatorName.toLowerCase());
                aggregationFunSql.append(" , ");
            }else if(e.attributeType==2){
                groupSql.append(""+e.dimensionTableName.substring(4)+"key , ");
                aggregationFunSql.append("COALESCE("+e.dimensionTableName.substring(4)+"key,'') AS "+e.dimensionTableName.toLowerCase()+" , ");
            }else if(e.attributeType==1){
                groupSql.append(""+e.factFieldName+", ");
                aggregationFunSql.append("COALESCE("+e.factFieldName+",'') AS "+e.factFieldName.toLowerCase()+" , ");
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
        sql.append("external_"+tableName);
        sql.append(" ");
        if (groupSql.length()>0){
            groupSql.deleteCharAt(groupSql.length()-1);
            sql.append("GROUP BY ");
            sql.append(groupSql);
        }
        return sql.toString();
    }

    public String createExternalTable(AtomicIndicatorFactDTO dto){
        //TODO 问题三
        List<AtomicIndicatorFactAttributeDTO> factAttributeDTOList = dto.factAttributeDTOList;
        String tableName="external_"+dto.factTable;
        String sql="drop table if exists "+tableName+";\n";
        sql+="CREATE EXTERNAL TABLE "+tableName+" ( ";
        for (AtomicIndicatorFactAttributeDTO atomicIndicatorFactAttributeDTO:factAttributeDTOList) {
            if(atomicIndicatorFactAttributeDTO.attributeType==0){
                sql+=atomicIndicatorFactAttributeDTO.factFieldCnName+" "+atomicIndicatorFactAttributeDTO.factFieldType+",";
            }
        }
        List<String> associateDimensionTableList = factAttributeDTOList.stream().map(d -> " " + d.associateDimensionTable + " ").collect(Collectors.toList());
        associateDimensionTableList.removeAll(Collections.singleton(" null "));
        associateDimensionTableList.stream().distinct();
        //' dim_test20211149 '
        for (String associateDimensionTable:associateDimensionTableList) {
            sql+=associateDimensionTable.substring(5,associateDimensionTable.length()-1)+"key varchar(50),";
        }
        sql=sql.substring(0,sql.length()-1)+")\n" + "ENGINE=ODBC\nPROPERTIES\n";
        sql+="(\"host\" = \"192.168.1.250\",\"port\" = \"5432\",\"user\" = \"postgres\",\"password\" = \"Password01!\",\"database\" = \"dmp_dw\",\"table\" = \"KKKK\",\"driver\" = \"PostgreSQL\",\"odbc_type\" = \"postgresql\");";
        sql=sql.replace("KKKK",dto.factTable);
        log.info("指标外部表建表语句:"+sql);
        doris.dorisBuildTable(sql);
        return tableName;
    }

    @Override
    public OlapPO selectByName(String name) {
        OlapPO olapPO = this.query().eq("table_name", name).eq("del_flag",1).one();
        return olapPO;
    }

    //查该业务域下所有的Doris里的表
    @Override
    public List<OlapPO> selectOlapByBusinessAreaId(String BusinessAreaId){
        List<OlapPO> list = this.query().eq("business_area_id", BusinessAreaId).eq("del_flag", 1).list();
        return list;
    }

}
