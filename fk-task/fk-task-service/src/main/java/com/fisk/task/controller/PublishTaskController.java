package com.fisk.task.controller;

import com.fisk.common.constants.MqConstants;
import com.fisk.common.enums.task.TaskTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.dto.modelpublish.ModelPublishDataDTO;
import com.fisk.task.dto.atlas.AtlasEntityDeleteDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import com.fisk.task.dto.doris.TableInfoDTO;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.dto.task.NifiCustomWorkListDTO;
import com.fisk.task.service.IBuildTaskService;
import com.google.gson.Gson;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author gy
 */
@RestController
@RequestMapping("/publishTask")
@Slf4j
public class PublishTaskController {

    @Resource
    IBuildTaskService service;

    @PostMapping("/nifiFlow")
    @ApiOperation(value = "创建同步数据nifi流程")
    public ResultEntity<Object> publishBuildNifiFlowTask(@RequestBody BuildNifiFlowDTO data) {
        return service.publishTask("创建表:"+data.tableName+"的数据流任务",
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_NIFI_FLOW,
                data);
    }

    /**
     * 在Doris中生成stg&ods数据表
     *
     * @param data
     * @return
     */
    @PostMapping("/dorisBuild")
    @ApiOperation(value = "在Doris中生成stg&ods数据表")
    public ResultEntity<Object> publishBuildDorisTask(@RequestBody TableInfoDTO data) {
        return service.publishTask(TaskTypeEnum.BUILD_DORIS_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_DORIS_FLOW,
                data);
    }

    /**
     * 在Atlas中生成实例与数据库的血缘关系
     *
     * @param ArDto
     * @return
     */
    @PostMapping("/atlasBuildInstance")
    @ApiOperation(value = "在Atlas中生成实例与数据库的血缘关系")
    public ResultEntity<Object> publishBuildAtlasInstanceTask(@RequestBody AtlasEntityQueryDTO ArDto) {
        return service.publishTask(TaskTypeEnum.BUILD_ATLAS_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_ATLAS_INSTANCE_FLOW,
                ArDto);
    }

    /**
     * 在Atlas中生成数据库、表、字段的血缘关系
     * @param ArDto
     * @return
     */
    @PostMapping("/atlasBuildTableAndColumn")
    @ApiOperation(value = "在Atlas中生成数据库、表、字段的血缘关系")
    public ResultEntity<Object> publishBuildAtlasTableTask(@RequestBody BuildPhysicalTableDTO ArDto) {
        log.info("进入方法");
         service.publishTask(TaskTypeEnum.BUILD_ATLAS_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_ATLAS_TABLECOLUMN_FLOW,
                ArDto);
         //pgsql
                return service.publishTask("数据湖表:"+ArDto.tableName+",结构处理成功",
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_DATAINPUT_PGSQL_TABLE_FLOW,
                ArDto);
        //Doris
//        return service.publishTask(TaskTypeEnum.BUILD_DORIS_TASK.getName(),
//                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
//                MqConstants.QueueConstants.BUILD_DORIS_FLOW,
//                ArDto);


    }

    /**
     *pgsql stg to ods
     * @param entityId
     * @return
     */
    @PostMapping("/pgsqlStgOdsIncrementalUpdate")
    @ApiOperation(value = "数据接入 STG TO ODS")
    public ResultEntity<Object> publishBuildPGSqlStgToOdsTask(@RequestBody AtlasEntityDeleteDTO entityId) {
        return service.publishTask(TaskTypeEnum.BUILD_DATAINPUT_PGSQL_STGTOODS_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_DATAINPUT_PGSQL_STGTOODS_FLOW,
                entityId);
    };

    /**
     * Doris 增量更新
     * @param entityId
     * @return
     */
    @PostMapping("/dorisIncrementalUpdate")
    @ApiOperation(value = "Doris 增量更新")
    public ResultEntity<Object> publishBuildDorisIncrementalUpdateTask(@RequestBody AtlasEntityDeleteDTO entityId) {
        return service.publishTask(TaskTypeEnum.BUILD_DORIS_INCREMENTAL_UPDATE_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_DORIS_INCREMENTAL_FLOW,
                entityId);
    }

    /**
     * Atlas 删除实体
     * @param entityId
     * @return
     */
    @PostMapping("/atlasEntityDelete")
    @ApiOperation(value = "Atlas 删除实体")
    public ResultEntity<Object> publishBuildAtlasEntityDeleteTask(@RequestBody AtlasEntityDeleteDTO entityId) {
        return service.publishTask(TaskTypeEnum.BUILD_ATLAS_ENTITYDELETE_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_ATLAS_ENTITYDELETE_FLOW,
                entityId);
    }
    /**
     * pgsql 删除表
     * @param delTable
     * @return
     */
    @PostMapping("/deletePgsqlTable")
    @ApiOperation(value = "pgsql 删除表")
    public ResultEntity<Object> publishBuildDeletePgsqlTableTask(@RequestBody PgsqlDelTableDTO delTable) {
        return service.publishTask(TaskTypeEnum.BUILD_DATAINPUT_DELETE_PGSQL_STGTOODS_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_DATAINPUT_DELETE_PGSQL_TABLE_FLOW,
                delTable);
    }

    /**
     * doris创建表BUILD_DORIS_TABLE
     * @param modelPublishDataDTO
     * @return
     */
    @PostMapping("/atlasDorisTable")
    @ApiOperation(value = "dmp_dw创建表")
    public ResultEntity<Object> publishBuildAtlasDorisTableTask(@RequestBody ModelPublishDataDTO modelPublishDataDTO){
        return service.publishTask(TaskTypeEnum.BUILD_DATAMODEL_DORIS_TABLE.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_DATAMODEL_DORIS_TABLE,
                modelPublishDataDTO);
    }

    /**
     * 创建管道
     * @param nifiCustomWorkListDTO
     * @return
     */
    @PostMapping("/NifiCustomWorkFlow")
    @ApiOperation(value = "创建管道")
    public ResultEntity<Object> publishBuildNifiCustomWorkFlowTask(@RequestBody NifiCustomWorkListDTO nifiCustomWorkListDTO){
        return service.publishTask(TaskTypeEnum.BUILD_CUSTOMWORK_TASK.getName(),
                MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME,
                MqConstants.QueueConstants.BUILD_CUSTOMWORK_FLOW,
                nifiCustomWorkListDTO);
    }

    public static void main(String[] args) {
        String s1="{643=job1}\t{645=job11}\n";
        String s2="{dde685ce-9b4b-421d-95aa-08ce2f488c97=cmd}\t{643=job1, 644=job2}\n";
         Gson gson = new Gson();
         //---------------------------------------
        //Map转String
        //String str1 = getMapToString();
        //System.out.println(str1);
        try {
            //String转map
            Map<String, Object> map1 = getStringToMap(s1);
            System.out.println(map1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     *
     * Map转String
     * @param map
     * @return
     */
    public static String getMapToString(Map<String,Object> map){
        Set<String> keySet = map.keySet();
        //将set集合转换为数组
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        //给数组排序(升序)
        Arrays.sort(keyArray);
        //因为String拼接效率会很低的，所以转用StringBuilder
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyArray.length; i++) {
            // 参数值为空，则不参与签名 这个方法trim()是去空格
            if ((String.valueOf(map.get(keyArray[i]))).trim().length() > 0) {
                sb.append(keyArray[i]).append(":").append(String.valueOf(map.get(keyArray[i])).trim());
            }
            if(i != keyArray.length-1){
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     *
     * String转map
     * @param str
     * @return
     */
    public static Map<String,Object> getStringToMap(String str){
        //根据逗号截取字符串数组
        String[] str1 = str.split(",");
        //创建Map对象
        Map<String,Object> map = new HashMap<>();
        //循环加入map集合
        for (int i = 0; i < str1.length; i++) {
            //根据":"截取字符串数组
            String[] str2 = str1[i].split(":");
            //str2[0]为KEY,str2[1]为值
            //map.put(str2[0],str2[1]);

            if (str2.length == 2){
                map.put(str2[0].trim(),str2[1]);
            }else{
                map.put(str2[0].trim(),"");
            }
        }
        return map;
    }

}
