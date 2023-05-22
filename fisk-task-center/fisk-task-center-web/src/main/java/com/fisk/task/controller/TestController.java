package com.fisk.task.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.davis.client.ApiException;
import com.davis.client.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.task.TaskTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.dto.api.ReceiveDataDTO;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.TaskHierarchyDTO;
import com.fisk.task.dto.daconfig.DataAccessConfigDTO;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.task.NifiCustomWorkListDTO;
import com.fisk.task.entity.TBETLlogPO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.listener.atlas.BuildAtlasTableAndColumnTaskListener;
import com.fisk.task.listener.doris.BuildDataModelDorisTableListener;
import com.fisk.task.listener.nifi.IExecScriptListener;
import com.fisk.task.listener.nifi.INifiTaskListener;
import com.fisk.task.listener.nifi.INonRealTimeListener;
import com.fisk.task.listener.nifi.ITriggerScheduling;
import com.fisk.task.listener.nifi.impl.BuildNifiCustomWorkFlow;
import com.fisk.task.listener.olap.BuildModelTaskListener;
import com.fisk.task.listener.olap.BuildWideTableTaskListener;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.listener.postgre.datainput.BuildDataInputDeletePgTableListener;
import com.fisk.task.listener.postgre.datainput.BuildDataInputPgTableListener;
import com.fisk.task.mapper.TBETLLogMapper;
import com.fisk.task.pipeline2.MissionEndCenter;
import com.fisk.task.pipeline2.TaskPublish;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.nifi.INifiStage;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.service.nifi.ITableNifiSettingService;
import com.fisk.task.service.task.IBuildKfkTaskService;
import com.fisk.task.service.task.ITBETLIncremental;
import com.fisk.task.utils.KafkaTemplateHelper;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.StackTraceHelper;
import com.fisk.task.utils.TaskPgTableStructureHelper;
import com.fisk.task.utils.nifi.INiFiHelper;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/TestController")
@Slf4j
public class TestController {

    @Resource
    INiFiHelper iNifiComponentsBuild;
    @Resource
    PublishTaskController pc;
    @Resource
    BuildDataInputPgTableListener buildDataInputPgTableListener;
    @Resource
    BuildModelTaskListener buildModelTaskListener;
    @Resource
    INifiTaskListener iNifiTaskListener;
    @Resource
    BuildAtlasTableAndColumnTaskListener buildAtlasTableAndColumnTaskListener;
    @Resource
    BuildNifiCustomWorkFlow buildNifiCustomWorkFlow;
    @Resource
    BuildDataModelDorisTableListener buildDataModelDorisTableListener;
    @Resource
    INifiStage iNifiStage;
    @Resource
    BuildWideTableTaskListener buildWideTableTaskListener;
    @Resource
    INonRealTimeListener iNonRealTimeListener;
    @Resource
    INiFiHelper iNiFiHelper;
    @Resource
    IPipelineTaskPublishCenter iPipelineTaskPublishCenter;
    @Resource
    IPipelTaskLog iPipelTaskLog;
    @Resource
    BuildDataInputDeletePgTableListener buildDataInputDeletePgTableListener;
    @Resource
    ITBETLIncremental itbetlIncremental;
    @Resource
    TaskPgTableStructureHelper taskPgTableStructureHelper;
    @Resource
    ITriggerScheduling iTriggerScheduling;
    @Resource
    KafkaTemplateHelper kafkaTemplateHelper;
    @Resource
    TBETLLogMapper tbetlLogMapper;
    @Resource
    DataFactoryClient dataFactoryClient;
    @Resource
    IOlap iOlap;
    @Resource
    IPipelJobLog iPipelJobLog;
    @Resource
    IPipelLog iPipelLog;
    @Resource
    ITableNifiSettingService iTableNifiSettingService;
    @Resource
    IExecScriptListener iExecScriptListener;
    @Resource
    TaskPublish taskPublish;
    @Resource
    MissionEndCenter missionEndCenter;
    @Resource
    IBuildKfkTaskService iBuildKfkTaskService;


    @PostMapping("/nificzh")
    public void nificzh(){
        try {
            ProcessorEntity ftpProcessor = NifiHelper.getProcessorsApi().getProcessor("a7800d8f-0184-1000-aea9-0405a8562102");
            ProcessorEntity toCsvProcessor = NifiHelper.getProcessorsApi().getProcessor("a7800dc4-0184-1000-a088-8baca2b6392c");
            ProcessorEntity coProcessor = NifiHelper.getProcessorsApi().getProcessor("a7800f92-0184-1000-0069-a748797c23a7");
//            /
            //todo 小批次之前还少一个

            ProcessorEntity xpcProcessor = NifiHelper.getProcessorsApi().getProcessor("a780138f-0184-1000-7b57-0e4ad760662b");

        } catch (ApiException e) {
            e.printStackTrace();
        }


    }

    @PostMapping("/nificzh1")
    public void nificzh1(){
        try {
           ProcessorEntity coProcessor = NifiHelper.getProcessorsApi().getProcessor("5b600ee8-0184-1000-7917-b37b99e606cd");
//            /
            //todo 小批次之前还少一个

            System.out.println("ffff"+JSON.toJSONString(coProcessor));

        } catch (ApiException e) {
            e.printStackTrace();
        }


    }

    @PostMapping("/nifiTest1")
    public void nifiTest1(){
        try{
            ProcessorEntity processor = NifiHelper.getProcessorsApi().getProcessor("018517b8-87c8-1425-aecf-c04511b906e8");
            System.out.println("nifi" + JSON.toJSONString(processor));
        }catch (ApiException e){
            log.error("错误信息",e);
            e.printStackTrace();
        }
    }

    //

    @PostMapping("/nificzhm")
    public void nificzhm(){
        try {
            //a7800d8f-0184-1000-aea9-0405a8562102
            ProcessorEntity coProcessor = NifiHelper.getProcessorsApi().getProcessor("a7800d8f-0184-1000-aea9-0405a8562102");
            ControllerServiceEntity re = NifiHelper.getControllerServicesApi().getControllerService("01851111-87c8-1425-3231-0bf4961b1c81");
            ControllerServiceEntity wr = NifiHelper.getControllerServicesApi().getControllerService("01851184-87c8-1425-960a-17587e91f442");
            System.out.println(JSON.toJSONString(coProcessor));
            System.out.println(JSON.toJSONString(re));
            System.out.println(JSON.toJSONString(wr));
            //todo 小批次之前还少一个



        } catch (ApiException e) {
            e.printStackTrace();
        }


    }

    @PostMapping("/testnififlow3")
    public void testnififlow3(){
        String dd="{\"logId\":1639,\"pipelJobTraceId\":\"26a7d272-a5ba-451d-bbcb-0a6c512c32a0\",\"pipelTaskTraceId\":\"eb3d6eb8-4f7c-4e82-9bd1-1bcffc445026\",\"pipelTraceId\":\"88e83b8c-cfd3-413d-aef8-e72baff1ca13\",\"taskId\":\"171\"}";
        iExecScriptListener.execScript(dd,null);
    }

    @PostMapping("/testnififlow4")
    public void testnififlow4(){
        Map<Integer, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put(5,"运行成功 - 2022-11-30 16:25:57");
        iPipelJobLog.savePipelJobLog("ee2f8df6-a81c-4d94-90ec-5b244f38aff4",objectObjectHashMap,"19","864bf7d9-819f-4596-8303-bfadf491b6f1","147");
    }

    @PostMapping("/ff")
    public void ff(){
        DispatchExceptionHandlingDTO build = DispatchExceptionHandlingDTO.builder()
                .pipelTaskTraceId("d1a1c7f1-d784-445d-b357-54ab12958e68")
                .pipelStageTraceId("d99e1e69-d1cf-4d91-8fa4-9401dfb60f34")
                .pipelJobTraceId("8a09fcac-b330-464e-9e49-d2255048b5b2")
                .pipelTraceId("9a99737b-9e83-49b9-8f9d-bdf62fcd4b9e")
                .comment("java.io.IOException: java.io.IOException: Could not obtain next record from ResultSet")
                .build();
        iPipelJobLog.exceptionHandlingLog(build);
    }


    @Resource
    RedisUtil redisUtil;
    @PostMapping("/nifi")
    public void nifi() {
        ProcessGroupDTO groupDTO = new ProcessGroupDTO();
        ProcessGroupEntity entity = new ProcessGroupEntity();
        //group基础信息
        groupDTO.setName("asd");
        groupDTO.setComments("fgh");
        //groupDTO.setPosition();

        entity.setComponent(groupDTO);
        entity.setRevision(NifiHelper.buildRevisionDTO());
        try {
            ProcessGroupEntity res = NifiHelper.getProcessGroupsApi().createProcessGroup(NifiHelper.getPid(null), entity);
            System.out.println(JSON.toJSONString(res));
        } catch (ApiException e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
        }

    }
    @PostMapping("/kfk")
    public void kfk(){
        String dd="{\"appId\":4,\"appName\":\"demo1027\",\"dataClassifyEnum\":\"DATAMODELING\",\"excelFlow\":false,\"id\":5,\"logId\":264,\"openTransmission\":true,\"selectSql\":\"SELECT * from dbo.assignment\",\"synMode\":3,\"synchronousTypeEnum\":\"PGTOPG\",\"tableName\":\"dim_assignment\",\"type\":\"DIMENSION\",\"userId\":60}";
                iNifiTaskListener.msg(dd,null);
    }

    @PostMapping("/upd")
    public void upd(){
        try {
            taskPgTableStructureHelper.updatePgTableStructure("alter1 table ods_asdmdm_tb_072701 add fffls varchar(200);",null,3);
        }catch (Exception e){
            log.error("错误信息",e);
        }

    }

    @PostMapping("/redisTest")
    public void redisTest(){
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("123","cvb");
        hashMap.put("232","vxx");
        redisUtil.hmset("pipel:task:id:2613",hashMap,3000);
        Map<Object, Object> andDel = redisUtil.getAndDel("pipel:task:id:2613");

    }

    @PostMapping("/nifivar")
    public void nifivar() {
        HashMap<String, String> Map = new HashMap<>();
        Map.put("yy","yy");
        Map.put("yy1","yy1");
        iNifiComponentsBuild.buildNifiGlobalVariable(Map);
    }


    @PostMapping("/nifivar1")
    public void nifivar1() {
        itbetlIncremental.converSql("YuG.未命名","SELECT  \n A.PUID \n      ,A.PCMCLOSURE \n      ,A.PCMDISPOSITION \n      ,A.PCMMATURITY \n      ,A.PCMCLOSURECOMMENTS \n      ,A.PCMDISPOSITIONCOMMENTS \n      ,A.PCMDISPOSITIONDATE \n      ,A.PCMCLOSUREDATE \n      ,A.RA9_TECHNICALREVIEWBOARDU \n      ,A.RA9_TECHNICALREVIEWBOARDC \n      ,A.PA9_TARGETEFFECTIVITYDATE \n      ,A.RA9_SALESU \n      ,A.RA9_SALESC \n      ,A.RA9_SMEU \n      ,A.RA9_SMEC \n      ,A.PA9_REASONFORCHANGE \n      ,A.RA9_QUALITYENGINEERU \n      ,A.RA9_QUALITYENGINEERC \n      ,A.RA9_PROGRAMMANAGERU \n      ,A.RA9_PROGRAMMANAGERC \n      ,A.RA9_PROGRAMU \n      ,A.RA9_PROGRAMC \n      ,A.RA9_PROGPHASEGATESU \n      ,A.RA9_PROGPHASEGATESC \n      ,A.RA9_PROCESSPLANNERU \n      ,A.RA9_PROCESSPLANNERC \n      ,A.RA9_FUNCTIONALTEAMMEMBERU \n      ,A.RA9_FUNCTIONALTEAMMEMBERC \n      ,A.RA9_FUNCTIONALMANAGERU \n      ,A.RA9_FUNCTIONALMANAGERC \n      ,A.RA9_ESTIMATORU \n      ,A.RA9_ESTIMATORC \n      ,A.PA9_CUSTAPPROVALREQ \n      ,A.RA9_CROSSFUNCTIONALTEAMU \n      ,A.RA9_CROSSFUNCTIONALTEAMC \n      ,A.PA9_COSTRESPONSIBILITY \n      ,A.RA9_CHECKERU \n      ,A.RA9_CHECKERC \n      ,A.PA9_CHANGETYPE \n      ,A.PA9_CHANGESOURCE \n      ,A.RA9_BUYERU \n      ,A.RA9_BUYERC \n      ,A.PA9_BOMSTATUSDATE \n      ,A.RYF5_OTHERU \n      ,A.RYF5_OTHERC \n      ,A.PYF5_LEADPROGRAM \nfrom INFODBA.PCHANGEITEMREVISION A \njoin  INFODBA.ppom_application_object  B on A.PUID=B.PUID \nwhere (B.PCREATION_DATE >=to_date(@start_time,'yyyy-mm-dd hh24:mi:ss') and B.PCREATION_DATE<to_date(@end_time,'yyyy-mm-dd hh24:mi:ss')) \nor    (B.PLAST_MOD_DATE>=to_date(@start_time,'yyyy-mm-dd hh24:mi:ss')  and B.PLAST_MOD_DATE<to_date(@end_time,'yyyy-mm-dd hh24:mi:ss')) ","","[{\"deltaTimeParameterTypeEnum\":\"CONSTANT\",\"systemVariableTypeEnum\":\"START_TIME\",\"variableValue\":\"2022-10-21 10:06:40\"},{\"deltaTimeParameterTypeEnum\":\"THE_DEFAULT_EMPTY\",\"systemVariableTypeEnum\":\"END_TIME\",\"variableValue\":\"2022-10-28 10:06:44\"}]");
    }


    @PostMapping("/fdgd")
    public void sdfd(){
        String dd="{\"atomicIndicatorList\":[{\"businessAreaId\":154,\"factAttributeDTOList\":[{\"attributeType\":0,\"factFieldCnName\":\"id\",\"factFieldLength\":0,\"factFieldType\":\"INT\"},{\"attributeType\":0,\"factFieldCnName\":\"projectid\",\"factFieldLength\":50,\"factFieldType\":\"VARCHAR\"},{\"attributeType\":2,\"factFieldCnName\":\"workingtime\",\"factFieldLength\":0,\"factFieldType\":\"INT\"},{\"attributeType\":0,\"factFieldCnName\":\"position\",\"factFieldLength\":50,\"factFieldType\":\"VARCHAR\"},{\"attributeType\":0,\"factFieldCnName\":\"departmentid\",\"factFieldLength\":0,\"factFieldType\":\"INT\"},{\"attributeType\":0,\"factFieldCnName\":\"remark\",\"factFieldLength\":50,\"factFieldType\":\"VARCHAR\"},{\"attributeType\":0,\"factFieldCnName\":\"validty\",\"factFieldLength\":0,\"factFieldType\":\"INT\"},{\"attributeType\":0,\"factFieldCnName\":\"createtime\",\"factFieldLength\":50,\"factFieldType\":\"VARCHAR\"},{\"attributeType\":0,\"factFieldCnName\":\"createuserid\",\"factFieldLength\":50,\"factFieldType\":\"VARCHAR\"},{\"attributeType\":0,\"factFieldCnName\":\"createname\",\"factFieldLength\":50,\"factFieldType\":\"VARCHAR\"},{\"attributeType\":0,\"factFieldCnName\":\"modifytime\",\"factFieldLength\":50,\"factFieldType\":\"VARCHAR\"},{\"attributeType\":0,\"factFieldCnName\":\"modifyuserid\",\"factFieldLength\":50,\"factFieldType\":\"VARCHAR\"},{\"attributeType\":0,\"factFieldCnName\":\"modifyname\",\"factFieldLength\":50,\"factFieldType\":\"VARCHAR\"},{\"associateDimensionTable\":\"dim_Date\",\"attributeType\":1,\"factFieldLength\":0}],\"factId\":131,\"factTable\":\"fact_test0325\",\"list\":[{\"attributeType\":1,\"dimensionTableName\":\"dim_Date\",\"factFieldLength\":0,\"id\":316}]}],\"businessAreaId\":154,\"dimensionList\":[{\"appId\":154,\"dto\":[{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"id\",\"fieldId\":\"3584\",\"fieldLength\":0,\"fieldType\":\"INT\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"projectid\",\"fieldId\":\"3585\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"projectname\",\"fieldId\":\"3586\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"projecttypeid\",\"fieldId\":\"3587\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"projecttype\",\"fieldId\":\"3588\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"customerid\",\"fieldId\":\"3589\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"customername\",\"fieldId\":\"3590\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"pmid\",\"fieldId\":\"3591\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"pmname\",\"fieldId\":\"3592\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"pmdepartmentid\",\"fieldId\":\"3593\",\"fieldLength\":0,\"fieldType\":\"INT\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"contractnumber\",\"fieldId\":\"3594\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"signingtime\",\"fieldId\":\"3595\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"starttime\",\"fieldId\":\"3596\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"contractdays\",\"fieldId\":\"3597\",\"fieldLength\":0,\"fieldType\":\"INT\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"estimateendtime\",\"fieldId\":\"3598\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"projectstatusid\",\"fieldId\":\"3599\",\"fieldLength\":0,\"fieldType\":\"INT\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"projectstatus\",\"fieldId\":\"3600\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"estimatemanday\",\"fieldId\":\"3601\",\"fieldLength\":0,\"fieldType\":\"INT\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"actualendtime\",\"fieldId\":\"3602\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"projectprogress\",\"fieldId\":\"3604\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"projecthealth\",\"fieldId\":\"3605\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"validty\",\"fieldId\":\"3606\",\"fieldLength\":0,\"fieldType\":\"INT\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"createtime\",\"fieldId\":\"3607\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"createuserid\",\"fieldId\":\"3608\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"createname\",\"fieldId\":\"3609\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"modifytime\",\"fieldId\":\"3610\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"modifyuserid\",\"fieldId\":\"3611\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0},{\"associationSourceFieldId\":0,\"attributeType\":1,\"fieldEnName\":\"modifyname\",\"fieldId\":\"3612\",\"fieldLength\":50,\"fieldType\":\"VARCHAR\",\"sourceFieldId\":0}],\"id\":334,\"tableName\":\"dim_ProjectInfo\"}],\"logId\":9038}";
        buildModelTaskListener.msg(dd,null);
    }

    @PostMapping("/sql")
    public void sql(){
        //itbetlIncremental.converSql("aaa_ccc","select * from HumanResources.Employee where HireDate>@start_time", "sqlserver");
    }

    @PostMapping("/shanchubiao")
    public void shanchubiao(){
        String dd="{\"businessTypeEnum\":\"DATAMODEL\",\"delApp\":false,\"logId\":11280,\"tableList\":[{\"tableName\":\"dim_apersss\"}],\"userId\":60}";
        buildDataInputDeletePgTableListener.msg(dd,null);
    }

    @PostMapping("/fdgd11")
    public void sdfd11(){
        String dd="{\"appAbbreviation\":\"ftp27\",\"appId\":\"29\",\"dbId\":\"142\",\"deltaTimes\":[],\"driveType\":\"ftp\",\"excelFlow\":true,\"logId\":14713,\"modelPublishTableDTO\":{\"createType\":3,\"fieldList\":[{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"id\",\"fieldId\":1710,\"fieldLength\":500,\"fieldType\":\"NVARCHAR\",\"isPrimaryKey\":0},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"app_name\",\"fieldId\":1711,\"fieldLength\":500,\"fieldType\":\"NVARCHAR\",\"isPrimaryKey\":0},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"app_abbreviation\",\"fieldId\":1712,\"fieldLength\":500,\"fieldType\":\"NVARCHAR\",\"isPrimaryKey\":0},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"app_des\",\"fieldId\":1713,\"fieldLength\":500,\"fieldType\":\"NVARCHAR\",\"isPrimaryKey\":0},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"app_type\",\"fieldId\":1714,\"fieldLength\":500,\"fieldType\":\"NVARCHAR\",\"isPrimaryKey\":0},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"app_principal\",\"fieldId\":1715,\"fieldLength\":500,\"fieldType\":\"NVARCHAR\",\"isPrimaryKey\":0},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"app_principal_email\",\"fieldId\":1716,\"fieldLength\":500,\"fieldType\":\"NVARCHAR\",\"isPrimaryKey\":0},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"sync_mode\",\"fieldId\":1717,\"fieldLength\":500,\"fieldType\":\"NVARCHAR\",\"isPrimaryKey\":0},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"expression\",\"fieldId\":1718,\"fieldLength\":500,\"fieldType\":\"NVARCHAR\",\"isPrimaryKey\":0},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"msg\",\"fieldId\":1719,\"fieldLength\":500,\"fieldType\":\"NVARCHAR\",\"isPrimaryKey\":0},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"create_time\",\"fieldId\":1720,\"fieldLength\":500,\"fieldType\":\"NVARCHAR\",\"isPrimaryKey\":0},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"create_user\",\"fieldId\":1721,\"fieldLength\":500,\"fieldType\":\"NVARCHAR\",\"isPrimaryKey\":0},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"update_time\",\"fieldId\":1722,\"fieldLength\":500,\"fieldType\":\"NVARCHAR\",\"isPrimaryKey\":0},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"update_user\",\"fieldId\":1723,\"fieldLength\":500,\"fieldType\":\"NVARCHAR\",\"isPrimaryKey\":0},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"del_flag\",\"fieldId\":1724,\"fieldLength\":500,\"fieldType\":\"NVARCHAR\",\"isPrimaryKey\":0}],\"tableId\":142,\"tableName\":\"ods_ftp27_ftptest1107\"},\"openTransmission\":true,\"syncMode\":1,\"tableFieldsDTOS\":[{\"fieldLength\":500,\"fieldName\":\"id\",\"fieldType\":\"NVARCHAR\",\"funcType\":0,\"id\":1710,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":1,\"isTimestamp\":0,\"sourceFieldName\":\"id\",\"tableAccessId\":142},{\"fieldLength\":500,\"fieldName\":\"app_name\",\"fieldType\":\"NVARCHAR\",\"funcType\":0,\"id\":1711,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":1,\"isTimestamp\":0,\"sourceFieldName\":\"app_name\",\"tableAccessId\":142},{\"fieldLength\":500,\"fieldName\":\"app_abbreviation\",\"fieldType\":\"NVARCHAR\",\"funcType\":0,\"id\":1712,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":1,\"isTimestamp\":0,\"sourceFieldName\":\"app_abbreviation\",\"tableAccessId\":142},{\"fieldLength\":500,\"fieldName\":\"app_des\",\"fieldType\":\"NVARCHAR\",\"funcType\":0,\"id\":1713,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":1,\"isTimestamp\":0,\"sourceFieldName\":\"app_des\",\"tableAccessId\":142},{\"fieldLength\":500,\"fieldName\":\"app_type\",\"fieldType\":\"NVARCHAR\",\"funcType\":0,\"id\":1714,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":1,\"isTimestamp\":0,\"sourceFieldName\":\"app_type\",\"tableAccessId\":142},{\"fieldLength\":500,\"fieldName\":\"app_principal\",\"fieldType\":\"NVARCHAR\",\"funcType\":0,\"id\":1715,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":1,\"isTimestamp\":0,\"sourceFieldName\":\"app_principal\",\"tableAccessId\":142},{\"fieldLength\":500,\"fieldName\":\"app_principal_email\",\"fieldType\":\"NVARCHAR\",\"funcType\":0,\"id\":1716,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":1,\"isTimestamp\":0,\"sourceFieldName\":\"app_principal_email\",\"tableAccessId\":142},{\"fieldLength\":500,\"fieldName\":\"sync_mode\",\"fieldType\":\"NVARCHAR\",\"funcType\":0,\"id\":1717,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":1,\"isTimestamp\":0,\"sourceFieldName\":\"sync_mode\",\"tableAccessId\":142},{\"fieldLength\":500,\"fieldName\":\"expression\",\"fieldType\":\"NVARCHAR\",\"funcType\":0,\"id\":1718,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":1,\"isTimestamp\":0,\"sourceFieldName\":\"expression\",\"tableAccessId\":142},{\"fieldLength\":500,\"fieldName\":\"msg\",\"fieldType\":\"NVARCHAR\",\"funcType\":0,\"id\":1719,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":1,\"isTimestamp\":0,\"sourceFieldName\":\"msg\",\"tableAccessId\":142},{\"fieldLength\":500,\"fieldName\":\"create_time\",\"fieldType\":\"NVARCHAR\",\"funcType\":0,\"id\":1720,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":1,\"isTimestamp\":0,\"sourceFieldName\":\"create_time\",\"tableAccessId\":142},{\"fieldLength\":500,\"fieldName\":\"create_user\",\"fieldType\":\"NVARCHAR\",\"funcType\":0,\"id\":1721,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":1,\"isTimestamp\":0,\"sourceFieldName\":\"create_user\",\"tableAccessId\":142},{\"fieldLength\":500,\"fieldName\":\"update_time\",\"fieldType\":\"NVARCHAR\",\"funcType\":0,\"id\":1722,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":1,\"isTimestamp\":0,\"sourceFieldName\":\"update_time\",\"tableAccessId\":142},{\"fieldLength\":500,\"fieldName\":\"update_user\",\"fieldType\":\"NVARCHAR\",\"funcType\":0,\"id\":1723,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":1,\"isTimestamp\":0,\"sourceFieldName\":\"update_user\",\"tableAccessId\":142},{\"fieldLength\":500,\"fieldName\":\"del_flag\",\"fieldType\":\"NVARCHAR\",\"funcType\":0,\"id\":1724,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":1,\"isTimestamp\":0,\"sourceFieldName\":\"del_flag\",\"tableAccessId\":142}],\"tableName\":\"ftptest1107\",\"userId\":101,\"whetherSchema\":false}\n";
                buildAtlasTableAndColumnTaskListener.msg(dd,null);
    }

    @PostMapping("/fdgd1")
    public void sdfd1(){
        try {

            ProcessorEntity processor4 = NifiHelper.getProcessorsApi().getProcessor("9d4be9b5-0184-1000-2644-203a9d5a38db");
            log.info("rd3:"+JSON.toJSONString(processor4));
        } catch (ApiException e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
        }
    }

    @PostMapping("/testnififlow")
    public void testnififlow(){

        String ss="{\"appAbbreviation\":\"cdc_test\",\"appId\":\"981\",\"dbId\":\"4057\",\"driveType\":\"oracle_cdc\",\"excelFlow\":false,\"logId\":13787,\"modelPublishTableDTO\":{\"createType\":3,\"fieldList\":[{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"ID\",\"fieldId\":14368,\"fieldLength\":10,\"fieldType\":\"INT\",\"isPrimaryKey\":1},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"NAME\",\"fieldId\":14369,\"fieldLength\":255,\"fieldType\":\"VARCHAR\",\"isPrimaryKey\":0},{\"associateDimensionFieldId\":0,\"associateDimensionId\":0,\"attributeType\":0,\"fieldEnName\":\"DESCRIPTION\",\"fieldId\":14370,\"fieldLength\":512,\"fieldType\":\"VARCHAR\",\"isPrimaryKey\":0}],\"tableId\":4057,\"tableName\":\"ods_cdc_test_test_20221010\"},\"openTransmission\":true,\"syncMode\":0,\"tableFieldsDTOS\":[{\"fieldLength\":10,\"fieldName\":\"ID\",\"fieldType\":\"INT\",\"funcType\":0,\"id\":14368,\"isBusinesstime\":0,\"isPrimarykey\":1,\"isRealtime\":0,\"isTimestamp\":0,\"sourceFieldName\":\"ID\",\"sourceFieldType\":\"NUMBER\",\"tableAccessId\":4057},{\"fieldLength\":255,\"fieldName\":\"NAME\",\"fieldType\":\"VARCHAR\",\"funcType\":0,\"id\":14369,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":0,\"isTimestamp\":0,\"sourceFieldName\":\"NAME\",\"sourceFieldType\":\"VARCHAR2\",\"tableAccessId\":4057},{\"fieldLength\":512,\"fieldName\":\"DESCRIPTION\",\"fieldType\":\"VARCHAR\",\"funcType\":0,\"id\":14370,\"isBusinesstime\":0,\"isPrimarykey\":0,\"isRealtime\":0,\"isTimestamp\":0,\"sourceFieldName\":\"DESCRIPTION\",\"sourceFieldType\":\"VARCHAR2\",\"tableAccessId\":4057}],\"tableName\":\"test_20221010\",\"userId\":52,\"whetherSchema\":false}";
                buildDataInputPgTableListener.msg(ss,null);
    }


    @PostMapping("/testnififlow1")
    public void testnififlow1(){
        Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":46834c71-c9a7-48f6-b277-58bf3b60588f");
        Object o = hmget.get("168");

        TaskHierarchyDTO taskHierarchy = JSON.parseObject(o.toString(), TaskHierarchyDTO.class);
        log.info("gggggggggggggg"+JSON.toJSONString(taskHierarchy));
    }

    @PostMapping("/testnififlow2")
    public void testnififlow2(){
        String dd1="{\"externalStructure1\":\"{{a4f32e41-0d97-4fc0-8feb-dbc6808cee69=ERP系统}={}}\",\"logId\":1122,\"nifiCustomWorkDTOS\":[{\"NifiNode\":{\"appId\":55,\"groupId\":\"25\",\"tableId\":\"106\",\"tableType\":\"CUSTOMWORKPHYSICS\",\"type\":\"CUSTOMWORKDATAACCESS\",\"workflowDetailId\":29},\"nifiNode\":{\"$ref\":\"$.nifiCustomWorkDTOS[0].NifiNode\"}},{\"NifiNode\":{\"appId\":55,\"groupId\":\"25\",\"tableId\":\"103\",\"tableType\":\"CUSTOMWORKPHYSICS\",\"type\":\"CUSTOMWORKDATAACCESS\",\"workflowDetailId\":32},\"nifiNode\":{\"$ref\":\"$.nifiCustomWorkDTOS[1].NifiNode\"}},{\"NifiNode\":{\"appId\":55,\"groupId\":\"25\",\"tableId\":\"104\",\"tableType\":\"CUSTOMWORKPHYSICS\",\"type\":\"CUSTOMWORKDATAACCESS\",\"workflowDetailId\":33},\"nifiNode\":{\"$ref\":\"$.nifiCustomWorkDTOS[2].NifiNode\"}},{\"NifiNode\":{\"appId\":55,\"groupId\":\"25\",\"tableId\":\"105\",\"tableType\":\"CUSTOMWORKPHYSICS\",\"type\":\"CUSTOMWORKDATAACCESS\",\"workflowDetailId\":34},\"nifiNode\":{\"$ref\":\"$.nifiCustomWorkDTOS[3].NifiNode\"}},{\"NifiNode\":{\"appId\":55,\"groupId\":\"25\",\"tableId\":\"107\",\"tableType\":\"CUSTOMWORKPHYSICS\",\"type\":\"CUSTOMWORKDATAACCESS\",\"workflowDetailId\":35},\"nifiNode\":{\"$ref\":\"$.nifiCustomWorkDTOS[4].NifiNode\"}},{\"NifiNode\":{\"groupId\":\"a4f32e41-0d97-4fc0-8feb-dbc6808cee69\",\"nifiCustomWorkflowId\":31,\"nifiCustomWorkflowName\":\"触发器\",\"scheduleExpression\":\"0 30 9 * * ?\",\"scheduleType\":\"CRON\",\"type\":\"CUSTOMWORKSCHEDULINGCOMPONENT\",\"workflowDetailId\":31},\"nifiNode\":{\"$ref\":\"$.nifiCustomWorkDTOS[5].NifiNode\"},\"outputDucts\":[]}],\"nifiCustomWorkflowId\":\"a4f32e41-0d97-4fc0-8feb-dbc6808cee69\",\"pipelineId\":4,\"pipelineName\":\"ERP系统\",\"structure1\":\"{}\",\"userId\":60}";
                buildNifiCustomWorkFlow.msg(dd1,null);
    }



    @PostMapping("/diaodu")
    public void diaodu(){
        String dd1="{\"externalStructure1\":\"{{9d10a60a-9097-4c61-9ad3-d3021327f9fe=gg}={}}\",\"logId\":10962,\"nifiCustomWorkDTOS\":[{\"NifiNode\":{\"groupId\":\"9d10a60a-9097-4c61-9ad3-d3021327f9fe\",\"nifiCustomWorkflowId\":2317,\"nifiCustomWorkflowName\":\"开始\",\"scheduleExpression\":\"2000\",\"scheduleType\":\"TIMER\",\"type\":\"CUSTOMWORKSCHEDULINGCOMPONENT\",\"workflowDetailId\":2317},\"nifiNode\":{\"$ref\":\"$.nifiCustomWorkDTOS[0].NifiNode\"},\"outputDucts\":[{\"appId\":896,\"groupId\":\"2318\",\"tableId\":\"119\",\"tableType\":\"PHYSICS_API\",\"type\":\"DATAACCESS_API\",\"workflowDetailId\":2319}]},{\"NifiNode\":{\"appId\":896,\"groupId\":\"2318\",\"tableId\":\"119\",\"tableType\":\"PHYSICS_API\",\"type\":\"DATAACCESS_API\",\"workflowDetailId\":2319},\"nifiNode\":{\"$ref\":\"$.nifiCustomWorkDTOS[1].NifiNode\"}}],\"nifiCustomWorkflowId\":\"9d10a60a-9097-4c61-9ad3-d3021327f9fe\",\"pipelineId\":256,\"pipelineName\":\"gg\",\"structure1\":\"{}\",\"userId\":60}";
                buildNifiCustomWorkFlow.msg(dd1,null);
    }
    @PostMapping("/rizhi")
    public void rizhi(){
        String dd1="{\"counts\":\"\",\"endTime\":\"\",\"entryDate\":\"2022-12-08 19:37:39\",\"groupId\":\"cbe851d7-0184-1000-f325-23bb427d4685\",\"message\":\"java.io.IOException: java.io.IOException: Could not obtain next record from ResultSet\",\"pipelJobTraceId\":\"30e015f9-9327-47b8-a476-f7bb06be1ae4\",\"pipelStageTraceId\":\"37f50463-6005-435c-8444-626f8a58e7f0\",\"pipelTaskTraceId\":\"4de77e80-09a0-4771-b2d3-99313db7e6ed\",\"pipelTraceId\":\"ac654455-ca22-4afc-8a4c-3376989a5066\",\"startTime\":\"2022-12-08 19:37:38\",\"topic\":\"dmp.datafactory.nifi.21.3.76.187\"}";
                iNifiStage.saveNifiStage(dd1,null);
    }

    @PostMapping("/fb")
    public void fb(){
        String dd1="{\"ifTaskStart\":false,\"nifiCustomWorkflowDetailId\":315,\"pipelJobTraceId\":\"ca07f4d3-fc45-49e1-bc46-9a736fa4e824\",\"pipelTaskTraceId\":\"2c7bb66a-75ba-4ac3-be75-0ef9eac6d314\",\"pipelTraceId\":\"69976b56-5626-4969-a90d-dc23c2d68cdc\",\"tableType\":13,\"topic\":\"dmp.datafactory.nifi.21.13.0.315\",\"topicType\":3}";
        taskPublish.taskPublish(dd1,null);
    }
    @PostMapping("/missionEndCenter")
    public void missionEndCenter() {
        String message = "{\"fidata_batch_code\":\"d7113560-90ea-4310-a23a-3e8589630568\",\"ifTaskStart\":false,\"pipelJobTraceId\":\"1b92f0b0-e5e4-4cfb-8671-6466b1aff0db\",\"pipelStageTraceId\":\"e8ef3ce1-338f-4596-b66f-ea060be9cc85\",\"pipelTaskTraceId\":\"2b86e239-8d19-4ddf-a003-9e564ffa3c7a\",\"pipelTraceId\":\"d7113560-90ea-4310-a23a-3e8589630568\",\"start_time\":\"2022-12-13 16:35:37\",\"topic\":\"dmp.datafactory.nifi.21.13.0.312\",\"topicType\":3}";
                missionEndCenter.missionEndCenter(message, null);
    }

    @PostMapping("/nifiTest")
    public void nifiTest(){
        String dd1="";
        try {
            ControllerServiceEntity controllerService = NifiHelper.getControllerServicesApi().getControllerService("22a46ec6-0181-1000-d03a-1b0b922fbb72");
            ControllerServiceDTO component = controllerService.getComponent();
            Map<String, String> properties = component.getProperties();
            log.info("控制器服务"+JSON.toJSONString(properties));
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/zhongzhuan")
    public void zhongzhuan(){
         //String dd1="{\"ifTaskStart\":false,\"nifiCustomWorkflowDetailId\":67,\"numbers\":400,\"pipelApiDispatch\":\"{\\\"apiId\\\":1,\\\"appId\\\":2,\\\"pipelineId\\\":5,\\\"workflowId\\\":\\\"67\\\"}\",\"pipelJobTraceId\":\"20fd8586-94bd-4be8-9002-2f2692cda8c1\",\"pipelStageTraceId\":\"2e080708-63fe-4668-86c6-75a37bfd5555\",\"pipelTaskTraceId\":\"443cf87b-ddba-4087-8d33-b26450a04774\",\"pipelTraceId\":\"6ee11c35-bd22-45ee-8d4a-0ab4d3ba3c14\",\"tableId\":1,\"tableType\":10,\"topic\":\"dmp.datafactory.nifi.67.10.2.1\",\"topicType\":3}";
         //String dd1="{\"pipelApiDispatch\": \"[{\\\"apiId\\\":1,\\\"appId\\\":2,\\\"pipelineId\\\":5,\\\"workflowId\\\":\\\"67\\\"}]\", \"topic\": \"build.access.api.flow\", \"pipelTraceId\": \"\", \"topicType\": \"2\"}";
         String dd1="{\"topic\": \"dmp.datafactory.nifi.21\", \"pipelTraceId\": \"\", \"topicType\": \"2\"}";
                 iPipelineTaskPublishCenter.msg(dd1,null);
    }

    @PostMapping("/zhongzhuan1")
    public void zhongzhuan1(){
        //{"channelDataEnum":"DATALAKE_TASK","tableId":"191","workflowId":"21"},pipelTraceId:2ff4e72e-abb2-4ae3-bfa4-eed3ab298764
        String dd1="{\"channelDataEnum\":\"DATALAKE_TASK\",\"tableId\":\"191\",\"workflowId\":\"21\"}";
        iPipelineTaskPublishCenter.getNifiPortHierarchy(JSON.parseObject(dd1, NifiGetPortHierarchyDTO.class),"2ff4e72e-abb2-4ae3-bfa4-eed3ab298764");
    }

    @PostMapping("/kb")
    public void kb(){
        String dd1="{\n" +
                "    \"businessId\":158,\n" +
                "    \"entity\":[\n" +
                "        {\n" +
                "            \"columnConfig\":[\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":3635,\n" +
                "                    \"fieldLength\":0,\n" +
                "                    \"fieldName\":\"distributorid\",\n" +
                "                    \"fieldType\":\"FLOAT\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":3636,\n" +
                "                    \"fieldLength\":255,\n" +
                "                    \"fieldName\":\"distributor\",\n" +
                "                    \"fieldType\":\"VARCHAR\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":3637,\n" +
                "                    \"fieldLength\":255,\n" +
                "                    \"fieldName\":\"rsm\",\n" +
                "                    \"fieldType\":\"VARCHAR\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":3638,\n" +
                "                    \"fieldLength\":255,\n" +
                "                    \"fieldName\":\"manager\",\n" +
                "                    \"fieldType\":\"VARCHAR\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":3639,\n" +
                "                    \"fieldLength\":255,\n" +
                "                    \"fieldName\":\"region\",\n" +
                "                    \"fieldType\":\"VARCHAR\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":3640,\n" +
                "                    \"fieldLength\":255,\n" +
                "                    \"fieldName\":\"level\",\n" +
                "                    \"fieldType\":\"VARCHAR\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"tableId\":341,\n" +
                "            \"tableName\":\"dim_distributor\",\n" +
                "            \"tableType\":0\n" +
                "        },\n" +
                "        {\n" +
                "            \"columnConfig\":[\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":626,\n" +
                "                    \"fieldLength\":50,\n" +
                "                    \"fieldName\":\"date\",\n" +
                "                    \"fieldType\":\"VARCHAR\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":627,\n" +
                "                    \"fieldLength\":0,\n" +
                "                    \"fieldName\":\"lpid\",\n" +
                "                    \"fieldType\":\"FLOAT\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"fact_dissales_distributorid\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":628,\n" +
                "                    \"fieldLength\":0,\n" +
                "                    \"fieldName\":\"distributorid\",\n" +
                "                    \"fieldType\":\"FLOAT\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":629,\n" +
                "                    \"fieldLength\":0,\n" +
                "                    \"fieldName\":\"productid\",\n" +
                "                    \"fieldType\":\"FLOAT\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":630,\n" +
                "                    \"fieldLength\":0,\n" +
                "                    \"fieldName\":\"amt\",\n" +
                "                    \"fieldType\":\"FLOAT\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":631,\n" +
                "                    \"fieldLength\":0,\n" +
                "                    \"fieldName\":\"units\",\n" +
                "                    \"fieldType\":\"FLOAT\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":632,\n" +
                "                    \"fieldLength\":0,\n" +
                "                    \"fieldName\":\"salesid\",\n" +
                "                    \"fieldType\":\"FLOAT\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"tableId\":135,\n" +
                "            \"tableName\":\"fact_dissales\",\n" +
                "            \"tableType\":1\n" +
                "        },\n" +
                "        {\n" +
                "            \"columnConfig\":[\n" +
                "                {\n" +
                "                    \"alias\":\"dim_products_productid\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":3641,\n" +
                "                    \"fieldLength\":0,\n" +
                "                    \"fieldName\":\"productid\",\n" +
                "                    \"fieldType\":\"FLOAT\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":3642,\n" +
                "                    \"fieldLength\":255,\n" +
                "                    \"fieldName\":\"product_code\",\n" +
                "                    \"fieldType\":\"VARCHAR\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":3643,\n" +
                "                    \"fieldLength\":255,\n" +
                "                    \"fieldName\":\"product_name\",\n" +
                "                    \"fieldType\":\"VARCHAR\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":3644,\n" +
                "                    \"fieldLength\":255,\n" +
                "                    \"fieldName\":\"level1\",\n" +
                "                    \"fieldType\":\"VARCHAR\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":3645,\n" +
                "                    \"fieldLength\":255,\n" +
                "                    \"fieldName\":\"level2\",\n" +
                "                    \"fieldType\":\"VARCHAR\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":3646,\n" +
                "                    \"fieldLength\":255,\n" +
                "                    \"fieldName\":\"level3\",\n" +
                "                    \"fieldType\":\"VARCHAR\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":3647,\n" +
                "                    \"fieldLength\":255,\n" +
                "                    \"fieldName\":\"level4\",\n" +
                "                    \"fieldType\":\"VARCHAR\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":3648,\n" +
                "                    \"fieldLength\":255,\n" +
                "                    \"fieldName\":\"level5\",\n" +
                "                    \"fieldType\":\"VARCHAR\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"alias\":\"\",\n" +
                "                    \"dataType\":\"\",\n" +
                "                    \"fieldId\":3649,\n" +
                "                    \"fieldLength\":255,\n" +
                "                    \"fieldName\":\"isnew\",\n" +
                "                    \"fieldType\":\"VARCHAR\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"tableId\":342,\n" +
                "            \"tableName\":\"dim_products\",\n" +
                "            \"tableType\":0\n" +
                "        }\n" +
                "    ],\n" +
                "    \"id\":7,\n" +
                "    \"name\":\"dws_dissalesdetail\",\n" +
                "    \"relations\":[\n" +
                "        {\n" +
                "            \"joinType\":\"left join\",\n" +
                "            \"sourceColumn\":\"productid\",\n" +
                "            \"sourceTable\":\"fact_dissales\",\n" +
                "            \"targetColumn\":\"productid\",\n" +
                "            \"targetTable\":\"dim_products\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"joinType\":\"left join\",\n" +
                "            \"sourceColumn\":\"distributorid\",\n" +
                "            \"sourceTable\":\"fact_dissales\",\n" +
                "            \"targetColumn\":\"distributorid\",\n" +
                "            \"targetTable\":\"dim_distributor\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"sqlScript\":\"select external_dim_distributor.distributorid,external_dim_distributor.distributor,external_dim_distributor.rsm,external_dim_distributor.manager,external_dim_distributor.region,external_dim_distributor.level,external_fact_dissales.date,external_fact_dissales.lpid,external_fact_dissales.distributorid as fact_dissales_distributorid,external_fact_dissales.productid,external_fact_dissales.amt,external_fact_dissales.units,external_fact_dissales.salesid,external_dim_products.productid as dim_products_productid,external_dim_products.product_code,external_dim_products.product_name,external_dim_products.level1,external_dim_products.level2,external_dim_products.level3,external_dim_products.level4,external_dim_products.level5,external_dim_products.isnew from external_fact_dissales left join external_dim_products on external_fact_dissales.productid = external_dim_products.productid left join external_dim_distributor on external_fact_dissales.distributorid = external_dim_distributor.distributorid \",\n" +
                "    \"userId\":60\n" +
                "}";
        buildWideTableTaskListener.msg(dd1,null);
    }

    @PostMapping("/api")
    public void aaa(){
        String dd1="{\"workflowIdAppIdApiId\": \"2204_896_121\"}";
        iNonRealTimeListener.importData(dd1,null);
    }

    @PostMapping("/tydd")
    public void tydd(){
        String dd1="{\"dataClassifyEnum\":\"UNIFIEDCONTROL\",\"deleted\":false,\"id\":4,\"scheduleExpression\":\"0 0 20 * * ?\",\"scheduleType\":\"CRON\",\"topic\":\"task.build.governance.template.flow\",\"type\":\"GOVERNANCE\",\"userId\":60}";
                iTriggerScheduling.unifiedControl(dd1,null);
    }

    //jobTraceId:null,pipelTaskTraceId:null,map:{8:"2022-06-16 13:30:23",9:"运行成功"},taskId:null
    @PostMapping("/pipelTraceid")
    public void pipelTraceid(){
        HashMap<Integer, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put(8,"2022-06-16 13:30:23");
        objectObjectHashMap.put(9,"运行成功");
        iPipelTaskLog.savePipelTaskLog(null,null,null,objectObjectHashMap,null,null,0);
    }


    @PostMapping("/dfdf")
    public void dfdf(){
        kafkaTemplateHelper.sendMessageAsync("task.acceptance.data","{\"data\": [\n" +
                " {\n" +
                " \"id\": \"\",\n" +
                " \"sensor_id\": \"\",\n" +
                " \"warning_id\": \"\",\n" +
                " \"streetname\": \"\",\n" +
                " \"communityname\": \"西三居委\",\n" +
                " \"communitycode\": \"\",\n" +
                " \"communitytype\": \"\",\n" +
                " \"mattertype\": \"\",\n" +
                " \"matter\": \"\",\n" +
                " \"taskaddress\": \"小区xxxx大门\",\n" +
                " \"taskcontent\": \"\",\n" +
                " \"taskmediaaddress\": \"\",\n" +
                " \"taskmediatype\": \"\",\n" +
                " \"tasklongitude\": \"\",\n" +
                " \"tasklatitude\": \"\",\n" +
                " \"tudetype\": \"\",\n" +
                " \"tasktime\": \"2022-09-09\",\n" +
                " \"handleuser\": \"\",\n" +
                " \"handleuserposition\": \"\",\n" +
                " \"handletime\": \"\",\n" +
                " \"disposer\": \"\",\n" +
                " \"disposerposition\": \"\",\n" +
                " \"disposertime\": \"\",\n" +
                " \"disposerresult\": \"\",\n" +
                " \"disposermediatype\": \"\",\n" +
                " \"disposermediaaddress\": \"\",\n" +
                " \"firsttaskuser\": \"\",\n" +
                " \"firsttaskuserposition\": \"\",\n" +
                " \"firsttaskusertime\": \"\",\n" +
                " \"firsttaskuserresult\": \"\",\n" +
                " \"coprocessinguser\": \"\",\n" +
                " \"coprocessingusertime\": \"\",\n" +
                " \"coprocessinguserresult\": \"\",\n" +
                " \"reportstreet\": \"\",\n" +
                " \"reportstreettime\": \"\",\n" +
                " \"reportstreetresult\": \"\",\n" +
                " \"apipulltime\": \"\",\n" +
                " \"dt_createdtime\": \"\",\n" +
                " \"lb_createduser\": \"\",\n" +
                " \"dt_updatedtime\": \"\",\n" +
                " \"lb_updateduser\": \"\",\n" +
                " \"disposerdepartment\": \"\",\n" +
                " \"researchtime\": \"\",\n" +
                " \"researchtype\": \"\",\n" +
                " \"researchcontent\": \"\",\n" +
                " \"sourcetype\": \"\",\n" +
                " \"status\": \"\",\n" +
                " \"researchimgurl\": \"\",\n" +
                " \"problemdescription\": \"\",\n" +
                " \"taskvideo\": \"\",\n" +
                " \"timelimit\": \"\",\n" +
                " \"tasktype\": \"\",\n" +
                " \"alarmupdatestring\": \"\",\n" +
                " \"warn_level\": \"\",\n" +
                " \"monitor_object\": \"\",\n" +
                " \"monitor_signs\": \"道路积水预警\",\n" +
                " \"warning_index\": \"\",\n" +
                " \"level\": \"\",\n" +
                " \"level_desc\": \"蓝色\" }\n" +
                " ]\n" +
                "}");}

    @PostMapping("/hhh")
    public void hhh(){
        try {
            ProcessGroupEntity root = NifiHelper.getProcessGroupsApi().getProcessGroup("root");
            log.info("ffffffffffff"+JSON.toJSONString(root));
            ScheduleComponentsEntity scheduleComponentsEntity = new ScheduleComponentsEntity();
            scheduleComponentsEntity.setId(root.getId());
            scheduleComponentsEntity.setDisconnectedNodeAcknowledged(false);
            scheduleComponentsEntity.setState(ScheduleComponentsEntity.StateEnum.STOPPED);
            NifiHelper.getFlowApi().scheduleComponents(root.getId(), scheduleComponentsEntity);
        } catch (ApiException e) {
            e.printStackTrace();
        }


    }

    @PostMapping("/hhhh")
    public void hhhh(){

        TBETLlogPO tbetLlogPO = tbetlLogMapper.selectById(64490);
        String dd = tbetLlogPO.querySql;
        log.info("sql语句"+tbetLlogPO.querySql);
        log.info("比较结果" + dd.contains("\""));
        String s = dd.replaceAll("\"", "\\\\\"");
        log.info("sql语句"+s);
        log.info("比较结果" + s.contains("\""));

    }

    @PostMapping("/getSqlForPgOds")
    public void getSqlForPgOds() {
        String ss="";
        DataAccessConfigDTO dataAccessConfigDTO = JSON.parseObject(ss, DataAccessConfigDTO.class);
        List<String> sqlForPgOds = iNiFiHelper.getSqlForPgOds(dataAccessConfigDTO);
        System.out.println(sqlForPgOds.get(0));
        System.out.println(sqlForPgOds.get(1));

    }

    @PostMapping("/kerborsrzh")
    public void kerborsrzh() {
        try {

            ControllerServiceEntity controllerService = NifiHelper.getControllerServicesApi().getControllerService("1f1164ef-1190-102b-a701-509f38623ba4");
            ProcessorEntity processor = NifiHelper.getProcessorsApi().getProcessor("01821769-06ca-1efc-b28c-1b5db1c334e2");
            ProcessorEntity processor1 = NifiHelper.getProcessorsApi().getProcessor("1f1164f2-1190-102b-f454-af68eeba348d");
            ProcessorEntity processor2 = NifiHelper.getProcessorsApi().getProcessor("01821a04-06ca-1efc-b3d4-17f8343b9eb2");
            log.info("控制器服务配置:{}",JSON.toJSONString(controllerService));
            log.info("组件配置:{}",JSON.toJSONString(processor));
            log.info("组件配置:{}",JSON.toJSONString(processor1));
            log.info("组件配置:{}",JSON.toJSONString(processor2));
        } catch (ApiException e) {
            e.printStackTrace();
        }

    }

    @PostMapping("/ggg")
    public void ggg() {
        String dd = "select \n" +
                "null as id,\n" +
                "null as deviceid,\n" +
                "CASE WHEN swz = '1' THEN '水位' \n" +
                "WHEN cwz = '1' THEN '潮位'\n" +
                "WHEN ylz = '1' THEN '雨量' \n" +
                "ELSE '1'\n" +
                "END AS indexname,\n" +
                "dataname as  stationcode,\n" +
                "dataid as stationame,\n" +
                "t_swy_shuiwen as reporttime,\n" +
                "null as unit,\n" +
                "null as lowvalue,\n" +
                "null as upvalue,\n" +
                "water as currentvalue,\n" +
                "null as historymax,\n" +
                "null as historymin,\n" +
                "null as \"level\",\n" +
                "null as discovertype,\n" +
                "'城建坐标' as coordtype,\n" +
                "null as longitude,\n" +
                "null as latitude,\n" +
                "x as coordx,\n" +
                "y as coordy,\n" +
                "streetname,\n" +
                "null as communityname,\n" +
                "null as remark,\n" +
                "'' as validity,\n" +
                "'fxft_user' as sourcetype\n" +
                "FROM CITYGRID.V_SWY_DATA\n" +
                "WHERE (swz = '1' OR cwz = '1' OR ylz = '1') AND  T_SWY_SHUIWEN >trunc(sysdate)";


    }

    public static void main(String[] args) {
        ReceiveDataDTO receiveDataDTO = new ReceiveDataDTO();
        receiveDataDTO.apiCode = 194L;
        receiveDataDTO.executeConfigFlag = false;
        receiveDataDTO.flag = false;
        receiveDataDTO.pushData = "{\"data\": [\n" +
                " {\n" +
                " \"id\": \"\",\n" +
                " \"sensor_id\": \"\",\n" +
                " \"warning_id\": \"\",\n" +
                " \"streetname\": \"\",\n" +
                " \"communityname\": \"西三居委\",\n" +
                " \"communitycode\": \"\",\n" +
                " \"communitytype\": \"\",\n" +
                " \"mattertype\": \"\",\n" +
                " \"matter\": \"\",\n" +
                " \"taskaddress\": \"小区xxxx大门\",\n" +
                " \"taskcontent\": \"\",\n" +
                " \"taskmediaaddress\": \"\",\n" +
                " \"taskmediatype\": \"\",\n" +
                " \"tasklongitude\": \"\",\n" +
                " \"tasklatitude\": \"\",\n" +
                " \"tudetype\": \"\",\n" +
                " \"tasktime\": \"2022-09-09\",\n" +
                " \"handleuser\": \"\",\n" +
                " \"handleuserposition\": \"\",\n" +
                " \"handletime\": \"\",\n" +
                " \"disposer\": \"\",\n" +
                " \"disposerposition\": \"\",\n" +
                " \"disposertime\": \"\",\n" +
                " \"disposerresult\": \"\",\n" +
                " \"disposermediatype\": \"\",\n" +
                " \"disposermediaaddress\": \"\",\n" +
                " \"firsttaskuser\": \"\",\n" +
                " \"firsttaskuserposition\": \"\",\n" +
                " \"firsttaskusertime\": \"\",\n" +
                " \"firsttaskuserresult\": \"\",\n" +
                " \"coprocessinguser\": \"\",\n" +
                " \"coprocessingusertime\": \"\",\n" +
                " \"coprocessinguserresult\": \"\",\n" +
                " \"reportstreet\": \"\",\n" +
                " \"reportstreettime\": \"\",\n" +
                " \"reportstreetresult\": \"\",\n" +
                " \"apipulltime\": \"\",\n" +
                " \"dt_createdtime\": \"\",\n" +
                " \"lb_createduser\": \"\",\n" +
                " \"dt_updatedtime\": \"\",\n" +
                " \"lb_updateduser\": \"\",\n" +
                " \"disposerdepartment\": \"\",\n" +
                " \"researchtime\": \"\",\n" +
                " \"researchtype\": \"\",\n" +
                " \"researchcontent\": \"\",\n" +
                " \"sourcetype\": \"\",\n" +
                " \"status\": \"\",\n" +
                " \"researchimgurl\": \"\",\n" +
                " \"problemdescription\": \"\",\n" +
                " \"taskvideo\": \"\",\n" +
                " \"timelimit\": \"\",\n" +
                " \"tasktype\": \"\",\n" +
                " \"alarmupdatestring\": \"\",\n" +
                " \"warn_level\": \"\",\n" +
                " \"monitor_object\": \"\",\n" +
                " \"monitor_signs\": \"道路积水预警\",\n" +
                " \"warning_index\": \"\",\n" +
                " \"level\": \"\",\n" +
                " \"level_desc\": \"蓝色\" }\n" +
                " ]\n" +
                "}\n";
        System.out.print(JSON.toJSONString(receiveDataDTO));
        String dd = receiveDataDTO.pushData;
        JSONObject jsonObject = JSON.parseObject(dd);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode node = objectMapper.readTree(dd);
            Iterator<String> stringIterator = node.fieldNames();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
 /*   public void processJson(String jsonStr) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode node = objectMapper.readTree(jsonStr);
            first = true;
            processNode(node);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processNode(JsonNode n) {
        if (n.isContainerNode()) {
            if (n.isArray()){
                Iterator<JsonNode> itt = n.iterator();
                while (itt.hasNext()) {
                    JsonNode innerNode = itt.next();
                    processNode(innerNode);
                }
            }
            else {
                Iterator<Map.Entry<String,JsonNode>> fieldsIterator = n.fields();
                Map.Entry<String,JsonNode> field;
                while (fieldsIterator.hasNext()){
                    field = fieldsIterator.next();
                    this.lastKey = field.getKey();
                    location += "/" + this.lastKey;
                    processNode(field.getValue());
                }
            }
        }
        else if (n.isNull()) {
            propertyCount++;
            System.out.println("Key: " + this.lastKey + " Value: " + n);
        } else {
            propertyCount++;
            location = location.substring(0,location.lastIndexOf("/"));

            System.out.println("Key: " + this.lastKey  + " Value: " + n.asText());
        }
    }*/

    public static void parseJsonToBeanInfo(String JsonInfo) throws Exception {
        if(!"".equals(JsonInfo)&&JsonInfo!=null){
//先把String 形式的 JSON 转换位 JSON 对象

            JSONObject json = new JSONObject(Boolean.parseBoolean(JsonInfo));

//得到 JSON 属性对象列表

            JSONArray jsonArray =json.getJSONArray("data");

//遍历，得到属性值

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jo = jsonArray.getJSONObject(i);

                String id = jo.getString("id");

                String name = jo.getString("name");

                String parentId = jo.getString("parentId");

                String alterationType = jo.getString("alterationType");

                System.out.print("id is:"+id);

                System.out.print(" name is:"+name);

                System.out.print(" parentId is:"+parentId);

                System.out.println(" alterationType is:"+alterationType);

            }

        }

    }



    @PostMapping("/rds")
    public void rds() {
        Map<Object, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("11","1");
        objectObjectHashMap.put("12","1");
        objectObjectHashMap.put("13","1");
        objectObjectHashMap.put("14","1");
        redisUtil.hmsset("cfk",objectObjectHashMap,3000);
        //Map<Object, Object> cfk = redisUtil.hmget("cfk");
        //cfk.put(11,2);
        Map<Object, Object> objectObjectHashMap1 = new HashMap<>();
        objectObjectHashMap1.put("11","2");
        redisUtil.hmsetForDispatch("cfk",objectObjectHashMap1,3000);
        Map<Object, Object> cfk1 = redisUtil.hmget("cfk");
        System.out.println(cfk1);


    }

//    /**
//     * 创建管道1
//     *
//     * @return
//     */
//    @PostMapping("/NifiCustomWorkFlow1")
//    @MQConsumerLog
//    public void publishBuildNifiCustomWorkFlowTask(String data) {
//        log.info(data);
//        taskPublish.taskPublish(data, null);
//    }


//    /**
//     * 创建管道2
//     *
//     * @param nifiCustomWorkListDTO
//     * @return
//     */
//    @PostMapping("/NifiCustomWorkFlow2")
//    @ApiOperation(value = "创建管道")
//    public ResultEntity<Object> publishBuildNifiCustomWorkFlowTask(@RequestBody NifiCustomWorkListDTO nifiCustomWorkListDTO) {
//        return iBuildKfkTaskService.publishTask(TaskTypeEnum.BUILD_CUSTOMWORK_TASK.getName(),
//                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
//                MqConstants.QueueConstants.DataServiceTopicConstants.BUILD_CUSTOMWORK_FLOW,
//                nifiCustomWorkListDTO);
//    }
//
////    /**
////     * task.build.customwork.flow
////     *
////     * @param dataInfo
////     * @return
////     */
////    @KafkaListener(topics = MqConstants.QueueConstants.DataServiceTopicConstants.BUILD_CUSTOMWORK_FLOW, containerFactory = "batchFactory",
////            groupId = MqConstants.TopicGroupId.TASK_GROUP_ID)
////    @MQConsumerLog
//    @PutMapping("/buildNifiCustomWorkFlow")
//    public ResultEntity<Object> buildNifiCustomWorkFlow(String dataInfo) {
//        return ResultEntityBuild.build(buildNifiCustomWorkFlow.msg(dataInfo, null));
//    }

}
