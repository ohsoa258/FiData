package com.fisk.task.consumer.doris;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.davis.client.ApiException;
import com.davis.client.model.*;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.constants.NifiConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.BusinessTypeEnum;
import com.fisk.common.enums.task.SynchronousTypeEnum;
import com.fisk.common.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.enums.ComponentIdTypeEnum;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.modelpublish.ModelPublishDataDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.nifi.*;
import com.fisk.task.dto.task.AppNifiSettingPO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.NifiConfigPO;
import com.fisk.task.dto.task.TableNifiSettingPO;
import com.fisk.task.entity.TBETLIncrementalPO;
import com.fisk.task.entity.TaskDwDimPO;
import com.fisk.task.entity.TaskPgTableStructurePO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.enums.PortComponentEnum;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.mapper.TBETLIncrementalMapper;
import com.fisk.task.mapper.TaskDwDimMapper;
import com.fisk.task.mapper.TaskPgTableStructureMapper;
import com.fisk.task.service.doris.IDorisBuild;
import com.fisk.task.service.nifi.INifiComponentsBuild;
import com.fisk.task.service.nifi.IPostgreBuild;
import com.fisk.task.service.nifi.ITaskDwDim;
import com.fisk.task.service.nifi.impl.AppNifiSettingServiceImpl;
import com.fisk.task.service.pipeline.impl.NifiConfigServiceImpl;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.NifiPositionHelper;
import com.fisk.task.utils.TaskPgTableStructureHelper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
    TaskPgTableStructureMapper taskPgTableStructureMapper;
    @Resource
    INifiComponentsBuild componentsBuild;
    @Value("${pgsql-datamodel.url}")
    public String pgsqlDatamodelUrl;
    @Value("${pgsql-datamodel.username}")
    public String pgsqlDatamodelUsername;
    @Value("${pgsql-datamodel.password}")
    public String pgsqlDatamodelPassword;
    @Value("${pgdw-dblink}")
    public String pgdwDblink;
    @Resource
    TaskPgTableStructureHelper taskPgTableStructureHelper;
    @Resource
    AppNifiSettingServiceImpl appNifiSettingService;
    @Resource
    NifiConfigServiceImpl nifiConfigService;
    @Resource
    TableNifiSettingServiceImpl tableNifiSettingService;
    @Resource
    public DataModelClient dataModelClient;
    @Resource
    PublishTaskController pc;
    @Resource
    private TBETLIncrementalMapper incrementalMapper;
    public String appParentGroupId;
    public String appGroupId;
    public String groupEntityId;
    public String taskGroupEntityId;
    public String appInputPortId;
    public String tableInputPortId;
    public String appOutputPortId;
    public String tableOutputPortId;


    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.DATAMODEL_DORIS_TABLE_MQ_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        ModelPublishStatusDTO modelPublishStatusDTO = new ModelPublishStatusDTO();
        int id=0;
        int tableType=0;
        //saveTableStructure(list);
        //1.,查询语句,并存库
        //2.修改表的存储过程
        //3.生成nifi流程
        try {
        ModelPublishDataDTO inpData = JSON.parseObject(dataInfo, ModelPublishDataDTO.class);
        List<ModelPublishTableDTO> dimensionList = inpData.dimensionList;

            for (ModelPublishTableDTO modelPublishTableDTO:dimensionList) {
                id=modelPublishStatusDTO.id;
                tableType=modelPublishTableDTO.createType;
            //生成版本号
            ResultEnum resultEnum = taskPgTableStructureHelper.saveTableStructure(modelPublishTableDTO);
            log.info("执行存储过程返回结果"+resultEnum.getCode());
                //生成建表语句
                List<String> pgdbTable2 = createPgdbTable2(modelPublishTableDTO);
                iPostgreBuild.postgreBuildTable(pgdbTable2.get(0), BusinessTypeEnum.DATAMODEL);
                if (resultEnum.getCode()==ResultEnum.TASK_TABLE_NOT_EXIST.getCode())
            {
                iPostgreBuild.postgreBuildTable(pgdbTable2.get(1), BusinessTypeEnum.DATAMODEL);
            }
            //生成函数,并执行
            /*String storedProcedure3 = createStoredProcedure3(modelPublishTableDTO);
            log.info("dw库生成函数:"+storedProcedure3);
            iPostgreBuild.postgreBuildTable(storedProcedure3, BusinessTypeEnum.DATAMODEL);*/
                log.info("开始执行nifi创建数据同步");
                TBETLIncrementalPO ETLIncremental=new TBETLIncrementalPO();
                ETLIncremental.object_name=modelPublishTableDTO.tableName;
                ETLIncremental.enable_flag="1";
                ETLIncremental.incremental_objectivescore_batchno=UUID.randomUUID().toString();
                Map<String, Object> conditionHashMap = new HashMap<>();
                conditionHashMap.put("object_name",ETLIncremental.object_name);
                List<TBETLIncrementalPO> tbetlIncrementalPos = incrementalMapper.selectByMap(conditionHashMap);
                if(tbetlIncrementalPos!=null&&tbetlIncrementalPos.size()>0){
                    log.info("此表已有同步记录,无需重复添加");
                }else{
                    incrementalMapper.insert(ETLIncremental);
                }
                BuildNifiFlowDTO bfd=new BuildNifiFlowDTO();
                bfd.userId=inpData.userId;
                bfd.appId=inpData.businessAreaId;
                bfd.synchronousTypeEnum= SynchronousTypeEnum.PGTOPG;
                //来源为数据接入
                bfd.dataClassifyEnum= DataClassifyEnum.DATAMODELING;
                bfd.id=modelPublishTableDTO.tableId;
                bfd.tableName=modelPublishTableDTO.tableName;
                bfd.selectSql=modelPublishTableDTO.sqlScript;
                //同步方式
                bfd.synMode=modelPublishTableDTO.synMode;
                bfd.queryStartTime = modelPublishTableDTO.queryStartTime;
                bfd.queryEndTime = modelPublishTableDTO.queryEndTime;
                bfd.appName = inpData.businessAreaName;
            if (modelPublishTableDTO.createType == 0) {
                //类型为物理表
                bfd.type= OlapTableEnum.DIMENSION;
            } else {
                //类型为物理表
                bfd.type= OlapTableEnum.FACT;
            }
                log.info("nifi传入参数："+JSON.toJSONString(bfd));
                TableNifiSettingPO one = tableNifiSettingService.query().eq("app_id", bfd.appId).eq("table_access_id", bfd.id).eq("type", bfd.type.getValue()).one();
                TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
                if(one!=null){
                    tableNifiSettingPO=one;
                }
                tableNifiSettingPO.appId= Math.toIntExact(bfd.appId);
                tableNifiSettingPO.tableName=bfd.tableName;
                tableNifiSettingPO.tableAccessId= Math.toIntExact(bfd.id);
                tableNifiSettingPO.selectSql=bfd.selectSql;
                tableNifiSettingPO.type=bfd.type.getValue();
                tableNifiSettingPO.syncMode=1;
                tableNifiSettingService.saveOrUpdate(tableNifiSettingPO);
                pc.publishBuildNifiFlowTask(bfd);
                log.info("执行完成");
            //0维度表
            if(modelPublishTableDTO.createType==0){
                modelPublishStatusDTO.status=1;
                modelPublishStatusDTO.id= Math.toIntExact(modelPublishTableDTO.tableId);
                modelPublishStatusDTO.type=0;
                dataModelClient.updateDimensionPublishStatus(modelPublishStatusDTO);
            }else{
                modelPublishStatusDTO.status=1;
                modelPublishStatusDTO.id= Math.toIntExact(modelPublishTableDTO.tableId);
                modelPublishStatusDTO.type=0;
                dataModelClient.updateFactPublishStatus(modelPublishStatusDTO);
            }

        }


        }catch (Exception e){
            log.error("dw发布失败,表id为"+id+e.getMessage());
            if(tableType==0){
                modelPublishStatusDTO.status=1;
                modelPublishStatusDTO.id= Math.toIntExact(id);
                modelPublishStatusDTO.type=0;
                dataModelClient.updateDimensionPublishStatus(modelPublishStatusDTO);
            }else{
                modelPublishStatusDTO.status=1;
                modelPublishStatusDTO.id= Math.toIntExact(id);
                modelPublishStatusDTO.type=0;
                dataModelClient.updateFactPublishStatus(modelPublishStatusDTO);
            }
        }

    }

    public String createStoredProcedure3(ModelPublishTableDTO modelPublishTableDTO){
        String tableName=modelPublishTableDTO.tableName;
        List<ModelPublishFieldDTO> fieldList1 = modelPublishTableDTO.fieldList;
        String tablePk="";
        if(modelPublishTableDTO.createType==0){
            tablePk=modelPublishTableDTO.tableName.substring(4)+"key";
        }else{
            tablePk=modelPublishTableDTO.tableName.substring(5)+"key";
        }
        String fileds=tablePk+",";
        for (ModelPublishFieldDTO modelPublishFieldDTO:fieldList1) {
            fileds+=""+modelPublishFieldDTO.fieldEnName+",";
        }
        String truncateTable="'truncate table "+tableName+"';\n";
        fileds+="fi_createtime,fi_updatetime";
        String storedProcedureSql="CREATE OR REPLACE PROCEDURE public.update"+tableName+"() \n"+
                "LANGUAGE 'plpgsql'\nas $BODY$\nDECLARE\nmysqlp text;\nmysqlk text;\nbegin\n";
        storedProcedureSql+="mysqlk:="+truncateTable+"mysqlp:='INSERT INTO "+tableName+" ("+fileds+") SELECT "+fileds+" FROM('||' "+selectSql1(modelPublishTableDTO);
        storedProcedureSql+="\nraise notice'%',mysqlk;\nEXECUTE mysqlk;\nraise notice'%',mysqlp;\nEXECUTE mysqlp;\n";
        storedProcedureSql+="end\n$BODY$;\n";
        List<ModelPublishFieldDTO> fieldList = modelPublishTableDTO.fieldList;
        //找到不同的关联表
        List<ModelPublishFieldDTO> collect1 = fieldList.stream().filter(e -> e.associateDimensionName != null).collect(Collectors.toList());
        List<ModelPublishFieldDTO> modelPublishFieldDTOS = collect1.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ModelPublishFieldDTO::getAssociateDimensionName))), ArrayList::new));
        modelPublishFieldDTOS.removeAll(Collections.singleton(null));
        if(modelPublishFieldDTOS.size()!=0){
            int i=0;
            for (ModelPublishFieldDTO modelPublishFieldDTO:modelPublishFieldDTOS) {
                //找到每个关联表关联的所有字段
                List<ModelPublishFieldDTO> collect = fieldList.stream().filter(e -> e.associateDimensionName != null && e.associateDimensionName.equals(modelPublishFieldDTO.associateDimensionName)).collect(Collectors.toList());
                //拼接语句,添加外键
                if(modelPublishFieldDTO.associateDimensionFieldName!=null){
                    storedProcedureSql=storedProcedureSql.replace("DECLARE\n","DECLARE\nmysql"+i+" text;\n");
                    //	UPDATE fact_InternetSales1
                    //  SET Date1key=d.Date1key
                    // FROM dim_Date1 as d
                    //  WHERE fact_InternetSales1.orderdate=d.fulldatealternatekey
                    String associateDimensionName = modelPublishFieldDTO.associateDimensionName;
                    String associateDimensionNamePK = modelPublishFieldDTO.associateDimensionName.substring(4)+"key";
                    String sql="update "+modelPublishTableDTO.tableName +" set  "+associateDimensionNamePK+
                            "="+associateDimensionName+"."+associateDimensionNamePK +" from "+associateDimensionName +" where ";
                            //条件

                    for(int j=0;j<collect.size();j++){
                        ModelPublishFieldDTO modelPublishFieldDTO1 = collect.get(j);
                        sql+=modelPublishTableDTO.tableName+"."+modelPublishFieldDTO1.fieldEnName+"="+modelPublishFieldDTO1.associateDimensionName+"."+modelPublishFieldDTO1.associateDimensionFieldName+" and ";
                    }
                    sql=sql.substring(0,sql.length()-4);
                    storedProcedureSql=storedProcedureSql.replace("begin\n","begin\nmysql"+i+":='"+sql+"';\n");
                    storedProcedureSql=storedProcedureSql.replace("EXECUTE mysqlp;\n","EXECUTE mysqlp;\n\nraise notice'%',mysql"+i+";\nEXECUTE mysql"+i+";\n");
                }
                i++;
            }
        }
        return storedProcedureSql;
    }

    public String selectSql1(ModelPublishTableDTO modelPublishTableDTO){
        String tableName = modelPublishTableDTO.tableName;
        String tablePk="";
        if(modelPublishTableDTO.createType==0){
             tablePk=modelPublishTableDTO.tableName.substring(4)+"key";
        }else{
            tablePk=modelPublishTableDTO.tableName.substring(5)+"key";
        }
        String selectSql="select * from dblink('||'''"+pgdwDblink+"'''||','||'''";
        String selectSql1="select sys_guid() as "+tablePk+", ";
        StringBuilder selectSql2=new StringBuilder("coalesce( fi_createtime,null),coalesce( fi_updatetime,null),");
        StringBuilder selectSql3=new StringBuilder(tablePk +" varchar,fi_createtime varchar,fi_updatetime varchar,");
        StringBuilder selectSql4=new StringBuilder(tablePk+"=EXCLUDED."+tablePk+",fi_createtime=EXCLUDED.fi_createtime,fi_updatetime=EXCLUDED.fi_updatetime,");
        StringBuilder selectSql5=new StringBuilder();
        StringBuilder selectSql6=new StringBuilder(" ("+modelPublishTableDTO.sqlScript+") fi1 ");
        List<ModelPublishFieldDTO> fieldList = modelPublishTableDTO.fieldList;
        fieldList.forEach((l) -> {
            log.info("字段属性为:"+l);
            if(l.fieldType.toLowerCase().contains("float")){
                selectSql2.append("coalesce("+l.sourceFieldName+" ,0.00),");
                selectSql3.append(""+l.fieldEnName+" numeric ,");
            }else if(l.fieldType.toLowerCase().contains("varchar")){
                selectSql2.append("coalesce("+l.sourceFieldName+" ,null),");
                selectSql3.append(""+l.fieldEnName+" VARCHAR ,");
            }else if(l.fieldType.toLowerCase().contains("int")){
                selectSql2.append("coalesce("+l.sourceFieldName+" ,0),");
                selectSql3.append(""+l.fieldEnName+" int ,");
            }else if(l.fieldType.toLowerCase().contains("numeric")){
                selectSql2.append("coalesce("+l.sourceFieldName+" ,0.00),");
                selectSql3.append(""+l.fieldEnName+" numeric ,");
            }

            selectSql4.append(""+l.fieldEnName+"=EXCLUDED."+l.fieldEnName+",");
            if(l.isPrimaryKey==1){
                selectSql5.append(","+l.fieldEnName+"");
            }

            //多表
            /*if(l.associateDimensionName!=null){
            String filed=l.associateDimensionName.substring(4)+"_pk ";
                selectSql2.append(filed);
                selectSql3.append(filed+" varchar,");
                selectSql4.append(filed+"=EXCLUDED."+filed+",");
                String uuid=UUID.randomUUID().toString().replaceAll("-","");
                selectSql6.append(" left join ( select * from "+l.associateDimensionName+" ) fi"+uuid+" on fi1."+l.fieldEnName+"= fi"+uuid+"."+l.associateDimensionFieldName);
            }*/
        });
        String sql4 = selectSql6.toString();
        String sql = selectSql2.toString();
        sql=sql.substring(0,sql.length()-1);
        sql+=" from  "+sql4+" '''||') as t (";
        String sql1 = selectSql3.toString();
        String sql3 = selectSql5.toString();
        if(sql3.length()!=0){
        tablePk=sql3.substring(1);
        }
        sql+=sql1.substring(0,sql1.length()-1)+"))'||' AS ods ON CONFLICT ( "+tablePk+" )  DO UPDATE SET ";
        String sql2 = selectSql4.toString();
        sql+=sql2.substring(0,sql2.length()-1)+"';";
        selectSql+=selectSql1+sql;
        return selectSql;
    }

    public List<String> createPgdbTable2(ModelPublishTableDTO modelPublishTableDTO){
        List<String> sqlList = new ArrayList<>();
        List<ModelPublishFieldDTO> fieldList = modelPublishTableDTO.fieldList;
        String tableName = modelPublishTableDTO.tableName;
        String tablePk="";
        if(modelPublishTableDTO.createType==0){
            tablePk=tableName.substring(4)+"key";
        }else{
            tablePk=tableName.substring(5)+"key";
        }

        StringBuilder sql = new StringBuilder();
        StringBuilder pksql=new StringBuilder("PRIMARY KEY ( ");
        sql.append("CREATE TABLE "+modelPublishTableDTO.tableName+" ( "+tablePk+" varchar(50), ");
        StringBuilder sqlFileds = new StringBuilder();
        StringBuilder sqlFileds1 = new StringBuilder();
        StringBuilder stgSqlFileds = new StringBuilder();
        log.info("pg_dw建表字段信息:"+fieldList);
        fieldList.forEach((l) -> {
            if(l.fieldType.contains("INT")||l.fieldType.contains("TEXT")){
                sqlFileds.append( ""+l.fieldEnName + " " + l.fieldType.toLowerCase() + ",");
                stgSqlFileds.append(l.fieldEnName+" text,");
            }else if(l.fieldType.toLowerCase().contains("numeric")||l.fieldType.toLowerCase().contains("float")){
                sqlFileds.append( ""+l.fieldEnName + " float ,");
                stgSqlFileds.append(l.fieldEnName+" text,");
            }else{
                sqlFileds.append( ""+l.fieldEnName + " " + l.fieldType.toLowerCase() + "("+l.fieldLength+") ,");
                stgSqlFileds.append(l.fieldEnName+" text,");
            }
            /*if(l.isPrimaryKey==1){
                pksql.append(""+l.fieldEnName+" ,");
            }*/

        });
        pksql.append(tablePk+",");
        String sql1 = sql.toString();
        String associatedKey = associatedConditions(fieldList);
        String sql2 = sqlFileds.toString() + associatedKey;
        sql2+="fi_createtime varchar(50),fi_updatetime varchar(50),";
        String sql3 = sqlFileds1.toString();
        String sql4 = pksql.toString();
        if(Objects.equals("",sql3)){
            sql1+=sql2+sql4.substring(0,sql4.length()-1)+"));";
        }else{
            sql1+=sql2+sql3+sql4.substring(0,sql4.length()-1)+"));";
        }

        //创建表
        log.info("pg_dw建表语句"+sql1);
        //String stgTable = sql1.replaceFirst(tableName, "stg_" + tableName);
        String stgTable = "DROP TABLE IF EXISTS stg_" + tableName + "; CREATE TABLE stg_" + tableName + " (" + tablePk + " varchar(50) NOT NULL DEFAULT sys_guid()," + stgSqlFileds.toString() + associatedKey + "fi_createtime varchar(50) DEFAULT to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss'),fi_updatetime varchar(50),enableflag varchar(50))";
        sqlList.add(stgTable);
        sqlList.add(sql1);
        HashMap<String, Object> map = new HashMap<>();
        map.put("table_name",tableName);
        taskDwDimMapper.deleteByMap(map);
            TaskDwDimPO taskDwDimPO = new TaskDwDimPO();
            //taskDwDimPO.areaBusinessName=businessAreaName;//业务域名
            taskDwDimPO.sqlContent=sql1;//创建表的sql
            taskDwDimPO.tableName=tableName;
            taskDwDimPO.storedProcedureName="update"+tableName+"()";
            taskDwDimMapper.insert(taskDwDimPO);
            return sqlList;
    }

    public String associatedConditions(List<ModelPublishFieldDTO> fieldList) {
        String filed = "";
        List<ModelPublishFieldDTO> collect1 = fieldList.stream().filter(e -> e.associateDimensionName != null).collect(Collectors.toList());
        List<ModelPublishFieldDTO> modelPublishFieldDTOS = collect1.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ModelPublishFieldDTO::getAssociateDimensionName))), ArrayList::new));
        modelPublishFieldDTOS.removeAll(Collections.singleton(null));
        if (modelPublishFieldDTOS.size() != 0) {
            for (ModelPublishFieldDTO modelPublishFieldDTO : modelPublishFieldDTOS) {
                filed += modelPublishFieldDTO.associateDimensionName.substring(4) + "key varchar(50),";
            }
        }
        return filed;
    }

    public void createNiFiFlow(ModelPublishDataDTO modelPublishDataDTO,ModelPublishTableDTO modelMetaDataDTO,String businessAreaName,DataClassifyEnum dataClassifyEnum,OlapTableEnum olapTableEnum){
        BuildDbControllerServiceDTO buildDbControllerServiceDTO = new BuildDbControllerServiceDTO();
        //数据连接池
        ControllerServiceEntity  data=new ControllerServiceEntity();
        //应用组
        ProcessGroupEntity data1=new ProcessGroupEntity();
        //任务组
        ProcessGroupEntity data2=new ProcessGroupEntity();

        //4. 创建任务组创建时要把原任务组删掉,防止重复发布带来影响  dto.id, dto.appId
        DataModelVO dataModelVO = new DataModelVO();
        dataModelVO.dataClassifyEnum=dataClassifyEnum;
        dataModelVO.delBusiness=false;
        dataModelVO.businessId=String.valueOf(modelPublishDataDTO.businessAreaId);
        DataModelTableVO dataModelTableVO = new DataModelTableVO();
        dataModelTableVO.type=olapTableEnum;
        List<Long> ids = new ArrayList<>();
        ids.add(modelMetaDataDTO.tableId);
        dataModelTableVO.ids=ids;
        dataModelVO.indicatorIdList=dataModelTableVO;
        if(modelPublishDataDTO.nifiCustomWorkflowId==null){
            componentsBuild.deleteNifiFlow(dataModelVO);
        }
        AppNifiSettingPO appNifiSettingPO = new AppNifiSettingPO();
        if(modelPublishDataDTO.nifiCustomWorkflowId!=null){
            appNifiSettingPO = appNifiSettingService.query().eq("app_id", modelPublishDataDTO.businessAreaId).eq("nifi_custom_workflow_id",modelPublishDataDTO.nifiCustomWorkflowId).eq("type", dataClassifyEnum.getValue()).eq("del_flag",1).one();

        }else{
             appNifiSettingPO = appNifiSettingService.query().eq("app_id", modelPublishDataDTO.businessAreaId).eq("type", dataClassifyEnum.getValue()).eq("del_flag",1).one();

        }
        if(appNifiSettingPO!=null&&modelMetaDataDTO.groupComponentId==null){
            try {
                data1=NifiHelper.getProcessGroupsApi().getProcessGroup(appNifiSettingPO.appComponentId);
            } catch (ApiException e) {
                log.error("组查询失败"+e);
            }
            data.setId(appNifiSettingPO.targetDbPoolComponentId);

        } else{
            //创建应用组

            BuildProcessGroupDTO dto = new BuildProcessGroupDTO();
            dto.name = modelPublishDataDTO.businessAreaName;
            dto.details = modelPublishDataDTO.businessAreaName;
            //根据组个数，定义坐标
            int count=0;
            if(modelMetaDataDTO.groupComponentId!=null){
                 count = componentsBuild.getGroupCount(modelMetaDataDTO.groupComponentId);
                 dto.groupId=modelMetaDataDTO.groupComponentId;
            }else{
                NifiConfigPO nifiConfigPO = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.DAILY_NIFI_FLOW_GROUP_ID.getName()).one();
                if (nifiConfigPO != null) {
                    dto.groupId = nifiConfigPO.componentId;
                } else {
                    BuildProcessGroupDTO buildProcessGroupDTO = new BuildProcessGroupDTO();
                    buildProcessGroupDTO.name = ComponentIdTypeEnum.DAILY_NIFI_FLOW_GROUP_ID.getName();
                    buildProcessGroupDTO.details = ComponentIdTypeEnum.DAILY_NIFI_FLOW_GROUP_ID.getName();
                    int groupCount = componentsBuild.getGroupCount(NifiConstants.ApiConstants.ROOT_NODE);
                    buildProcessGroupDTO.positionDTO = NifiPositionHelper.buildXPositionDTO(groupCount);
                    BusinessResult<ProcessGroupEntity> processGroupEntityBusinessResult = componentsBuild.buildProcessGroup(buildProcessGroupDTO);
                    if (processGroupEntityBusinessResult.success) {
                        dto.groupId = processGroupEntityBusinessResult.data.getId();
                        NifiConfigPO nifiConfigPO1 = new NifiConfigPO();
                        nifiConfigPO1.componentId=dto.groupId;
                        nifiConfigPO1.componentKey=ComponentIdTypeEnum.DAILY_NIFI_FLOW_GROUP_ID.getName();
                        nifiConfigService.save(nifiConfigPO1);
                    } else {
                        throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, processGroupEntityBusinessResult.msg);
                    }
                }
                 count = componentsBuild.getGroupCount(dto.groupId);
            }

            dto.positionDTO = NifiPositionHelper.buildXPositionDTO(count);
            BusinessResult<ProcessGroupEntity> res = componentsBuild.buildProcessGroup(dto);
            if (res.success) {
                data1 = res.data;
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
            }
            buildDbControllerServiceDTO.driverLocation= NifiConstants.DriveConstants.POSTGRESQL_DRIVE_PATH;
            buildDbControllerServiceDTO.conUrl=pgsqlDatamodelUrl;
            buildDbControllerServiceDTO.driverName= DriverTypeEnum.POSTGRESQL.getName();
            buildDbControllerServiceDTO.pwd=pgsqlDatamodelPassword;
            buildDbControllerServiceDTO.name=businessAreaName;
            buildDbControllerServiceDTO.enabled = true;
       /*     if(modelMetaDataDTO.groupStructureId!=null){
                buildDbControllerServiceDTO.groupId = modelMetaDataDTO.groupStructureId;
            }else{
                buildDbControllerServiceDTO.groupId = data1.getId();
            }*/
            buildDbControllerServiceDTO.groupId=NifiConstants.ApiConstants.ROOT_NODE;
            buildDbControllerServiceDTO.details=businessAreaName;
            buildDbControllerServiceDTO.user=pgsqlDatamodelUsername;
            NifiConfigPO nifiConfigPO = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.PG_DW_DB_POOL_COMPONENT_ID.getName()).one();
            if(nifiConfigPO!=null){
                data.setId(nifiConfigPO.componentId);
            }else{
                BusinessResult<ControllerServiceEntity> controllerServiceEntityBusinessResult = componentsBuild.buildDbControllerService(buildDbControllerServiceDTO);
                if (controllerServiceEntityBusinessResult.success) {
                    data = controllerServiceEntityBusinessResult.data;
                    NifiConfigPO SaveNifiConfigPO = new NifiConfigPO();
                    SaveNifiConfigPO.componentId =  controllerServiceEntityBusinessResult.data.getId();
                    SaveNifiConfigPO.componentKey = ComponentIdTypeEnum.PG_DW_DB_POOL_COMPONENT_ID.getName();
                    nifiConfigService.save(SaveNifiConfigPO);
                } else {
                    throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, controllerServiceEntityBusinessResult.msg);
                }
            }

            // TODO: 创建input组件功能(第一层应用)
            ProcessGroupEntity processGroupData1 = null;
            try {
                processGroupData1 = NifiHelper.getProcessGroupsApi().getProcessGroup(data1.getId());

            } catch (ApiException e) {
                log.error(e.getMessage());
            }
            assert processGroupData1 != null;
            appParentGroupId = processGroupData1.getComponent().getParentGroupId();

//            appInputPortId = buildPortComponent(processGroupData1.getComponent().getName(), appParentGroupId, processGroupData1.getPosition().getX(),
//                    processGroupData1.getPosition().getY(), PortComponentEnum.APP_INPUT_PORT_COMPONENT);

            // TODO: 创建output组件功能(第一层应用)
//            appOutputPortId = buildPortComponent(processGroupData1.getComponent().getName(), appParentGroupId, processGroupData1.getPosition().getX(),
//                    processGroupData1.getPosition().getY(), PortComponentEnum.APP_OUTPUT_PORT_COMPONENT);

        }

        /*TableNifiSettingPO tableNifiSettingPO = tableNifiSettingService.query().eq("app_id", modelMetaDataDTO.appId).eq("table_access_id", modelMetaDataDTO.id).eq("type", olapTableEnum.getValue()).one();
        if(tableNifiSettingPO!=null){
           data2.setId(tableNifiSettingPO.tableComponentId);
        }else{*/
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
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res1.msg);
            }

            // TODO: 创建input组件功能(第二层表)
            ProcessGroupEntity processGroupData2 = null;
            try {
                processGroupData2 = NifiHelper.getProcessGroupsApi().getProcessGroup(data2.getId());

            } catch (ApiException e) {
                log.error(e.getMessage());
            }
            assert processGroupData2 != null;
            appParentGroupId = processGroupData2.getComponent().getParentGroupId();

            // TODO: 创建output组件功能(第二层表)
            // 创建input_port(任务)   (后期入库)
            tableInputPortId = buildPortComponent(processGroupData2.getComponent().getName(), appParentGroupId,
                    processGroupData2.getPosition().getX(), processGroupData2.getPosition().getY(), PortComponentEnum.TASK_INPUT_PORT_COMPONENT);
            // 创建output_port(任务)   (后期入库)
            tableOutputPortId = buildPortComponent(processGroupData2.getComponent().getName(), appParentGroupId,
                    processGroupData2.getPosition().getX(), processGroupData2.getPosition().getY(), PortComponentEnum.TASK_OUTPUT_PORT_COMPONENT);

        //}

        //创建组件,启动组件
        TableNifiSettingPO tableNifiSetting = new TableNifiSettingPO();
        String  Componentid= createComponents(modelPublishDataDTO,data2.getId(), data.getId(), "update"+modelMetaDataDTO.tableName+"()",data1,tableNifiSetting);

        //回写
        savaNifiAllSetting(modelPublishDataDTO,data,data1,data2,Componentid, modelMetaDataDTO,dataClassifyEnum,olapTableEnum, tableNifiSetting);

    }

    public void savaNifiAllSetting(ModelPublishDataDTO ModelPublishDataDTO,ControllerServiceEntity controllerServiceEntity,ProcessGroupEntity processGroupEntity1,ProcessGroupEntity processGroupEntity2,String Componentid,ModelPublishTableDTO modelMetaDataDTO,DataClassifyEnum dataClassifyEnum,OlapTableEnum olapTableEnum,TableNifiSettingPO tableNifiSettingPO){
        AppNifiSettingPO appNifiSettingPO = new AppNifiSettingPO();
        AppNifiSettingPO appNifiSettingPO1 = new AppNifiSettingPO();
        if(ModelPublishDataDTO.nifiCustomWorkflowId==null){
             appNifiSettingPO1 = appNifiSettingService.query().eq("app_id", ModelPublishDataDTO.businessAreaId).eq("type", dataClassifyEnum.getValue()).eq("del_flag",1).one();
        }else{
            appNifiSettingPO1 = appNifiSettingService.query().eq("app_id", ModelPublishDataDTO.businessAreaId).eq("nifi_custom_workflow_id",ModelPublishDataDTO.nifiCustomWorkflowId).eq("type", dataClassifyEnum.getValue()).eq("del_flag",1).one();
        }
        if(appNifiSettingPO1!=null){
            appNifiSettingPO=appNifiSettingPO1;
        }
        appNifiSettingPO.targetDbPoolComponentId=controllerServiceEntity.getId();
        appNifiSettingPO.appId= String.valueOf(ModelPublishDataDTO.businessAreaId);
        appNifiSettingPO.type=dataClassifyEnum.getValue();
        //做判断,是否新增
        appNifiSettingPO.appComponentId=processGroupEntity1.getId();
        appNifiSettingPO.nifiCustomWorkflowId=ModelPublishDataDTO.nifiCustomWorkflowId;
        appNifiSettingService.saveOrUpdate(appNifiSettingPO);
        Map<String, Object>  queryCondition= new HashMap<>();
        queryCondition.put("app_id",ModelPublishDataDTO.businessAreaId);
        queryCondition.put("table_access_id",modelMetaDataDTO.tableId);
        queryCondition.put("type",olapTableEnum.getValue());
        if(modelMetaDataDTO.nifiCustomWorkflowDetailId!=null&&!Objects.equals(modelMetaDataDTO.nifiCustomWorkflowDetailId,"null")){
            queryCondition.put("nifi_custom_workflow_detail_id",modelMetaDataDTO.nifiCustomWorkflowDetailId);
            tableNifiSettingPO.nifiCustomWorkflowDetailId=modelMetaDataDTO.nifiCustomWorkflowDetailId;
        }
        tableNifiSettingService.removeByMap(queryCondition);
        tableNifiSettingPO.tableAccessId= Math.toIntExact(modelMetaDataDTO.tableId);
        tableNifiSettingPO.tableName=modelMetaDataDTO.tableName;
        tableNifiSettingPO.appId= Math.toIntExact(ModelPublishDataDTO.businessAreaId);
        tableNifiSettingPO.selectSql="call update"+modelMetaDataDTO.tableName+"()";
        tableNifiSettingPO.queryIncrementProcessorId=Componentid;
        tableNifiSettingPO.tableComponentId=processGroupEntity2.getId();
        tableNifiSettingPO.type=olapTableEnum.getValue();
        tableNifiSettingService.saveOrUpdate(tableNifiSettingPO);
    }

    /*
    * 创建组件
    * */
    public String createComponents(ModelPublishDataDTO ModelPublishDataDTO,String groupId,String componentId,String executsql,ProcessGroupEntity data1,TableNifiSettingPO tableNifiSetting){
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
        log.info("组件id为:"+processorEntityBusinessResult.data.getId());
        processors.add(processorEntityBusinessResult.data);
        if(ModelPublishDataDTO.nifiCustomWorkflowId!=null){
            List<ProcessorEntity> processorEntities = componentsBuild.enabledProcessor(groupId, processors);
        }

        ProcessorEntity processor = processorEntityBusinessResult.data;

        appGroupId = data1.getComponent().getParentGroupId();

        // TODO 创建input_port(组)   (后期入库)
        String inputPortId = buildPortComponent(processor.getComponent().getName(), processor.getComponent().getParentGroupId(),
                processor.getPosition().getX(), processor.getPosition().getY(), PortComponentEnum.COMPONENT_INPUT_PORT_COMPONENT);

        // TODO 创建output_port(组)   (后期入库)
        String outputPortId = buildPortComponent(processor.getComponent().getName(), processor.getComponent().getParentGroupId(),
                processor.getPosition().getX(), processor.getPosition().getY(), PortComponentEnum.COMPONENT_OUTPUT_PORT_COMPONENT);


        // ===============================================================================================================
        // TODO 创建input_port connection(组)
        String componentInputPortConnectionId = buildPortConnection(processor.getComponent().getParentGroupId(),
                processor.getComponent().getParentGroupId(), processor.getId(), ConnectableDTO.TypeEnum.PROCESSOR,
                processor.getComponent().getParentGroupId(), inputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION);

        // TODO 创建output_port connection(组)
        String componentOutputPortConnectionId = buildPortConnection(processor.getComponent().getParentGroupId(),
                processor.getComponent().getParentGroupId(), outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
                processor.getComponent().getParentGroupId(), processor.getId(), ConnectableDTO.TypeEnum.PROCESSOR,
                3, PortComponentEnum.COMPONENT_OUTPUT_PORT_CONNECTION);

        // TODO 创建input_port connection(任务)
        String taskInputPortConnectionId = buildPortConnection(data1.getId(),
                groupId, inputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                data1.getId(), tableInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                0, PortComponentEnum.TASK_INPUT_PORT_CONNECTION);

        // TODO 创建output connection(任务)
        String taskOutputPortConnectionId = buildPortConnection(data1.getId(),
                data1.getId(), tableOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
                groupId, outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
                2, PortComponentEnum.TASK_OUTPUT_PORT_CONNECTION);

        // TODO 创建input_port connection(应用)
//        String appInputPortConnectionId = buildPortConnection(appGroupId,
//                data1.getId(), tableInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
//                appGroupId, appInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
//                0, PortComponentEnum.APP_INPUT_PORT_CONNECTION);

        // TODO 创建output connection(应用)
//        String appOutputPortConnectionId = buildPortConnection(appGroupId,
//                appGroupId, appOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
//                data1.getId(), tableOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
//                1, PortComponentEnum.APP_OUTPUT_PORT_CONNECTION);

        tableNifiSetting.processorInputPortConnectId=componentInputPortConnectionId;
        tableNifiSetting.processorOutputPortConnectId=componentOutputPortConnectionId;
        tableNifiSetting.tableInputPortConnectId=taskInputPortConnectionId;
        tableNifiSetting.tableOutputPortConnectId=taskOutputPortConnectionId;
        tableNifiSetting.tableInputPortId=tableInputPortId;
        tableNifiSetting.tableOutputPortId=tableOutputPortId;
        tableNifiSetting.processorInputPortId=inputPortId;
        tableNifiSetting.processorOutputPortId=outputPortId;
        return processorEntityBusinessResult.data.getId();
    }






    /**
     * 创建input_port/output_port组件
     * @param portName 组件名称
     * @param componentId 上级id
     * @param componentX 坐标
     * @param componentY 坐标
     * @param typeEnum 组件类型
     * @return 生成的组件id
     */
    private String buildPortComponent(String portName, String componentId, Double componentX, Double componentY, PortComponentEnum typeEnum) {

        BuildPortDTO buildPortDTO = new BuildPortDTO();
        PortEntity portEntity;

        switch (typeEnum.getValue()) {
            // 创建input_port(应用)
            case 0:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_APP_SUFFIX;
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX;
                buildPortDTO.componentY = componentY + NifiConstants.PortConstants.INPUT_PORT_Y / 2 + NifiConstants.PortConstants.INPUT_PORT_OFFSET_Y;
                portEntity = componentsBuild.buildInputPort(buildPortDTO);
                return portEntity.getId();
            // 创建output_port(应用)
            case 1:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_APP_SUFFIX;
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX;
                buildPortDTO.componentY = componentY + NifiConstants.PortConstants.OUTPUT_PORT_Y;
                portEntity = componentsBuild.buildOutputPort(buildPortDTO);
                return portEntity.getId();
            // 创建input_port(任务)
            case 2:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_TABLE_SUFFIX + new Date().getTime();
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX / NifiConstants.AttrConstants.POSITION_X;
                buildPortDTO.componentY = componentY / NifiConstants.AttrConstants.POSITION_X + NifiConstants.PortConstants.INPUT_PORT_Y;
                portEntity = componentsBuild.buildInputPort(buildPortDTO);
                return portEntity.getId();
            // 创建output_port(任务)
            case 3:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_TABLE_SUFFIX + new Date().getTime();
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX / NifiConstants.AttrConstants.POSITION_X;
                buildPortDTO.componentY = componentY / NifiConstants.AttrConstants.POSITION_X + NifiConstants.PortConstants.OUTPUT_PORT_Y;
                portEntity = componentsBuild.buildOutputPort(buildPortDTO);
                return portEntity.getId();
            // 创建input_port(组)
            case 4:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_FIELD_SUFFIX + new Date().getTime();
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX;
                buildPortDTO.componentY = componentY + NifiConstants.PortConstants.INPUT_PORT_Y;
                portEntity = componentsBuild.buildInputPort(buildPortDTO);
                return portEntity.getId();
            // 创建output_port(组)
            case 5:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_FIELD_SUFFIX + new Date().getTime();
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX;
                buildPortDTO.componentY = componentY + NifiConstants.PortConstants.OUTPUT_PORT_Y;
                portEntity = componentsBuild.buildOutputPort(buildPortDTO);
                return portEntity.getId();
            default:
                break;
        }
        return null;
    }

    /**
     * 创建input_port/output_port connections
     * @param fatherComponentId 当前组件父id
     * @param destinationGroupId destinationGroupId
     * @param destinationId destinationId
     * @param destinationTypeEnum destinationTypeEnum
     * @param sourceGroupId sourceGroupId
     * @param sourceId sourceId
     * @param sourceTypeEnum sourceTypeEnum
     * @param level level
     * @param typeEnum typeEnum
     * @return connection id
     */
    private String buildPortConnection(String fatherComponentId, String destinationGroupId, String destinationId, ConnectableDTO.TypeEnum destinationTypeEnum,
                                       String sourceGroupId, String sourceId, ConnectableDTO.TypeEnum sourceTypeEnum, int level, PortComponentEnum typeEnum) {
        BuildConnectDTO buildConnectDTO = new BuildConnectDTO();
        NifiConnectDTO destination = new NifiConnectDTO();
        NifiConnectDTO source = new NifiConnectDTO();
        ConnectionEntity connectionEntity;
        switch (typeEnum.getValue()) {
            // 创建input_port连接(应用)
            case 6:
                // 创建input_port连接(任务)
            case 8:
                // 创建input_port连接(组)
            case 10:
                buildConnectDTO.fatherComponentId = fatherComponentId;
                destination.groupId = destinationGroupId;
                destination.id = destinationId;
                destination.typeEnum = destinationTypeEnum;
                source.groupId = sourceGroupId;
                source.id = sourceId;
                source.typeEnum = sourceTypeEnum;
                buildConnectDTO.destination = destination;
                buildConnectDTO.source = source;
                connectionEntity = componentsBuild.buildInputPortConnections(buildConnectDTO);
                return connectionEntity.getId();
            // 创建output_port连接(应用)
            case 7:
                // 创建output_port连接(任务)
            case 9:
                // 创建output_port连接(组)
            case 11:
                buildConnectDTO.fatherComponentId = fatherComponentId;
                destination.groupId = destinationGroupId;
                destination.id = destinationId;
                destination.typeEnum = destinationTypeEnum;
                source.groupId = sourceGroupId;
                source.id = sourceId;
                source.typeEnum = sourceTypeEnum;
                buildConnectDTO.level = level;
                buildConnectDTO.destination = destination;
                buildConnectDTO.source = source;
                connectionEntity = componentsBuild.buildOutPortPortConnections(buildConnectDTO);
                return connectionEntity.getId();
            default:
                break;
        }
        return null;
    }

}
