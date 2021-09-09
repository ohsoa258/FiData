package com.fisk.task.consumer.doris;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.BusinessTypeEnum;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.datamodel.dto.dimensionattribute.ModelAttributeMetaDataDTO;
import com.fisk.task.entity.TaskDwDimPO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.mapper.TaskDwDimMapper;
import com.fisk.task.service.IDorisBuild;
import com.fisk.task.service.IPostgreBuild;
import com.fisk.task.service.ITaskDwDim;
import com.fisk.task.utils.PostgreHelper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: cfk
 * CreateTime: 2021/08/03 15:05
 * Description:
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_DATAMODEL_DORIS_TABLE)
@Slf4j
public class BuildDataModelDorisTableListener {
    @Resource
    DataModelClient dc;
    @Resource
    IDorisBuild doris;
    @Resource
    IPostgreBuild iPostgreBuild;
    @Resource
    ITaskDwDim iTaskDwDim;
    @Resource
    TaskDwDimMapper taskDwDimMapper;
    @Resource
    DataAccessClient client;

    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.DATAMODEL_DORIS_TABLE_MQ_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        DimensionAttributeAddDTO inpData = JSON.parseObject(dataInfo, DimensionAttributeAddDTO.class);
        ResultEntity<Object> dimensionAttributeList =new ResultEntity<>();
        boolean pgdbTable=false;
        if(inpData.createType==0){
            dimensionAttributeList=dc.getDimensionEntity(inpData.dimensionId);
            ModelMetaDataDTO modelMetaDataDTO = JSON.parseObject(JSON.toJSONString(dimensionAttributeList.data), ModelMetaDataDTO.class);
             pgdbTable = createPgdbTable(modelMetaDataDTO);
            log.info("pg数据库创表结果为" + pgdbTable);
            String storedProcedure = createStoredProcedure(modelMetaDataDTO);
            PostgreHelper.postgreExecuteSql(storedProcedure,BusinessTypeEnum.DATAMODEL);
        }else {
            dimensionAttributeList=dc.getFactEntity(inpData.dimensionId);
            List<ModelMetaDataDTO> modelMetaDataDTOS = JSON.parseArray(JSON.toJSONString(dimensionAttributeList), ModelMetaDataDTO.class);
            for (ModelMetaDataDTO modelMetaDataDTO:modelMetaDataDTOS) {
             pgdbTable = createPgdbTable(modelMetaDataDTO);
             log.info(modelMetaDataDTO.tableName+"pg数据库创表结果为" + pgdbTable);
             String storedProcedure = createStoredProcedure(modelMetaDataDTO);
             PostgreHelper.postgreExecuteSql(storedProcedure,BusinessTypeEnum.DATAMODEL);
            }
        }
        //nifi组件配置pg-ods2pg-dw,调用存储过程


    }
    /*
    * 创建表
    *
    * */
    private boolean createPgdbTable(ModelMetaDataDTO modelMetaDataDTO){
        //创建pgdb表
        //1.拼接sql
        StringBuilder sql = new StringBuilder();
        List<String> strings = new ArrayList<>();
        String stg_table =  modelMetaDataDTO.tableName;
        String stg_sql = "";
        sql.append("CREATE TABLE tableName (");
        StringBuilder sqlFileds = new StringBuilder();
        List<ModelAttributeMetaDataDTO> dto = modelMetaDataDTO.dto;
        dto.forEach((l) -> {
            if(l.fieldEnName!=null){
            sqlFileds.append( l.fieldEnName + " " + l.fieldType + " ,");
                strings.add(l.fieldEnName);
            }
        });
        List<String> collect1 = dto.stream().map(e -> e.associationTable).collect(Collectors.toList());
        List<String> collect = collect1.stream().distinct().collect(Collectors.toList());//去重
        collect.forEach((l) -> {
            if(l!=null){
                sqlFileds.append( l+"_key" + " " + "varchar" + " ,");
                strings.add(l+"_key");
            }
        });
        modelMetaDataDTO.fieldEnNames=strings;
        sqlFileds.delete(sqlFileds.length()-1,sqlFileds.length());
        sqlFileds.append(")");
        sql.append(sqlFileds);
        stg_sql = sql.toString().replace("tableName", stg_table);
        //2.连接jdbc执行sql
        BusinessResult datamodel = iPostgreBuild.postgreBuildTable(stg_sql, BusinessTypeEnum.DATAMODEL);
        log.info("【PGSTG】" + stg_sql);
        TaskDwDimPO taskDwDimPO = new TaskDwDimPO();
        taskDwDimPO.areaBusinessBame="";//业务域名
        taskDwDimPO.sqlContent=stg_sql;//创建表的sql
        taskDwDimPO.tableName=stg_table;
        taskDwDimPO.storedProcedureName="update"+stg_table+"()";
        taskDwDimMapper.insert(taskDwDimPO);
        if(datamodel.success==true){
            return true;
        }else {
            return false;
        }
    }
    /*
    * 创建存储过程
    * */
    public String createStoredProcedure(ModelMetaDataDTO modelMetaDataDTO){
        List<String> fieldEnNames = modelMetaDataDTO.fieldEnNames;
        List<ModelAttributeMetaDataDTO> dto = modelMetaDataDTO.dto;
        String fieldString="";
        String fieldValue="";
        String fieldUpdate="";
        fieldEnNames.removeAll(Collections.singleton(null));
        String storedProcedureSql="CREATE OR REPLACE PROCEDURE public.update";
        storedProcedureSql+=modelMetaDataDTO.tableName+"() \nRETURNS void AS\n" +
                "$BODY$\nDECLARE";
        storedProcedureSql+= "mysql1 text;\nmysql2 text;\nresrow1 record;\ngeshu text;\ninsert_sql TEXT;\n update_sql TEXT;\n";
        storedProcedureSql+="begin\n";
        storedProcedureSql+="mysql1:='"+selectSql(modelMetaDataDTO)+"';\n";
        storedProcedureSql+="mysql2:='select case when count(*)>0 THEN ''y'' ELSE ''n'' end from "+modelMetaDataDTO.tableName+" where "+modelMetaDataDTO.tableName+"_key=\n";
        storedProcedureSql+="FOR resrow1 IN EXECUTE mysql1\n";
        storedProcedureSql+="LOOP\n";
        storedProcedureSql+="mysql2:=mysql2 ||''''|| resrow1."+modelMetaDataDTO.tableName+"_key||'''';\n";
        storedProcedureSql+="raise notice'%',mysql2;";
        storedProcedureSql+="EXECUTE mysql2 into geshu;\n";
        storedProcedureSql+="raise notice'%',geshu;\n";
        storedProcedureSql+="if geshu!='y' then \n";
        storedProcedureSql+="insert_sql:=insert into "+modelMetaDataDTO.tableName+"(";
        for (String field:fieldEnNames) {
            fieldString+=field+",";
            fieldValue+="'''||resrow1."+field+"||''',";
            fieldUpdate+=field+"'''||resrow1."+field+"||''',";
        }
        fieldString.substring(0,fieldString.length()-1);
        fieldValue=fieldValue.substring(0,fieldValue.length()-4)+");\n";
        fieldUpdate=fieldUpdate.substring(0,fieldUpdate.length()-3)+" where "+modelMetaDataDTO.tableName+"_key= '''||resrow1."+modelMetaDataDTO.tableName+"_key||'''';\n";
        storedProcedureSql+=fieldString+" ) values (";
        storedProcedureSql+=fieldValue;
        storedProcedureSql+="else\n";
        storedProcedureSql+="update_sql:=update "+modelMetaDataDTO.tableName+" set "+fieldUpdate;//塞字段,慢慢塞吧
        storedProcedureSql+="EXECUTE update_sql;\n";
        storedProcedureSql+="end if;\n";
        storedProcedureSql+="END LOOP;\n";
        storedProcedureSql+="end;\n";
        return storedProcedureSql;
    }
    /*
    * 拼装查询数据的sql
    * */
    public String selectSql(ModelMetaDataDTO modelMetaDataDTO){
        List<ModelAttributeMetaDataDTO> dto = modelMetaDataDTO.dto;
        String selectSql="select  "+modelMetaDataDTO+"_key,";
        String selectSql1="";
        String selectSql2="";
        int id=0;
        for (ModelAttributeMetaDataDTO d:dto) {
            //d.sourceFieldId;
            id=d.sourceFieldId;
            ResultEntity<Object> tableField = client.getTableField(id);
            TableFieldsDTO tableFieldsDTO = JSON.parseObject(JSON.toJSONString(tableField.data), TableFieldsDTO.class);
            //拼接selectsql
            selectSql+=modelMetaDataDTO.tableName+"."+tableFieldsDTO.fieldName+",";
            if(d.associationTable!=null&&!selectSql1.contains(d.associationTable)){//去重去空
                selectSql1+=d.associationTable+"."+d.associationTable+"_key,";
            }
            if(d.associationField!=null){
                selectSql2+="left join "+d.associationTable+" on "+d.associationTable+"."+d.associationField+"="+modelMetaDataDTO.tableName+"."+tableFieldsDTO.fieldName;
            }
        }
        selectSql= selectSql.substring(0,selectSql.length()-1);
        selectSql1+=" from "+modelMetaDataDTO.tableName;
        selectSql=selectSql+selectSql1+selectSql2;
        return selectSql;
    }

}
