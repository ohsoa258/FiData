package com.fisk.task.consumer.doris;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.davis.client.model.ControllerServiceEntity;
import com.davis.client.model.ProcessGroupEntity;
import com.davis.client.model.ProcessorEntity;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.constants.NifiConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.BusinessTypeEnum;
import com.fisk.common.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeAddDTO;
import com.fisk.datamodel.dto.dimensionattribute.ModelAttributeMetaDataDTO;
import com.fisk.datamodel.enums.DimensionAttributeEnum;
import com.fisk.task.dto.nifi.BuildCallDbProcedureProcessorDTO;
import com.fisk.task.dto.nifi.BuildDbControllerServiceDTO;
import com.fisk.task.dto.nifi.BuildProcessGroupDTO;
import com.fisk.task.dto.task.AppNifiSettingPO;
import com.fisk.task.dto.task.TableNifiSettingPO;
import com.fisk.task.entity.TaskDwDimPO;
import com.fisk.task.entity.TaskPgTableStructurePO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.mapper.TaskDwDimMapper;
import com.fisk.task.mapper.TaskPgTableStructureMapper;
import com.fisk.task.service.IDorisBuild;
import com.fisk.task.service.INifiComponentsBuild;
import com.fisk.task.service.IPostgreBuild;
import com.fisk.task.service.ITaskDwDim;
import com.fisk.task.service.impl.AppNifiSettingServiceImpl;
import com.fisk.task.service.impl.NifiConfigServiceImpl;
import com.fisk.task.service.impl.TableNifiSettingServiceImpl;
import com.fisk.task.utils.NifiPositionHelper;
import com.fisk.task.utils.PostgreHelper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: cfk
 * CreateTime: 2021/08/03 15:05
 * Description:
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_DATAMODEL_DORIS_TABLE)
@Slf4j
public class BuildDataModelDorisTableListener
        extends ServiceImpl<TaskPgTableStructureMapper, TaskPgTableStructurePO>
{
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
    @Resource
    INifiComponentsBuild componentsBuild;
    @Value("${pgsql-datamodel.url}")
    public String pgsqlDatamodelUrl;
    @Value("${pgsql-datamodel.username}")
    public String pgsqlDatamodelUsername;
    @Value("${pgsql-datamodel.password}")
    public String pgsqlDatamodelPassword;
    @Resource
    AppNifiSettingServiceImpl appNifiSettingService;
    @Resource
    NifiConfigServiceImpl nifiConfigService;
    @Resource
    TableNifiSettingServiceImpl tableNifiSettingService;


    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.DATAMODEL_DORIS_TABLE_MQ_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        DimensionAttributeAddDTO inpData = JSON.parseObject(dataInfo, DimensionAttributeAddDTO.class);
        ResultEntity<Object> dimensionAttributeList =new ResultEntity<>();
        boolean pgdbTable=false;
        if(inpData.createType==0){
            dimensionAttributeList=dc.getDimensionEntity(inpData.dimensionId);
            ModelMetaDataDTO modelMetaDataDTO = JSON.parseObject(JSON.toJSONString(dimensionAttributeList.data), ModelMetaDataDTO.class);

            //向task库中添加维度数据结构
            List<ModelMetaDataDTO> list=new ArrayList<>();
            list.add(modelMetaDataDTO);
            saveTableStructure(list);

             pgdbTable = createPgdbTable(modelMetaDataDTO,inpData.businessAreaName);
            log.info("pg数据库创表结果为" + pgdbTable);
            String storedProcedure = createStoredProcedure2(modelMetaDataDTO);
            PostgreHelper.postgreExecuteSql(storedProcedure,BusinessTypeEnum.DATAMODEL);
            //nifi组件配置pg-ods2pg-dw,调用存储过程
            createNiFiFlow(modelMetaDataDTO,inpData.businessAreaName,DataClassifyEnum.DATAMODELING,OlapTableEnum.DIMENSION);
            //根据业务域名称创建组,加数据库连接池
            //根据表名创建任务组
            //创建组件
        }else {
            dimensionAttributeList=dc.getBusinessProcessFact(inpData.dimensionId);
            List<ModelMetaDataDTO> modelMetaDataDTOS = JSON.parseArray(JSON.toJSONString(dimensionAttributeList.data), ModelMetaDataDTO.class);

            //向task库中添加业务下所有事实表数据结构
            saveTableStructure(modelMetaDataDTOS);

            for (ModelMetaDataDTO modelMetaDataDTO:modelMetaDataDTOS) {
             pgdbTable = createPgdbTable(modelMetaDataDTO,inpData.businessAreaName);
             log.info(modelMetaDataDTO.tableName+"pg数据库创表结果为" + pgdbTable);
             String storedProcedure = createStoredProcedure2(modelMetaDataDTO);
             PostgreHelper.postgreExecuteSql(storedProcedure,BusinessTypeEnum.DATAMODEL);
                //nifi组件配置pg-ods2pg-dw,调用存储过程
                createNiFiFlow(modelMetaDataDTO,inpData.businessAreaName,DataClassifyEnum.DATAMODELING,OlapTableEnum.FACT);
                //根据业务域名称创建组,加数据库连接池
                //根据表名创建任务组
                //创建组件
            }
        }


    }

    private void createNiFiFlow(ModelMetaDataDTO modelMetaDataDTO,String businessAreaName,DataClassifyEnum dataClassifyEnum,OlapTableEnum olapTableEnum){
        BuildDbControllerServiceDTO buildDbControllerServiceDTO = new BuildDbControllerServiceDTO();
        //数据连接池
        ControllerServiceEntity  data=new ControllerServiceEntity();
        //应用组
        ProcessGroupEntity data1=new ProcessGroupEntity();
        //任务组
        ProcessGroupEntity data2=new ProcessGroupEntity();

        buildDbControllerServiceDTO.driverLocation= NifiConstants.DriveConstants.POSTGRESQL_DRIVE_PATH;
        buildDbControllerServiceDTO.conUrl=pgsqlDatamodelUrl;
        buildDbControllerServiceDTO.driverName= DriverTypeEnum.POSTGRESQL.getName();
        buildDbControllerServiceDTO.pwd=pgsqlDatamodelPassword;
        buildDbControllerServiceDTO.name=businessAreaName;
        buildDbControllerServiceDTO.enabled = true;
        buildDbControllerServiceDTO.groupId = NifiConstants.ApiConstants.ROOT_NODE;;
        buildDbControllerServiceDTO.details=businessAreaName;
        buildDbControllerServiceDTO.user=pgsqlDatamodelUsername;
        BusinessResult<ControllerServiceEntity> controllerServiceEntityBusinessResult = componentsBuild.buildDbControllerService(buildDbControllerServiceDTO);
        if (controllerServiceEntityBusinessResult.success) {
             data = controllerServiceEntityBusinessResult.data;
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, controllerServiceEntityBusinessResult.msg);
        }
        //创建应用组

        BuildProcessGroupDTO dto = new BuildProcessGroupDTO();
        dto.name = modelMetaDataDTO.tableName;
        dto.details = modelMetaDataDTO.tableName;
        //根据组个数，定义坐标
        int count = componentsBuild.getGroupCount(NifiConstants.ApiConstants.ROOT_NODE);
        dto.positionDTO = NifiPositionHelper.buildXPositionDTO(count);
        //创建任务组
        BusinessResult<ProcessGroupEntity> res = componentsBuild.buildProcessGroup(dto);
        if (res.success) {
             data1 = res.data;
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
        }
        //创建任务组
        BuildProcessGroupDTO buildProcessGroupDTO = new BuildProcessGroupDTO();
        buildProcessGroupDTO.name = modelMetaDataDTO.tableName;
        buildProcessGroupDTO.details = modelMetaDataDTO.tableName;
        buildProcessGroupDTO.groupId = data1.getId();
        //根据组个数，定义坐标
        int count1 = componentsBuild.getGroupCount(data1.getId());
        buildProcessGroupDTO.positionDTO = NifiPositionHelper.buildXPositionDTO(count1);
        //创建组
        BusinessResult<ProcessGroupEntity> res1 = componentsBuild.buildProcessGroup(buildProcessGroupDTO);
        if (res1.success) {
            data2= res1.data;
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
        }
        //创建组件,启动组件
        List<ProcessorEntity> components = createComponents(data2.getId(), data.getId(), modelMetaDataDTO.sqlName);
        //回写
        savaNifiAllSetting(data,data1,data2,components, modelMetaDataDTO,dataClassifyEnum,olapTableEnum);

    }

    public void savaNifiAllSetting(ControllerServiceEntity controllerServiceEntity,ProcessGroupEntity processGroupEntity1,ProcessGroupEntity processGroupEntity2,List<ProcessorEntity> processorEntities,ModelMetaDataDTO modelMetaDataDTO,DataClassifyEnum dataClassifyEnum,OlapTableEnum olapTableEnum){
        AppNifiSettingPO appNifiSettingPO = new AppNifiSettingPO();
        TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
        appNifiSettingService.query().eq("","").one();
        appNifiSettingPO.targetDbPoolComponentId=controllerServiceEntity.getId();
        appNifiSettingPO.appId=modelMetaDataDTO.appId;
        appNifiSettingPO.type=dataClassifyEnum.getValue();
        //做判断,是否新增
        appNifiSettingPO.appComponentId=processGroupEntity1.getId();
        appNifiSettingService.saveOrUpdate(appNifiSettingPO);
        tableNifiSettingPO.tableAccessId= Math.toIntExact(modelMetaDataDTO.id);
        tableNifiSettingPO.tableName=modelMetaDataDTO.tableName;
        tableNifiSettingPO.appId=modelMetaDataDTO.appId;
        tableNifiSettingPO.selectSql="call "+modelMetaDataDTO.sqlName;
        tableNifiSettingPO.saveTargetDbProcessorId=processorEntities.get(0).getId();
        tableNifiSettingPO.tableComponentId=processGroupEntity2.getId();
        tableNifiSettingService.saveOrUpdate(tableNifiSettingPO);
    }

    /*
    * 创建组件
    * */
    public List<ProcessorEntity> createComponents(String groupId,String componentId,String executsql){
        List<ProcessorEntity> processors=new ArrayList<>();
        BuildCallDbProcedureProcessorDTO callDbProcedureProcessorDTO = new BuildCallDbProcedureProcessorDTO();
        callDbProcedureProcessorDTO.name = "CallDbProcedure";
        callDbProcedureProcessorDTO.details = "CallDbProcedure";
        callDbProcedureProcessorDTO.groupId = groupId;
        callDbProcedureProcessorDTO.dbConnectionId=componentId;
        callDbProcedureProcessorDTO.executsql="call "+executsql;
        callDbProcedureProcessorDTO.haveNextOne=false;
        callDbProcedureProcessorDTO.positionDTO=NifiPositionHelper.buildYPositionDTO(1);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildCallDbProcedureProcess(callDbProcedureProcessorDTO);
        if( !processorEntityBusinessResult.success){
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, processorEntityBusinessResult.msg);
        }
        processors.add(processorEntityBusinessResult.data);
        List<ProcessorEntity> processorEntities = componentsBuild.enabledProcessor(groupId, processors);
        return processorEntities;
    }

    /*
    * 创建表
    *
    * */
    private boolean createPgdbTable(ModelMetaDataDTO modelMetaDataDTO,String businessAreaName){
        String tableName=modelMetaDataDTO.tableName;
        String tableKeyName=modelMetaDataDTO.tableName+"_pk";
        //创建pgdb表
        //1.拼接sql
        StringBuilder sql = new StringBuilder();
        List<String> strings = new ArrayList<>();
        Map<String, String> Map = new HashMap<>();
        String stg_table =  modelMetaDataDTO.tableName;
        String stg_sql = "";
        sql.append("CREATE TABLE tableName ( "+tableKeyName+" varchar PRIMARY KEY,fk_doris_increment_code varchar,");
        StringBuilder sqlFileds = new StringBuilder();
        List<ModelAttributeMetaDataDTO> dto = modelMetaDataDTO.dto;
        dto.forEach((l) -> {
            if(l.associationTable==null){
            sqlFileds.append( l.fieldEnName + " " + l.fieldType.toLowerCase() + " ,");
            ResultEntity<Object> tableField = client.getTableField(l.sourceFieldId);
            TableFieldsDTO tableFieldsDTO = JSON.parseObject(JSON.toJSONString(tableField.data), TableFieldsDTO.class);
            Map.put(l.fieldEnName,tableFieldsDTO.fieldName);
            strings.add(l.fieldEnName);
            }else{
                String tableName2="";
                if(l!=null){
                    tableName2=l.associationTable;
                    sqlFileds.append( tableName2+"_pk" + " " + "varchar" + " ,");
                    Map.put(tableName2+"_pk",tableName2+"_pk");
                    strings.add(tableName2+"_pk");
                }
            }
        });
        modelMetaDataDTO.fieldEnNames=strings;
        modelMetaDataDTO.fieldEnNameMaps=Map;
        sqlFileds.delete(sqlFileds.length()-1,sqlFileds.length());
        sqlFileds.append(")");
        sql.append(sqlFileds);
        stg_sql = sql.toString().replace("tableName", tableName);
        //2.连接jdbc执行sql
        BusinessResult datamodel = iPostgreBuild.postgreBuildTable(stg_sql, BusinessTypeEnum.DATAMODEL);
        log.info("【PGSTG】" + stg_sql);
        TaskDwDimPO taskDwDimPO = new TaskDwDimPO();
        taskDwDimPO.areaBusinessName=businessAreaName;//业务域名
        taskDwDimPO.sqlContent=stg_sql;//创建表的sql
        taskDwDimPO.tableName=stg_table;
        taskDwDimPO.storedProcedureName="update"+tableName+"()";
        taskDwDimMapper.insert(taskDwDimPO);
        modelMetaDataDTO.sqlName=taskDwDimPO.storedProcedureName;
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
        storedProcedureSql+=modelMetaDataDTO.tableName+"() \n" +
                "LANGUAGE 'plpgsql'\nas $BODY$\nDECLARE\n";
        storedProcedureSql+= "mysql1 text;\nmysql2 text;\nresrow1 record;\ngeshu text;\ninsert_sql TEXT;\n update_sql TEXT;\n";
        storedProcedureSql+="begin\n";
        storedProcedureSql+="mysql1:='"+selectSql(modelMetaDataDTO)+"';\n";
        storedProcedureSql+="FOR resrow1 IN EXECUTE mysql1\n";
        storedProcedureSql+="LOOP\n";
        storedProcedureSql+="mysql2:='select case when count(*)>0 THEN ''y'' ELSE ''n'' end from "+modelMetaDataDTO.tableName+" where "+modelMetaDataDTO.tableName+"_pk=';\n";
        storedProcedureSql+="mysql2:=mysql2 ||''''|| resrow1."+modelMetaDataDTO.tableName+"_pk||'''';\n";
        storedProcedureSql+="raise notice'%',mysql2;";
        storedProcedureSql+="EXECUTE mysql2 into geshu;\n";
        storedProcedureSql+="raise notice'%',geshu;\n";
        storedProcedureSql+="if geshu!='y' then \n";
        storedProcedureSql+="insert_sql:='insert into "+modelMetaDataDTO.tableName+"( "+modelMetaDataDTO.tableName+"_pk, fk_doris_increment_code,";
       /* for (String field:fieldEnNames) {
            fieldString+=field+",";
            fieldValue+="'''||resrow1."+field+"||''',";
            fieldUpdate+=field+"='''||resrow1."+field+"||''',";
        }*/
        for (String key : modelMetaDataDTO.fieldEnNameMaps.keySet()) {
            fieldString+=key+",";
            fieldValue+="'''||resrow1."+modelMetaDataDTO.fieldEnNameMaps.get(key)+"||''',";
            fieldUpdate+=key+"='''||resrow1."+modelMetaDataDTO.fieldEnNameMaps.get(key)+"||''',";
        }
        fieldString=fieldString.substring(0,fieldString.length()-1);
        fieldValue=fieldValue.substring(0,fieldValue.length()-1)+")';\n";
        fieldUpdate=fieldUpdate.substring(0,fieldUpdate.length()-1)+" where "+modelMetaDataDTO.tableName+"_pk= '''||resrow1."+modelMetaDataDTO.tableName+"_pk||'''';\n";
        storedProcedureSql+=fieldString+" ) values ( '''||resrow1."+modelMetaDataDTO.tableName+"_pk||''','''||resrow1.fk_doris_increment_code||''',";
        storedProcedureSql+=fieldValue;
        storedProcedureSql+="EXECUTE insert_sql;\n";
        storedProcedureSql+="else\n";
        storedProcedureSql+="update_sql:='update "+modelMetaDataDTO.tableName+" set fk_doris_increment_code='''||resrow1.fk_doris_increment_code||''',"+fieldUpdate;//塞字段,慢慢塞吧
        storedProcedureSql+="EXECUTE update_sql;\n";
        storedProcedureSql+="end if;\n";
        storedProcedureSql+="END LOOP;\n";
        storedProcedureSql+="end;\n";
        storedProcedureSql+="$BODY$;\n";
        return storedProcedureSql;
    }
    /*
    * 拼装查询数据的sql
    * */
    public String selectSql(ModelMetaDataDTO modelMetaDataDTO){
        List<ModelAttributeMetaDataDTO> dto = modelMetaDataDTO.dto;
        String selectSql="select  ods_"+modelMetaDataDTO.appbAbreviation+"_"+modelMetaDataDTO.sourceTableName+"."+modelMetaDataDTO.appbAbreviation+"_"+modelMetaDataDTO.sourceTableName +"_pk,ods_"+modelMetaDataDTO.appbAbreviation+"_"+modelMetaDataDTO.sourceTableName+".fk_doris_increment_code ,";
        String selectSql1=" ";
        String selectSql2="";
        String selectSql3="";
        String selectSql4=" ";
        int id=0;
        for (ModelAttributeMetaDataDTO d:dto) {
            //d.sourceFieldId;
            id=d.sourceFieldId;
            ResultEntity<Object> tableField = client.getTableField(id);
            TableFieldsDTO tableFieldsDTO = JSON.parseObject(JSON.toJSONString(tableField.data), TableFieldsDTO.class);
            //拼接selectsql
            if (!Objects.equals(d.attributeType, 1)) {
                if (Objects.equals(d.fieldType.toLowerCase(), "int")) {
                    selectSql += "coalesce( ods_" + modelMetaDataDTO.appbAbreviation + "_" + modelMetaDataDTO.sourceTableName + "." + tableFieldsDTO.fieldName + ",0),";
                } else {
                    selectSql += "coalesce( ods_" + modelMetaDataDTO.appbAbreviation + "_" + modelMetaDataDTO.sourceTableName + "." + tableFieldsDTO.fieldName + ",''''null''''),";
                }
                //主表
                selectSql3 += tableFieldsDTO.fieldName + " " + tableFieldsDTO.fieldType.toLowerCase() + ",";
            }

            if(d.associationTable!=null&&!selectSql1.contains(d.associationTable)){//去重去空
                //这里要改,前缀
                if (Objects.equals(d.fieldType.toLowerCase(), "int")) {
                    selectSql1 += "coalesce( ods_" + tableFieldsDTO.appbAbreviation + "_" + tableFieldsDTO.originalTableName + "." + tableFieldsDTO.appbAbreviation + "_" + tableFieldsDTO.originalTableName + "_pk,0),";
                } else {
                    selectSql1 += "coalesce( ods_" + tableFieldsDTO.appbAbreviation + "_" + tableFieldsDTO.originalTableName + "." + tableFieldsDTO.appbAbreviation + "_" + tableFieldsDTO.originalTableName + "_pk,''''null''''),";

                }
                //别名我说的算
                selectSql4+="dim_"+tableFieldsDTO.originalTableName+"_pk  varchar,";
            }
            if(d.associationField!=null){
                //这里要改,前缀
                ResultEntity<Object> tableField1 = client.getTableField(d.associationSourceFieldId);
                TableFieldsDTO tableFieldsDTO1 = JSON.parseObject(JSON.toJSONString(tableField1.data), TableFieldsDTO.class);
                selectSql2+=" left join ods_"+tableFieldsDTO.appbAbreviation+"_"+tableFieldsDTO.originalTableName+" on ods_"+tableFieldsDTO.appbAbreviation+"_"+tableFieldsDTO.originalTableName+"."+tableFieldsDTO.fieldName+"=ods_"+modelMetaDataDTO.appbAbreviation+"_"+modelMetaDataDTO.sourceTableName+"."+tableFieldsDTO1.fieldName;
            }
        }
        if(Objects.equals(selectSql1," ")){
            selectSql=selectSql.substring(0,selectSql.length()-1);
            selectSql3=selectSql3.substring(0,selectSql3.length()-1);
        }
        selectSql1=selectSql1.substring(0,selectSql1.length()-1)+" from ods_"+modelMetaDataDTO.appbAbreviation+"_"+modelMetaDataDTO.sourceTableName;
        selectSql=selectSql+selectSql1+selectSql2;
        selectSql="select * from dblink('||'''host=192.168.1.250 dbname=dmp_ods user=postgres password=Password01!'''||','||'''"+selectSql+"'''||') as t (";
        selectSql3=modelMetaDataDTO.tableName+"_pk varchar, fk_doris_increment_code varchar,"+selectSql3;
        selectSql=selectSql+selectSql3+selectSql4.substring(0,selectSql4.length()-1)+")";
        return selectSql;
    }

    public String createStoredProcedure2(ModelMetaDataDTO modelMetaDataDTO){
        String tableName=modelMetaDataDTO.tableName;
        String tableKeyName=modelMetaDataDTO.tableName+"_pk";
        String fieldValue="";
        String storedProcedureSql="CREATE OR REPLACE PROCEDURE public.update"+tableName+"() \n"+
                "LANGUAGE 'plpgsql'\nas $BODY$\nDECLARE\nmysql1 text;\nbegin\n";
         storedProcedureSql+="mysql1:='INSERT INTO "+tableName+" SELECT * FROM('||' "+selectSql(modelMetaDataDTO)+")'||' AS ods ON CONFLICT (" ;
        storedProcedureSql+=tableKeyName+")  DO UPDATE SET ";
        for (String key : modelMetaDataDTO.fieldEnNameMaps.keySet()) {
            fieldValue+=key+"=EXCLUDED."+modelMetaDataDTO.fieldEnNameMaps.get(key)+",";
        }
        storedProcedureSql+=fieldValue.substring(0,fieldValue.length()-1)+"';\n";
        storedProcedureSql+="raise notice'%',mysql1;\nEXECUTE mysql1;\n";
        storedProcedureSql+="end\n$BODY$;\n";
        return storedProcedureSql;
    }

    /**
     * 保存建模相关表结构数据
     * @param dto
     */
    public void saveTableStructure(List<ModelMetaDataDTO> dto)
    {
        try {
            List<TaskPgTableStructurePO> poList=new ArrayList<>();
            for (ModelMetaDataDTO item: dto) {
                Thread.sleep(200);
                //获取时间戳版本号
                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                Calendar calendar = Calendar.getInstance();
                String version = df.format(calendar.getTime());
                for (ModelAttributeMetaDataDTO fieldData:item.dto)
                {
                    TaskPgTableStructurePO po=new TaskPgTableStructurePO();
                    po.version=version;
                    po.tableId= String.valueOf(item.id);
                    po.tableName=item.tableName;
                    po.fieldId=fieldData.fieldId;
                    po.fieldName=fieldData.fieldEnName;
                    po.fieldType=fieldData.fieldType;
                    po.appId=String.valueOf(item.appId);
                    if (fieldData.fieldLength !=0)
                    {
                        po.fieldType=fieldData.fieldType+"("+fieldData.fieldLength+")";
                    }
                    //是否为关联维度
                    if (fieldData.attributeType== DimensionAttributeEnum.ASSOCIATED_DIMENSION.getValue())
                    {
                        po.fieldName=fieldData.associationTable+"_pk";
                        po.fieldType="varchar(100)";
                    }
                    poList.add(po);
                }
            }
            this.saveBatch(poList);
        }
        catch (Exception ex)
        {
            log.error("saveTableStructure:"+ex);
        }
    }

}
