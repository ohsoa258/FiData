package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataservice.dto.tableapi.ApiResultDTO;
import com.fisk.dataservice.dto.tableapi.TableApiSyncDTO;
import com.fisk.dataservice.dto.tableapi.TableApiTaskDTO;
import com.fisk.dataservice.dto.tableservice.TableServiceEmailDTO;
import com.fisk.dataservice.entity.TableApiLogPO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.enums.AuthenticationTypeEnum;
import com.fisk.dataservice.enums.InterfaceTypeEnum;
import com.fisk.dataservice.enums.SpecialTypeEnum;
import com.fisk.dataservice.handler.ksf.KsfWebServiceHandler;
import com.fisk.dataservice.handler.ksf.factory.KsfInterfaceFactory;
import com.fisk.dataservice.handler.restapi.RestApiHandler;
import com.fisk.dataservice.handler.restapi.factory.InterfaceRestApiFactory;
import com.fisk.dataservice.handler.webservice.WebServiceHandler;
import com.fisk.dataservice.handler.webservice.factory.InterfaceWebServiceFactory;
import com.fisk.dataservice.service.ITableApiLogService;
import com.fisk.dataservice.service.ITableApiService;
import com.fisk.dataservice.service.ITableAppManageService;
import com.fisk.dataservice.service.ITableService;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.dispatchlog.PipelTaskLogVO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: wangjian
 * @Date: 2023-11-29
 * @Description:
 */
@Service
@Slf4j
public class AsyncImpl {
    @Resource
    ITableAppManageService tableAppService;
    @Resource
    private ITableApiLogService tableApiLogService;

    @Resource
    private ITableService tableService;

    @Resource
    private PublishTaskClient publishTaskClient;

    @Resource
    private ITableApiService tableApiService;

    @Value("${dataservice.scan.api_address}")
    private String dataserviceUrl;
    @Value("${dataservice.retrynum}")
    private Integer retryNum;
    @Value("${dataservice.retrytime}")
    private Integer retryTime;
    @Resource
    private RedisUtil redisUtil;

    @Async
    public ResultEnum syncTableApi(TableApiSyncDTO dto) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        HashMap<String, Object> checkByFieldMap = dto.getCheckByFieldMap();
        String pipelTaskTraceId = (String) checkByFieldMap.get("pipelTaskTraceId");

        log.info("syncTableApi参数" + JSONObject.toJSONString(dto));


        TableApiTaskDTO tableApiTaskDTO = new TableApiTaskDTO();
        tableApiTaskDTO.setApiId(dto.getApiId());
        tableApiTaskDTO.setTableType(dto.getTableType());
        tableApiTaskDTO.setPipelTaskTraceId(pipelTaskTraceId);
        HashMap<Integer, Object> taskMap = new HashMap<>();
        String format = simpleDateFormat.format(new Date());
        TableApiLogPO tableApiLogPO = new TableApiLogPO();
        //查询app配置信息
        TableAppPO tableAppPO = tableAppService.getById(dto.appId);

        TableApiServicePO tableApiServicePO = tableApiService.getById(dto.getApiId());

        ApiResultDTO apiResultDTO = null;
        //判断api类型
        if (tableAppPO.interfaceType == InterfaceTypeEnum.REST_API.getValue()) {
            RestApiHandler handler = InterfaceRestApiFactory.getRestApiHandlerByType(AuthenticationTypeEnum.getEnum(tableAppPO.authenticationType));
            for (int i = 0; i < retryNum; i++) {

                apiResultDTO = handler.sendApi(dto.apiId);
                if (apiResultDTO.getFlag()) {
                    break;
                }
                log.info("第" + (i + 1) + "次发送api");
                try {
                    log.info("发送异常:" + apiResultDTO.getMsg() + ",等待" + retryTime / 1000 + "秒重新发送。");
                    Thread.sleep(retryTime);
                } catch (InterruptedException e) {
                    String msg = " - api异常:" + apiResultDTO.getMsg() + "重试异常:" + e.getMessage();
                    taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + msg);
                    tableApiTaskDTO.setMsg(taskMap);
                    publishTaskClient.savePipelTaskLog(tableApiTaskDTO);
                    throw new RuntimeException(e);
                }
            }
        } else if (tableAppPO.interfaceType == InterfaceTypeEnum.WEB_SERVICE.getValue()) {
            Boolean setnx = redisUtil.setnx(RedisKeyEnum.TABLE_KSF_WEB_SERVER_SYNC.getName() + tableApiServicePO.getId(), retryNum*retryTime/1000+100, TimeUnit.SECONDS);
            if (setnx) {
                for (int i = 0; i < retryNum; i++) {
                    switch (SpecialTypeEnum.getEnum(tableApiServicePO.specialType)) {
                        case NONE:
                            WebServiceHandler webServiceHandler = InterfaceWebServiceFactory.getWebServiceHandlerByType();
                            apiResultDTO = webServiceHandler.sendApi(dto.apiId);
                            break;
                        case KSF_ITEM_DATA:
                        case KSF_NOTICE:
                        case KSF_INVENTORY_STATUS_CHANGES:
                        case KSF_ACKNOWLEDGEMENT:
                            KsfWebServiceHandler ksfWebServiceHandler = KsfInterfaceFactory.getKsfWebServiceHandlerByType(SpecialTypeEnum.getEnum(tableApiServicePO.specialType));
                            apiResultDTO = ksfWebServiceHandler.sendApi(tableAppPO, dto.apiId);
                            break;
                        default:
                            break;
                    }
                    if (apiResultDTO.getFlag()) {
                        break;
                    }
                    log.info("第" + (i + 1) + "次发送api");
                    try {
                        log.info("发送异常:" + apiResultDTO.getMsg() + ",等待" + retryTime / 1000 + "秒重新发送。");
                        Thread.sleep(retryTime);
                    } catch (InterruptedException e) {
                        String msg = " - api异常:" + apiResultDTO.getMsg() + "重试异常:" + e.getMessage();
                        taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + msg);
                        tableApiTaskDTO.setMsg(taskMap);
                        publishTaskClient.savePipelTaskLog(tableApiTaskDTO);
                    }
                }
                redisUtil.del(RedisKeyEnum.TABLE_KSF_WEB_SERVER_SYNC.getName() + tableApiServicePO.getId());
            } else {
                apiResultDTO = new ApiResultDTO();
                apiResultDTO.setFlag(false);
                apiResultDTO.setMsg("上次同步未完成");
            }
        }
        //记日志

        if (apiResultDTO.getFlag()) {
            taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.SUCCESSFUL_RUNNING.getName() + " - " + format + " - " + apiResultDTO.getMsg());
            tableApiLogPO.setApiId(dto.apiId.intValue());
            tableApiLogPO.setNumber(apiResultDTO.getNumber());
            tableApiLogPO.setImportantInterface(tableApiServicePO.getImportantInterface());
            tableApiLogPO.setStatus(1);
        } else {
            taskMap.put(DispatchLogEnum.taskend.getValue(), NifiStageTypeEnum.RUN_FAILED.getName() + " - " + format + " - ErrorMessage:" + apiResultDTO.getMsg());

            tableApiLogPO.setApiId(dto.apiId.intValue());
            tableApiLogPO.setNumber(apiResultDTO.getNumber());
            tableApiLogPO.setStatus(0);
        }
        try {
            tableApiLogService.save(tableApiLogPO);
            sendEmail(tableAppPO, apiResultDTO, Integer.valueOf(dto.getApiId().toString()), pipelTaskTraceId);
        } catch (Exception e) {
            String msg = (String) taskMap.get(DispatchLogEnum.taskend.getValue());
            msg += " - 邮件发送失败:" + e.getMessage();
            taskMap.put(DispatchLogEnum.taskend.getValue(), msg);
        }
        tableApiTaskDTO.setMsg(taskMap);

        publishTaskClient.savePipelTaskLog(tableApiTaskDTO);
        return ResultEnum.SUCCESS;
    }

    void sendEmail(TableAppPO tableAppPO, ApiResultDTO apiResultDTO, Integer apiId, String taskTraceId) {
        TableServiceEmailDTO tableServiceEmailDTO = new TableServiceEmailDTO();
        ResultEntity<List<PipelTaskLogVO>> pipelTaskLogVo = publishTaskClient.getPipelTaskLogVo(taskTraceId);
        List<PipelTaskLogVO> data = pipelTaskLogVo.data;
        if (pipelTaskLogVo.code == ResultEnum.SUCCESS.getCode() && CollectionUtils.isNotEmpty(data)) {
            List<PipelTaskLogVO> taskLogVOS = data.stream().filter(i -> i.getType() == DispatchLogEnum.taskend.getValue()).collect(Collectors.toList());
            PipelTaskLogVO taskLogVO = taskLogVOS.get(0);
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
        } else {
            log.error("远程调用失败，方法名：【data-service:sendEmail】");
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        tableServiceEmailDTO.appType = 2;
        tableServiceEmailDTO.appId = (int) tableAppPO.getId();
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
            log.error("发邮件出错,但是不影响主流程。异常如下：" + e.getMessage());
        }
    }
}
