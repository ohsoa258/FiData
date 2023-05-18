package com.fisk.task.service.nifi.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorFactAttributeDTO;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorFactDTO;
import com.fisk.datamodel.dto.businessarea.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.ModelAttributeMetaDataDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigDTO;
import com.fisk.datamodel.enums.FactAttributeEnum;
import com.fisk.task.entity.OlapPO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.mapper.OlapMapper;
import com.fisk.task.service.doris.IDorisBuild;
import com.fisk.task.service.nifi.IOlap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${external-table-link}")
    public String externalTableLink;
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
            List<String> fileds=new ArrayList<>();
            fileds.add(" "+e.tableName.substring(4)+"key ");
            fileds.addAll(e.dto.stream().map(d->" "+d.fieldEnName+" ").collect(Collectors.toList()));
            List<String> correlationFileds=e.dto.stream().map(d->" "+d.associationTable+" ").collect(Collectors.toList());
            correlationFileds.removeAll(Collections.singleton(" null "));
            fileds.removeAll(Collections.singleton(" null "));
            correlationFileds.stream().distinct();
            OlapPO po=new OlapPO();
            po.businessAreaId=businessAreaId;
            String selectSql=fileds.stream().collect(Collectors.joining(","));
            //selectSql+=correlationFileds.stream().collect(Collectors.joining(","));
            log.info("external表拼接参数"+selectSql);
            if(correlationFileds!=null&&correlationFileds.size()>0){
                selectSql+=",";
                for (String correlationFiled:correlationFileds) {
                    selectSql+=correlationFiled.trim().substring(4)+"key,";
                }
            }
            selectSql=selectSql.substring(0,selectSql.length()-1);
            //po.selectDataSql=selectSql+" FROM external_"+e.tableName+"";
            String doUpdateOrInsertSql="insert into "+e.tableName+" ("+selectSql+") select  "+selectSql +" from ( select "+selectSql+" FROM external_"+e.tableName+") dw";
            po.selectDataSql=doUpdateOrInsertSql;
            po.tableName=e.tableName;
            po.createTableSql=buildCreateUniqModelSql(e);
            po.type= OlapTableEnum.DIMENSION;
            po.tableId=e.id;
            log.info("查询外表语句"+po.selectDataSql);
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
            log.info("查询外表语句"+po.selectDataSql);
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
        //dto.dto.forEach((l) -> sqlFiledBuild.append("`"+l.fieldEnName + "` " + l.fieldType +"("+l.fieldLength+ ") comment " + "'" + l.fieldCnName + "' ,"));
        List<ModelAttributeMetaDataDTO> dto2 = dto.dto;
        for (ModelAttributeMetaDataDTO modelAttributeMetaDataDTO : dto2) {
            if (modelAttributeMetaDataDTO.fieldType != null) {
                if (modelAttributeMetaDataDTO.fieldType.contains("TEXT") || modelAttributeMetaDataDTO.fieldType.contains("INT")) {
                    sqlFiledBuild.append("`" + modelAttributeMetaDataDTO.fieldEnName + "` " + modelAttributeMetaDataDTO.fieldType + " comment " + "'" + modelAttributeMetaDataDTO.fieldCnName + "' ,");

                } else if (modelAttributeMetaDataDTO.fieldType.toLowerCase().contains("numeric")||
                        modelAttributeMetaDataDTO.fieldType.toLowerCase().contains("float")) {
                    sqlFiledBuild.append("`" + modelAttributeMetaDataDTO.fieldEnName + "` float" + " comment " + "'" + modelAttributeMetaDataDTO.fieldCnName + "' ,");

                } else {
                    sqlFiledBuild.append("`" + modelAttributeMetaDataDTO.fieldEnName + "` " + modelAttributeMetaDataDTO.fieldType + "(" + modelAttributeMetaDataDTO.fieldLength + ") comment " + "'" + modelAttributeMetaDataDTO.fieldCnName + "' ,");

                }
            }
        }
        List<ModelAttributeMetaDataDTO> dto1 = dto.dto;
        List<String> fileds=dto1.stream().map(d->" "+d.associationTable+" ").collect(Collectors.toList());
        fileds.removeAll(Collections.singleton(" null "));
        fileds.stream().distinct();
        fileds.forEach((l) -> sqlFiledBuild.append("`"+l.toLowerCase().trim().substring(4) + "key` varchar(50) ,"));
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
            if (modelAttributeMetaDataDTO.fieldEnName != null) {
                if (modelAttributeMetaDataDTO.fieldType.toLowerCase().contains("numeric")||modelAttributeMetaDataDTO.fieldType.toLowerCase().contains("float")) {
                    sql += "`" + modelAttributeMetaDataDTO.fieldEnName + "` float,";
                } else {
                    sql += "`" + modelAttributeMetaDataDTO.fieldEnName + "` " + modelAttributeMetaDataDTO.fieldType + ",";
                }
            }

        }
        List<String> associationKeys = dto1.stream().map(d -> " " + d.associationTable + " ").collect(Collectors.toList());
        associationKeys.removeAll(Collections.singleton(" null "));
        associationKeys.stream().distinct();
        for (String associationKey:associationKeys) {
            sql+=associationKey.substring(5,associationKey.length()-1)+"key varchar(50),";
        }

        sql=sql.substring(0,sql.length()-1)+")\n" + "ENGINE=ODBC\nPROPERTIES\n";
        sql+="("+externalTableLink+",\"table\" = \"KKKKK\",\"driver\" = \"PostgreSQL\",\"odbc_type\" = \"postgresql\");";
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
        dto.list.stream().filter(e->e.attributeType== FactAttributeEnum.DIMENSION_KEY.getValue()).forEach(e->{
            sql.append("`"+e.dimensionTableName.substring(4)+"key` VARCHAR(50) COMMENT \"\" , ");
            aggregateKeys.add(e.dimensionTableName);
        });
        //退化维度
        dto.list.stream().filter(e -> e.attributeType == FactAttributeEnum.DEGENERATION_DIMENSION.getValue()).forEach(e-> sql.append("`"+e.factFieldName+"` "+e.factFieldType+"("+e.factFieldLength+") COMMENT \"\", "));
        //聚合字段
        dto.list.stream().filter(e -> e.attributeType == FactAttributeEnum.MEASURE.getValue()).forEach(e-> sql.append("`"+e.atomicIndicatorName+"` BIGINT "+e.aggregationLogic+" COMMENT \"\", "));
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
        StringBuilder filedsBuilder = new StringBuilder();
        String tableName=dto.factTable;
        dto.list.forEach(e->{
            if (e.attributeType == FactAttributeEnum.MEASURE.getValue()) {
                aggregationFunSql.append("COALESCE(");
                aggregationFunSql.append(e.aggregationLogic);
                aggregationFunSql.append("(");
                aggregationFunSql.append(e.aggregatedField.toLowerCase());
                aggregationFunSql.append(") ,0)AS ");
                aggregationFunSql.append(e.atomicIndicatorName.toLowerCase());
                aggregationFunSql.append(" , ");
                filedsBuilder.append(e.atomicIndicatorName.toLowerCase()+", ");
            } else if (e.attributeType == FactAttributeEnum.DIMENSION_KEY.getValue()) {
                groupSql.append(""+e.dimensionTableName.substring(4)+"key , ");
                aggregationFunSql.append("COALESCE("+e.dimensionTableName.substring(4)+"key,'') AS "+e.dimensionTableName.toLowerCase().substring(4)+"key , ");
                filedsBuilder.append(e.dimensionTableName.toLowerCase().substring(4)+"key , ");
            } else if (e.attributeType == FactAttributeEnum.DEGENERATION_DIMENSION.getValue()) {
                groupSql.append(""+e.factFieldName+", ");
                aggregationFunSql.append("COALESCE("+e.factFieldName+",'') AS "+e.factFieldName.toLowerCase()+" , ");
                filedsBuilder.append(e.factFieldName.toLowerCase()+" , ");
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
        String fileds = filedsBuilder.deleteCharAt(filedsBuilder.length()-2).toString();
        String doUpdateOrInsertSql="insert into "+tableName+" ("+fileds+") select  "+fileds +" from ( "+sql.toString()+") dw";
        return doUpdateOrInsertSql;
    }

    public String createExternalTable(AtomicIndicatorFactDTO dto){
        List<AtomicIndicatorFactAttributeDTO> factAttributeDTOList = dto.factAttributeDTOList;
        String tableName="external_"+dto.factTable;
        String sql="drop table if exists "+tableName+";\n";
        sql+="CREATE EXTERNAL TABLE "+tableName+" ( ";
        for (AtomicIndicatorFactAttributeDTO atomicIndicatorFactAttributeDTO:factAttributeDTOList) {
            if(atomicIndicatorFactAttributeDTO.attributeType==0||atomicIndicatorFactAttributeDTO.attributeType==2){
                if(atomicIndicatorFactAttributeDTO.factFieldType.toLowerCase().contains("numeric")||
                        atomicIndicatorFactAttributeDTO.factFieldType.toLowerCase().contains("float")){
                    sql+="`"+atomicIndicatorFactAttributeDTO.factFieldCnName+"` FLOAT,";
                }else{
                    sql+="`"+atomicIndicatorFactAttributeDTO.factFieldCnName+"` "+atomicIndicatorFactAttributeDTO.factFieldType+",";
                }


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
        //sql+="("+externalTableLink+",\"table\" = \"KKKKK\",\"driver\" = \"PostgreSQL\",\"odbc_type\" = \"postgresql\");";
        sql+="("+externalTableLink+",\"table\" = \"KKKK\",\"driver\" = \"PostgreSQL\",\"odbc_type\" = \"postgresql\");";
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

    @Override
    public OlapPO selectOlapPO(int id) {
        HashMap<String, Object> conditionHashMap = new HashMap<>();
        OlapPO olapPO = new OlapPO();
        conditionHashMap.put("del_flag", 1);
        conditionHashMap.put("id", id);
        List<OlapPO> olapPOS = mapper.selectByMap(conditionHashMap);
        if (olapPOS.size() > 0) {
            olapPO = olapPOS.get(0);
        } else {
            log.error("未找到对应指标表,id" + id);
        }
        return olapPO;
    }

    @Override
    public NifiGetPortHierarchyDTO getNifiGetPortHierarchy(String pipelineId,Integer type,String tableName,Integer tableAccessId) {
        NifiGetPortHierarchyDTO nifiGetPortHierarchyDTO = new NifiGetPortHierarchyDTO();
        nifiGetPortHierarchyDTO.workflowId = pipelineId;
        OlapTableEnum nameByValue = OlapTableEnum.getNameByValue(type);
        switch (nameByValue) {
            case KPI:
                OlapPO olapPO = this.query().eq("id", tableAccessId).one();
                nifiGetPortHierarchyDTO.tableId = String.valueOf(olapPO.tableId);
                if (olapPO.tableName.contains("dim")) {
                    nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.OLAP_DIMENSION_TASK;
                } else {
                    nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.OLAP_FACT_TASK;
                }
                break;
            case DIMENSION:
                nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.DW_DIMENSION_TASK;
                nifiGetPortHierarchyDTO.tableId = String.valueOf(tableAccessId);
                break;
            case FACT:
                nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.DW_FACT_TASK;
                nifiGetPortHierarchyDTO.tableId = String.valueOf(tableAccessId);
                break;
            case PHYSICS:
                nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.DATALAKE_TASK;
                nifiGetPortHierarchyDTO.tableId = String.valueOf(tableAccessId);
                break;
            case WIDETABLE:
                nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.OLAP_WIDETABLE_TASK;
                nifiGetPortHierarchyDTO.tableId = String.valueOf(tableAccessId);
                break;
            case PHYSICS_API:
                nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.DATALAKE_API_TASK;
                nifiGetPortHierarchyDTO.tableId = String.valueOf(tableAccessId);
                break;
            case CUSTOMIZESCRIPT:
                nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.CUSTOMIZE_SCRIPT_TASK;
                break;
            case SFTPFILECOPYTASK:
                nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.SFTP_FILE_COPY_TASK;
                break;
            case POWERBIDATASETREFRESHTASK:
                nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.POWERBI_DATA_SET_REFRESH_TASK;
                break;
            case MDM_DATA_ACCESS:
                nifiGetPortHierarchyDTO.channelDataEnum = ChannelDataEnum.MDM_TABLE_TASK;
                nifiGetPortHierarchyDTO.tableId = String.valueOf(tableAccessId);
            default:
                break;
        }
        return nifiGetPortHierarchyDTO;
    }


}
