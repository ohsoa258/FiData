package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.dto.tableapi.*;
import com.fisk.dataservice.dto.tableservice.TableServiceEmailDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePublishStatusDTO;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.AppServiceTypeEnum;
import com.fisk.dataservice.enums.AuthenticationTypeEnum;
import com.fisk.dataservice.enums.InterfaceTypeEnum;
import com.fisk.dataservice.handler.restapi.RestApiHandler;
import com.fisk.dataservice.handler.restapi.factory.InterfaceRestApiFactory;
import com.fisk.dataservice.map.TableApiParameterMap;
import com.fisk.dataservice.map.TableApiServiceMap;
import com.fisk.dataservice.mapper.TableApiServiceMapper;
import com.fisk.dataservice.service.*;
import com.fisk.dataservice.util.TreeBuilder;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.dispatchlog.PipelTaskLogVO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.BuildDeleteTableServiceDTO;
import com.fisk.task.dto.task.BuildTableApiServiceDTO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("tableApiService")
public class TableApiServiceImpl extends ServiceImpl<TableApiServiceMapper, TableApiServicePO> implements ITableApiService {

    @Resource
    ITableAppManageService tableAppService;
    @Resource
    private ITableApiAuthRequestService tableApiAuthRequestService;

    @Resource
    private ITableApiParameterService tableApiParameterService;
    @Resource
    private TableSyncModeImpl tableSyncMode;
    @Resource
    private PublishTaskClient publishTaskClient;
    @Resource
    private UserHelper userHelper;
    @Resource
    private ITableService tableService;

    @Value("${dataservice.scan.api_address}")
    private String dataserviceUrl;

    @Override
    public String getToken(long apiId) {

        TableApiServicePO tableApiServicePO = this.getById(apiId);
        LambdaQueryWrapper<TableApiAuthRequestPO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TableApiAuthRequestPO::getAppId, tableApiServicePO.getAppId());
        List<TableApiAuthRequestPO> list = tableApiAuthRequestService.list(lambdaQueryWrapper);
        List<TreeBuilder.Node> nodes = list.stream().map(i -> {
            TreeBuilder.Node node = new TreeBuilder.Node();
            node.setId(String.valueOf(i.getId()));
            node.setPid(String.valueOf(i.getPid()));
            JSONObject json = new JSONObject();
            json.put(i.getParameterKey(), i.getParameterValue());
            node.setParameter(json);
            return node;
        }).collect(Collectors.toList());
        JSONObject json = new TreeBuilder().buildTree(nodes);
        JsonNode mapper = new ObjectMapper().valueToTree(json);
        String schemaString = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"password\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"properties\": {\n" +
                "        \"test\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"properties\": {\n" +
                "            \"child\": {\n" +
                "              \"type\": \"string\"\n" +
                "            },\n" +
                "            \"child1\": {\n" +
                "              \"type\": \"string\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"test\": {\n" +
                "      \"type\": \"string\"\n" +
                "    },\n" +
                "    \"userAccount\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"properties\": {\n" +
                "        \"user\": {\n" +
                "          \"type\": \"string\"\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"test1\": {\n" +
                "      \"type\": \"string\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JsonNode schemaNode = null;

        JsonSchema jsonSchema = null;
        try {
            schemaNode = new ObjectMapper().readTree(schemaString);
            jsonSchema = JsonSchemaFactory.byDefault().getJsonSchema(schemaNode);
        } catch (Exception e) {
            log.info("getToken异常" + e.getMessage());
        }

        try {
            jsonSchema.validate(mapper);
            log.info("API 响应数据格式正确！");
        } catch (ProcessingException e) {
            log.info("API 响应数据格式错误：" + e.getMessage());
            throw new RuntimeException();
        }
        return json.toJSONString();
    }

    @Override
    public Page<TableApiPageDataDTO> getTableApiListData(TableApiPageQueryDTO dto) {
        return baseMapper.getTableApiListData(dto.page, dto);
    }

    @Override
    public TableApiServiceSaveDTO getApiServiceById(long apiId) {
        TableApiServicePO tableApiServicePO = getById(apiId);
        if (tableApiServicePO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        TableApiServiceSaveDTO tableApiServiceSaveDTO = new TableApiServiceSaveDTO();
        tableApiServiceSaveDTO.setTableApiServiceDTO(TableApiServiceMap.INSTANCES.poToDto(tableApiServicePO));
        LambdaQueryWrapper<TableApiParameterPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiParameterPO::getApiId, apiId);
        List<TableApiParameterPO> list = tableApiParameterService.list(queryWrapper);
        List<TableApiParameterDTO> tableApiParameterDTOS = TableApiParameterMap.INSTANCES.listPoToDto(list);
        tableApiServiceSaveDTO.setTableApiParameterDTO(tableApiParameterDTOS);
        tableApiServiceSaveDTO.tableSyncMode = tableSyncMode.getTableServiceSyncMode(apiId, AppServiceTypeEnum.TABLE_API.getValue());
        return tableApiServiceSaveDTO;
    }


    @Override
    public ResultEntity<Object> addTableApiService(TableApiServiceDTO dto) {
        TableApiServicePO po = this.query().eq("api_name", dto.apiName).one();
        if (po != null) {
            throw new FkException(ResultEnum.DATA_EXISTS);
        }
        TableApiServicePO data = TableApiServiceMap.INSTANCES.dtoToPo(dto);
        data.setPublish(0);
        if (!this.save(data)) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, data.id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum TableApiServiceSave(TableApiServiceSaveDTO dto) {
        updateTableApiService(dto.tableApiServiceDTO);
        tableApiParameterService.savetTableApiParameter(dto.tableApiParameterDTO, dto.tableApiServiceDTO.id);
        //覆盖方式
        dto.tableSyncMode.typeTableId = (int) dto.tableApiServiceDTO.id;
        dto.tableSyncMode.type = AppServiceTypeEnum.TABLE_API.getValue();
        tableSyncMode.tableServiceTableSyncMode(dto.tableSyncMode);
        BuildTableApiServiceDTO buildTableApiServiceDTO = buildParameter(dto);
        UserInfo userInfo = userHelper.getLoginUserInfo();
        buildTableApiServiceDTO.userId = userInfo.id;
        //推送task
        publishTaskClient.publishBuildDataServiceApi(buildTableApiServiceDTO);
        return ResultEnum.SUCCESS;
    }

    /**
     * 构建参数
     *
     * @param dto
     */
    public BuildTableApiServiceDTO buildParameter(TableApiServiceSaveDTO dto) {

        BuildTableApiServiceDTO data = new BuildTableApiServiceDTO();
        //表信息
        data.id = dto.tableApiServiceDTO.id;
        LambdaQueryWrapper<TableApiServicePO> apiQueryWrapper = new LambdaQueryWrapper<>();
        apiQueryWrapper.eq(TableApiServicePO::getId, dto.tableApiServiceDTO.id);
        TableApiServicePO tableApiServicePO = getOne(apiQueryWrapper);
        LambdaQueryWrapper<TableAppPO> appQueryWrapper = new LambdaQueryWrapper<>();
        appQueryWrapper.eq(TableAppPO::getId, tableApiServicePO.getAppId());
        TableAppPO tableAppPO = tableAppService.getOne(appQueryWrapper);

        data.apiDes = tableApiServicePO.getApiDes();
        data.apiName = tableApiServicePO.getApiName();
        data.appId = (int) tableAppPO.getId();
        data.appDesc = tableAppPO.getAppDesc();
        data.appName = tableAppPO.getAppName();

        //同步配置
        data.syncModeDTO = dto.tableSyncMode;

        return data;
    }

    @Override
    public ResultEnum delTableApiById(long id) {
        LambdaQueryWrapper<TableApiParameterPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiParameterPO::getApiId,id);
        tableApiParameterService.remove(queryWrapper);
        tableSyncMode.delTableServiceSyncMode(id, AppServiceTypeEnum.TABLE_API.getValue());
        TableApiServicePO apiService = this.getById(id);
        this.removeById(id);
        BuildDeleteTableServiceDTO buildDeleteTableService = new BuildDeleteTableServiceDTO();
        buildDeleteTableService.appId = String.valueOf(apiService.getAppId());
        buildDeleteTableService.ids = Arrays.asList(apiService.id);
        buildDeleteTableService.olapTableEnum = OlapTableEnum.DATASERVICES;
        buildDeleteTableService.userId = userHelper.getLoginUserInfo().id;
        buildDeleteTableService.delBusiness = true;
        publishTaskClient.publishBuildDeleteDataServices(buildDeleteTableService);
        return ResultEnum.SUCCESS;
    }

    @Override
    public void updateTableServiceStatus(TableServicePublishStatusDTO dto) {
        TableApiServicePO po = this.getById(dto.id);
        if (po == null) {
            log.error("【表服务修改状态失败,原因:表不存在】");
            return;
        }
        po.setPublish(dto.status);
        if (this.updateById(po)) {
            log.error("表服务修改状态失败,原因表:修改异常");
        }
    }

    @Override
    public void syncTableApi(TableApiSyncDTO dto) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        HashMap<String, Object> checkByFieldMap = dto.getCheckByFieldMap();
        String pipelTaskTraceId = (String) checkByFieldMap.get("pipelTaskTraceId");
        log.info("syncTableApi参数" + JSONObject.toJSONString(dto));
        //查询app配置信息
        TableAppPO tableAppPO = tableAppService.getById(dto.appId);
        ApiResultDTO apiResultDTO = null;
        //判断api身份验证方式
        if (tableAppPO.interfaceType == InterfaceTypeEnum.REST_API.getValue()) {
            RestApiHandler handler = InterfaceRestApiFactory.getRestApiHandlerByType(AuthenticationTypeEnum.getEnum(tableAppPO.authenticationType));
            apiResultDTO = handler.sendApi(dto.apiId);
        } else if (tableAppPO.interfaceType == InterfaceTypeEnum.WEB_SERVICE.getValue()) {

        }
        //记日志
        TableApiTaskDTO tableApiTaskDTO = new TableApiTaskDTO();
        tableApiTaskDTO.setApiId(dto.getApiId());
        tableApiTaskDTO.setTableType(dto.getTableType());
        tableApiTaskDTO.setPipelTaskTraceId(pipelTaskTraceId);
        HashMap<Integer, Object> taskMap = new HashMap<>();
        String format = simpleDateFormat.format(new Date());
        if (apiResultDTO.getFlag()){
            taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + format +" - " + apiResultDTO.getMsg());
        }else{
            taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + apiResultDTO.getMsg());
        }
        try {
            sendEmail(tableAppPO,apiResultDTO,Integer.valueOf(dto.getApiId().toString()),pipelTaskTraceId);
        }catch (Exception e){
            String msg = (String)taskMap.get(DispatchLogEnum.taskend.getValue());
            msg +=" - 邮件发送失败:" + e.getMessage();
            taskMap.put(DispatchLogEnum.taskend.getValue(),msg);
        }
        tableApiTaskDTO.setMsg(taskMap);
        publishTaskClient.savePipelTaskLog(tableApiTaskDTO);

    }
    void sendEmail(TableAppPO tableAppPO,ApiResultDTO apiResultDTO,Integer apiId,String taskTraceId) {
        TableServiceEmailDTO tableServiceEmailDTO = new TableServiceEmailDTO();
        ResultEntity<List<PipelTaskLogVO>> pipelTaskLogVo = publishTaskClient.getPipelTaskLogVo(taskTraceId);
        List<PipelTaskLogVO> data = pipelTaskLogVo.data;
        if (pipelTaskLogVo.code == ResultEnum.SUCCESS.getCode() && CollectionUtils.isNotEmpty(data)){
            List<PipelTaskLogVO> taskLogVOS = data.stream().filter(i -> i.getType() == DispatchLogEnum.taskend.getValue()).collect(Collectors.toList());
            PipelTaskLogVO taskLogVO= taskLogVOS.get(0);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    java.util.Date date = new java.util.Date();
                    Date parse = simpleDateFormat.parse(taskLogVO.msg.substring(7, 26));
                    Long second = (date.getTime() - parse.getTime()) / 1000 % 60;
                    Long minutes = (date.getTime() - parse.getTime()) / (60 * 1000) % 60;
                    tableServiceEmailDTO.duration = minutes + "m " + second + "s";
                } catch (ParseException e) {
                    e.printStackTrace();
                }
        }else {
            log.error("远程调用失败，方法名：【data-service:sendEmail】");
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        tableServiceEmailDTO.appType = 2;
        tableServiceEmailDTO.appId = (int)tableAppPO.getId();
        tableServiceEmailDTO.msg = apiResultDTO.getMsg();
        tableServiceEmailDTO.result = "【运行成功】";
        tableServiceEmailDTO.pipelTraceId = taskTraceId;
        tableServiceEmailDTO.url = "【" + dataserviceUrl + "/#/DataFactory/pipelineSettings?pipelTraceId="
                + tableServiceEmailDTO.pipelTraceId + "】";
        try {
            Map<String, String> hashMap = new HashMap<>();
            hashMap.put("数据分发api服务名称", "");
            hashMap.put("表名", String.valueOf(apiId));
            hashMap.put("运行结果", tableServiceEmailDTO.result);
            hashMap.put("运行时长", tableServiceEmailDTO.duration);
            hashMap.put("运行详情", tableServiceEmailDTO.msg);
            hashMap.put("TraceID", tableServiceEmailDTO.pipelTraceId);
            hashMap.put("页面地址", tableServiceEmailDTO.url);
            tableServiceEmailDTO.body = hashMap;
            tableService.tableServiceSendEmails(tableServiceEmailDTO);
        } catch (Exception e) {
            log.error("发邮件出错,但是不影响主流程。异常如下：" + e);
            throw new FkException(ResultEnum.ERROR,e.getMessage());
        }
    }

    @Override
    public List<BuildTableApiServiceDTO> getTableApiListByPipelineId(Integer pipelineId) {
            List<Integer> tableListByPipelineId = tableSyncMode.getTableListByPipelineId(pipelineId,4);
            if (CollectionUtils.isEmpty(tableListByPipelineId)) {
                return new ArrayList<>();
            }
            List<BuildTableApiServiceDTO> list = new ArrayList<>();

            for (Integer id : tableListByPipelineId) {
                TableApiServiceSaveDTO tableService = getApiServiceById(id);
                list.add(buildParameter(tableService));
            }
        return list;
    }

    @Override
    public ResultEnum editTableApiServiceSync(TableApiServiceSyncDTO tableApiServiceSyncDTO) {
        TableApiServicePO tableApiServicePO = baseMapper.selectById(tableApiServiceSyncDTO.getApiId());
        //判断表状态是否已发布
        if (tableApiServicePO.getPublish() != 1) {
            log.info("手动同步失败，原因：表未发布");
            return ResultEnum.TABLE_NOT_PUBLISHED;
        }
        //获取远程调用接口中需要的参数KafkaReceiveDTO
        KafkaReceiveDTO kafkaReceiveDTO = getKafkaReceive(tableApiServiceSyncDTO);
        log.info(JSON.toJSONString(kafkaReceiveDTO));

        //参数配置完毕，远程调用接口，发送参数，执行同步
        ResultEntity<Object> resultEntity = publishTaskClient.universalPublish(kafkaReceiveDTO);
        if (resultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 用于远程调用方法的参数，↑
     *
     * @return
     */
    public static KafkaReceiveDTO getKafkaReceive(TableApiServiceSyncDTO dto) {

        //拼接所需的topic
        String topic = MqConstants.TopicPrefix.TOPIC_PREFIX + OlapTableEnum.DATA_SERVICE_API.getValue() + "." + dto.appId + "." + dto.apiId;
        //获取当前时间并格式化
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTime = formatter.format(LocalDateTime.now());
        //剔除uuid生成字符串里面的"-"符号
        String pipeTaskTraceId = UUID.randomUUID().toString().replace("-", "");
        String fidata_batch_code = UUID.randomUUID().toString().replace("-", "");
        String pipelStageTraceId = UUID.randomUUID().toString().replace("-", "");
        return KafkaReceiveDTO.builder()
                .topic(topic)
                .start_time(dateTime)
                .pipelTaskTraceId(pipeTaskTraceId)
                .fidata_batch_code(fidata_batch_code)
                .pipelStageTraceId(pipelStageTraceId)
                .ifTaskStart(true)
                .topicType(1)
                .build();
    }

    @Override
    public ResultEnum enableOrDisable(Integer id) {
        TableApiServicePO tableApiServicePO = this.getById(id);
        TableApiServiceDTO tableServiceDTO = TableApiServiceMap.INSTANCES.poToDto(tableApiServicePO);
        ResultEntity<TableApiServiceDTO> result = publishTaskClient.apiEnableOrDisable(tableServiceDTO);
        if(result.getCode() != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }else {
            TableApiServiceDTO data = result.getData();
            TableApiServicePO tableApiService = TableApiServiceMap.INSTANCES.dtoToPo(data);
            if (baseMapper.updateById(tableApiService) == 0) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        return ResultEnum.SUCCESS;
    }

    private ResultEnum updateTableApiService(TableApiServiceDTO dto) {
        dto.enable = 1;
        TableApiServicePO po = this.query().eq("id", dto.id).one();
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        po = TableApiServiceMap.INSTANCES.dtoToPo(dto);
        po.id = dto.id;
        po.setPublish(3);
        this.updateById(po);
        return ResultEnum.SUCCESS;
    }
}
