package com.fisk.task.consumer.postgre.datainput;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.BusinessTypeEnum;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.task.dto.atlas.AtlasEntityDbTableColumnDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.dto.task.TableFieldDetailDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IPostgreBuild;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author: DennyHui
 * CreateTime: 2021/8/27 12:57
 * Description:在pgsql库中创建表
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_DATAINPUT_PGSQL_TABLE_FLOW)
@Slf4j
public class BuildDataInputPgTableListener {
    @Resource
    IPostgreBuild pg;
    @Resource
    DataAccessClient dc;

    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.DATAINPUT_PG_TABLE_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        log.info("执行pg build table");
        log.info("dataInfo:" + dataInfo);
        AtlasEntityQueryDTO inpData = JSON.parseObject(dataInfo, AtlasEntityQueryDTO.class);
        ResultEntity<BuildPhysicalTableDTO> data = dc.getBuildPhysicalTableDTO(Long.parseLong(inpData.dbId), Long.parseLong(inpData.appId));
        BuildPhysicalTableDTO buildPhysicalTableDTO = data.data;
        //修改或创建表
        String selectTableSql="select\n" +
                "col.table_schema,\n" +
                "col.table_name,\n" +
                "col.ordinal_position,\n" +
                "col.column_name,\n" +
                "col.data_type,\n" +
                "col.character_maximum_length,\n" +
                "col.numeric_precision,\n" +
                "col.numeric_scale,\n" +
                "col.is_nullable,\n" +
                "col.column_default,\n" +
                "udt_name,\n" +
                "des.description\n" +
                "from\n" +
                "information_schema.columns col left join pg_description des on\n" +
                "col.table_name::regclass = des.objoid\n" +
                "and col.ordinal_position = des.objsubid\n" +
                "where\n" +
                " table_name = 'tableName'\n" +
                "order by\n" +
                "ordinal_position";

        selectTableSql=selectTableSql.replace("tableName","ods_" + buildPhysicalTableDTO.appAbbreviation.toLowerCase() + "_" + buildPhysicalTableDTO.tableName.toLowerCase());
        BusinessResult resultSetBusinessResult = pg.postgreQuery(selectTableSql, BusinessTypeEnum.DATAINPUT);
        List<TableFieldDetailDTO> arrayLists = JSONArray.parseArray(JSON.toJSONString(resultSetBusinessResult.data), TableFieldDetailDTO.class);
        if(arrayLists.size()!=0){
            updataOrCreateTable(arrayLists,buildPhysicalTableDTO.tableFieldsDTOS);
        }else{
            StringBuilder sql = new StringBuilder();
            StringBuilder sqlFileds = new StringBuilder();
            sql.append("CREATE TABLE tableName ( " + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName + "_pk" + " varchar(50) NOT NULL DEFAULT sys_guid() PRIMARY KEY,fi_bach_code varchar(50),");
            List<TableFieldsDTO> tableFieldsDTOS = buildPhysicalTableDTO.tableFieldsDTOS;
            tableFieldsDTOS.forEach((l) -> {
                sqlFileds.append(l.fieldName + " " + l.fieldType.toLowerCase() + ",");
            });
            sqlFileds.delete(sqlFileds.length() - 1, sqlFileds.length());
            sqlFileds.append(")");
            sql.append(sqlFileds);
            String stg_sql1 = sql.toString().replace("tableName", "ods_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            String stg_sql2 = sql.toString().replace("tableName", "stg_" + buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName);
            pg.postgreBuildTable(stg_sql1, BusinessTypeEnum.DATAINPUT);
            pg.postgreBuildTable(stg_sql2, BusinessTypeEnum.DATAINPUT);
            log.info("【PGSTG】" + stg_sql1);
            log.info("pg：建表完成");
        }
    }
    public void updataOrCreateTable(List<TableFieldDetailDTO> tableFieldDetailDTOS,List<TableFieldsDTO> tableFieldsDTOS){
        if(tableFieldDetailDTOS.size()!=0){
            List<TableFieldDetailDTO> tableFieldDetailDTOS1 = new ArrayList<>();
            List<TableFieldsDTO> tableFieldsDTOS1 = new ArrayList<>();
            tableFieldDetailDTOS1.addAll(tableFieldDetailDTOS);
            tableFieldsDTOS1.addAll(tableFieldsDTOS);
            String tableName=tableFieldDetailDTOS.get(0).tableName;
            String tableName1="stg_"+tableName.substring(4);
            String tablePk=tableFieldDetailDTOS.get(0).tableName;
            tablePk=tablePk.substring(4)+"_pk";
            String sql="";
            String sql1="";
            //库里已有的字段
            for(int i=0;i<tableFieldDetailDTOS.size();i++){

                //传过来的字段
                for(int j=0;j<tableFieldsDTOS.size();j++){
                    //如果库里的字段比到最后没有相同的,说明这个字段被删掉了
                    if(Objects.equals(tableFieldDetailDTOS.get(i).columnName,tableFieldsDTOS.get(j).fieldName.toLowerCase())){
                        tableFieldDetailDTOS1.remove(tableFieldDetailDTOS.get(i));
                    }else if(Objects.equals(tableFieldDetailDTOS.get(i).columnName,tableFieldsDTOS.get(j).fieldName)&&!Objects.equals(tableFieldDetailDTOS.get(i).udtName,tableFieldsDTOS.get(j).fieldType)){
                        //修改字段ALTER TABLE table_name ALTER COLUMN column_name TYPE datatype;
                        sql="ALTER TABLE "+tableName+" ALTER COLUMN "+tableFieldDetailDTOS.get(i).columnName+" type "+tableFieldsDTOS.get(j).fieldType;
                        sql1="ALTER TABLE "+tableName1+" ALTER COLUMN "+tableFieldDetailDTOS.get(i).columnName+" type "+tableFieldsDTOS.get(j).fieldType;
                        log.info("修改字段" + sql);
                        pg.postgreBuildTable(sql,BusinessTypeEnum.DATAINPUT);
                        pg.postgreBuildTable(sql1,BusinessTypeEnum.DATAINPUT);
                    }
                }
            }
            for (TableFieldDetailDTO tableFieldDetailDTO:tableFieldDetailDTOS1) {
                if(Objects.equals(tableFieldDetailDTO.columnName,tablePk)||Objects.equals(tableFieldDetailDTO.columnName,"fi_bach_code")){
                    continue;
                }else{
                    sql="ALTER TABLE "+tableName+" DROP COLUMN "+tableFieldDetailDTO.columnName;
                    sql1="ALTER TABLE "+tableName1+" DROP COLUMN "+tableFieldDetailDTO.columnName;
                    log.info("删除字段" + sql);
                    pg.postgreBuildTable(sql,BusinessTypeEnum.DATAINPUT);
                    pg.postgreBuildTable(sql1,BusinessTypeEnum.DATAINPUT);
                }
            }
            for (int j=0;j<tableFieldsDTOS.size();j++) {
                for (int i=0;i<tableFieldDetailDTOS.size();i++) {
                    if(Objects.equals(tableFieldsDTOS.get(j).fieldName.toLowerCase(),tableFieldDetailDTOS.get(i).columnName)){
                        tableFieldsDTOS1.remove(tableFieldsDTOS.get(j));
                    }
                }
            }
            for (TableFieldsDTO tableFieldsDTO:tableFieldsDTOS1) {
                //添加字段ALTER TABLE table_name ADD column_name datatype;
                sql="ALTER TABLE "+tableName+" add COLUMN "+tableFieldsDTO.fieldName+" "+tableFieldsDTO.fieldType;
                sql1="ALTER TABLE "+tableName1+" add COLUMN "+tableFieldsDTO.fieldName+" "+tableFieldsDTO.fieldType;
                log.info("添加字段" + sql);
                pg.postgreBuildTable(sql,BusinessTypeEnum.DATAINPUT);
                pg.postgreBuildTable(sql1,BusinessTypeEnum.DATAINPUT);
            }
        }

    }
}
