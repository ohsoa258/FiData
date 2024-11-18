package com.fisk.task.pipeline2;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.davis.client.model.ProcessGroupEntity;
import com.davis.client.model.ProcessGroupStatusDTO;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.mdc.MDCHelper;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.tasknifi.TaskHierarchyDTO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.customscript.CustomScriptInfoDTO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.dispatchlog.DispatchExceptionHandlingDTO;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.entity.PipelineTableLogPO;
import com.fisk.task.enums.DispatchLogEnum;
import com.fisk.task.enums.MyTopicStateEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.pipeline.IPipelineTaskPublishCenter;
import com.fisk.task.mapper.PipelineTableLogMapper;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.po.TableTopicPO;
import com.fisk.task.service.dispatchLog.IPipelJobLog;
import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.service.dispatchLog.IPipelStageLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.nifi.IOlap;
import com.fisk.task.service.nifi.IPipelineTableLog;
import com.fisk.task.service.nifi.ITableNifiSettingService;
import com.fisk.task.service.pipeline.ITableTopicService;
import com.fisk.task.utils.KafkaTemplateHelper;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HeartbeatService {
    @Resource
    RedisUtil redisUtil;
    @Value("${nifi.pipeline.waitTime}")
    private String waitTime;
    @Value("${nifi.pipeline.maxTime}")
    public String maxTime;
    @Resource
    KafkaTemplateHelper kafkaTemplateHelper;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    private DataModelClient dataModelClient;
    @Resource
    private UserClient userClient;
    @Resource
    IOlap iOlap;
    @Resource
    IPipelJobLog iPipelJobLog;
    @Resource
    IPipelLog iPipelLog;
    @Resource
    IPipelTaskLog iPipelTaskLog;
    @Resource
    IPipelineTaskPublishCenter iPipelineTaskPublishCenter;
    @Resource
    ITableNifiSettingService iTableNifiSettingService;
    @Resource
    PublishTaskController publishTaskController;
    @Resource
    ITableTopicService tableTopicService;
    @Resource
    IPipelStageLog iPipelStageLog;
    @Resource
    private PipelineTableLogMapper pipelineTableLog;
    @Resource
    private IPipelineTableLog pipelineTableLogService;

    @Value("${fiData-data-dw-source}")
    private String dwId;

    public void heartbeatService2(String data, Acknowledgment acke) {
        //List<KafkaReceiveDTO>
        log.info("心跳服务,参数:{}", data);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<String> msg = JSON.parseArray(data, String.class);
            for (String message : msg) {
                KafkaReceiveDTO kafkaReceive = JSON.parseObject(message, KafkaReceiveDTO.class);
                String topic = kafkaReceive.topic;
                String pipelTraceId = kafkaReceive.pipelTraceId;
                //管道总的pipelTraceId
                if (StringUtils.isEmpty(kafkaReceive.pipelTraceId)) {
                    kafkaReceive.pipelTraceId = UUID.randomUUID().toString();
                    MDCHelper.setPipelTraceId(kafkaReceive.pipelTraceId);
                }

                Map<String, Object> map = new HashMap<>();
                map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                map.put(DispatchLogEnum.taskcount.getName(), kafkaReceive.numbers + "");
                log.info("打印条数81" + JSON.toJSONString(map));
                redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + ":" + kafkaReceive.pipelTaskTraceId, map, 3600);
                // 第一步  创建rediskey 先判断有没有这个key,没有创建,设置过期时间30秒
                String value = UUID.randomUUID().toString();
                //刷新时间和创建key或者修改value,会产生延时任务
                redisUtil.set(RedisKeyEnum.DELAYED_TASK.getName() + ":" + topic, value, Long.parseLong(waitTime) * 100);
                redisUtil.set(topic, value, Long.parseLong(waitTime));
                // 第二步  创建延时队列,延时时间要比rediskey过期时间长5秒,且可配置,需要携带的参数有报错信息
                DelayedTask2 delayedTask2 = new DelayedTask2(kafkaReceive, value, topic, kafkaTemplateHelper,
                        dataFactoryClient,
                        iOlap, iPipelJobLog, iPipelLog,
                        iPipelTaskLog, redisUtil,
                        iTableNifiSettingService,
                        dataAccessClient,
                        iPipelineTaskPublishCenter,
                        publishTaskController);
                Timer timer = new Timer();
                timer.schedule(delayedTask2, (Long.parseLong(waitTime) + 5) * 1000);
                //记报错日志
                if (!StringUtils.isEmpty(kafkaReceive.message)) {
                    // 第三步  如果有报错,记录报错信息
                    Map<Integer, Object> errorMap = new HashMap<>();
                    errorMap.put(DispatchLogEnum.stagestate.getValue(), kafkaReceive.message);
                    iPipelStageLog.savePipelTaskStageLog(kafkaReceive.pipelStageTraceId, kafkaReceive.pipelTaskTraceId, errorMap);
                }
            }
        } catch (Exception e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
        } finally {
            acke.acknowledge();
        }
    }

    public void endService(String data, Acknowledgment acke) {
        //List<KafkaReceiveDTO>
        log.info("my-topic服务,参数:{}", data);
        Boolean flag = false;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<String> msg = JSON.parseArray(data, String.class);
            log.info("my-topic接收条数{}", msg.size());
            for (String message : msg) {
                KafkaReceiveDTO kafkaReceive = JSON.parseObject(message, KafkaReceiveDTO.class);
                try {
                    String topic = kafkaReceive.topic;
                    String pipelTraceId = kafkaReceive.pipelTraceId;
                    //管道总的pipelTraceId
                    if (StringUtils.isEmpty(kafkaReceive.pipelTraceId)) {
                        kafkaReceive.pipelTraceId = UUID.randomUUID().toString();
                        MDCHelper.setPipelTraceId(kafkaReceive.pipelTraceId);
                    }
                    String[] split = topic.split("\\.");
                    LambdaQueryWrapper<TableTopicPO> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(TableTopicPO::getTopicName, topic).eq(TableTopicPO::getDelFlag, 1);
                    TableTopicPO topicPO = tableTopicService.getOne(queryWrapper);
                    if (split.length == 7) {
                        Map<String, Object> map = new HashMap<>();
                        map.put(DispatchLogEnum.taskend.getName(), simpleDateFormat.format(new Date()));
                        map.put(DispatchLogEnum.taskcount.getName(), kafkaReceive.numbers + "");
                        log.info("打印条数81" + JSON.toJSONString(map));
                        redisUtil.hmset(RedisKeyEnum.PIPEL_TASK.getName() + ":" + kafkaReceive.pipelTaskTraceId, map, 3600);
                        iPipelTaskLog.updatePipelTaskLog(kafkaReceive.pipelTaskTraceId);
                        Boolean setnx;
                        do {
                            Thread.sleep(200);
//                        log.info("endService获取锁PipelLock:{}",kafkaReceive.pipelTraceId);
                            setnx = redisUtil.setnx("PipelLock:" + kafkaReceive.pipelTraceId, 100, TimeUnit.SECONDS);
                        } while (!setnx);
                        // 获取redis中TASK任务
                        Map<Object, Object> hmget = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId);
                        //管道快照续期
                        redisUtil.expire(RedisKeyEnum.PIPEL_TRACE_ID.getName() + ":" + pipelTraceId, Long.parseLong(maxTime));
                        redisUtil.expire(RedisKeyEnum.PIPEL_JOB_TRACE_ID.getName() + ":" + pipelTraceId, Long.parseLong(maxTime));
                        redisUtil.expire(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + pipelTraceId, Long.parseLong(maxTime));
                        //获取my-topic运行状态
                        TaskHierarchyDTO taskHierarchy = JSON.parseObject(hmget.get(topicPO.getComponentId().toString()).toString(), TaskHierarchyDTO.class);
                        if (taskHierarchy.myTopicState.equals(MyTopicStateEnum.RUNNING)) {
                            redisUtil.del("PipelLock:" + kafkaReceive.pipelTraceId);
                            continue;
                        }
                        taskHierarchy.setMyTopicState(MyTopicStateEnum.RUNNING);
                        HashMap<Object, Object> map1 = new HashMap<>();
                        map1.put(topicPO.getComponentId().toString(), JSON.toJSONString(taskHierarchy));
                        //更新my-topic运行状态
                        redisUtil.hmsetForDispatch(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + kafkaReceive.pipelTraceId, map1, Long.parseLong(maxTime));
                        redisUtil.del("PipelLock:" + kafkaReceive.pipelTraceId);
                        //同方法内@Async注解不生效所以需获取本service的代理对象
                        HeartbeatService heartbeatService = NifiHelper.getBean(HeartbeatService.class);
                        //异步执行避免阻塞
                        heartbeatService.sendKafka(topicPO, kafkaReceive);
                    } else if (split.length == 6) {
                        String state = (String) redisUtil.get(RedisKeyEnum.DELAYED_TASK.getName() + ":" + kafkaReceive.pipelTaskTraceId);
                        if (state.equals(MyTopicStateEnum.RUNNING.getName())) {
                            continue;
                        }
                        redisUtil.set(RedisKeyEnum.DELAYED_TASK.getName() + ":" + kafkaReceive.pipelTaskTraceId, MyTopicStateEnum.RUNNING.getName(), Long.parseLong(maxTime));
                        //同方法内@Async注解不生效所以需获取本service的代理对象
                        HeartbeatService heartbeatService = NifiHelper.getBean(HeartbeatService.class);
                        //异步执行避免阻塞
                        heartbeatService.sendKafka(topicPO, kafkaReceive);
                    }
                } catch (Exception e) {
                    log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
                    redisUtil.del("PipelLock:" + kafkaReceive.pipelTraceId);
                    DispatchExceptionHandlingDTO dto = buildDispatchExceptionHandling(kafkaReceive);
                    try {
                        iPipelJobLog.exceptionHandlingLog(dto);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        } catch (Exception e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
        } finally {
            acke.acknowledge();
        }
    }

    /**
     * 构建 TableMetaDataObject 对象
     */
    public static DispatchExceptionHandlingDTO buildDispatchExceptionHandling(KafkaReceiveDTO kafkaReceive) {
        /*dispatchExceptionHandlingDTO.pipleName = pipelName;
        dispatchExceptionHandlingDTO.JobName = jobName;*/
        return DispatchExceptionHandlingDTO.builder()
                .comment("发布中心报错")
                .pipelTraceId(kafkaReceive.pipelTraceId)
                .pipelJobTraceId(kafkaReceive.pipelJobTraceId)
                .pipelStageTraceId(kafkaReceive.pipelStageTraceId)
                .pipelTaskTraceId(kafkaReceive.pipelTaskTraceId)
                .build();
    }


    @Async
    public void sendKafka(TableTopicPO topicPO, KafkaReceiveDTO kafkaReceive) {
        try {
            String groupId = "";
            List<TableNifiSettingPO> list = new ArrayList<>();
            String[] split = topicPO.getTopicName().split("\\.");
            //表类型
            int tableType = 0;
            //表id
            int tableId = 0;
            //应用id
            int appId = 0;
            if (split.length == 6) {
                list = iTableNifiSettingService.query().eq("type", split[3]).eq("table_access_id", split[5]).list();
                //表类型
                tableType = Integer.parseInt(split[3]);
                //表id
                tableId = Integer.parseInt(split[5]);
                //应用id
                appId = Integer.parseInt(split[4]);
            } else if (split.length == 7) {
                list = iTableNifiSettingService.query().eq("type", split[4]).eq("table_access_id", split[6]).list();
                //表类型
                tableType = Integer.parseInt(split[4]);
                //表id
                tableId = Integer.parseInt(split[6]);
                //应用id
                appId = Integer.parseInt(split[5]);
            }
            if (!CollectionUtils.isEmpty(list)) {
                groupId = list.get(0).tableComponentId;
            }
            //只有是nifi处理的任务才有这个groupId
            if (!StringUtils.isEmpty(groupId)) {
                Integer flowFilesQueued;
                Integer activeThreadCount;
                //调用nifi api，检查单表同步任务是否真的结束
                do {
                    Thread.sleep(500);
                    ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(groupId);
                    ProcessGroupStatusDTO status = processGroup.getStatus();
                    //flowFilesQueued 组内流文件数量,如果为0代表组内无流文件
                    //activeThreadCount 组内活跃线程数量，为0代表没有正在工作的组件
                    flowFilesQueued = status.getAggregateSnapshot().getFlowFilesQueued();
                    activeThreadCount = status.getAggregateSnapshot().getActiveThreadCount();
                    log.info("管道内剩余流文件flowFilesQueued:{}", flowFilesQueued);
                    log.info("管道内正在执行线程数activeThreadCount:{}", activeThreadCount);
                } while (activeThreadCount != 0 || flowFilesQueued != 0);

                ////////////////////////////////////////////////////////////////////////////////////////////////////////
                //如果是数仓维度表或事实表，则查看该表是否有加载后语句，如果有的话 执行
                Connection connection = null;
                Statement statement = null;
                List<CustomScriptInfoDTO> sqls = new ArrayList<>();
                try {
                    if (Objects.equals(tableType, OlapTableEnum.DIMENSION.getValue())
                            || Objects.equals(tableType, OlapTableEnum.FACT.getValue())
                    ) {
                        //远程调用数仓建模的接口，获取该维度表/事实表待执行的自定义加载后sql语句以及jdbc连接dmp_dw所需的信息
                        ResultEntity<List<CustomScriptInfoDTO>> resultEntity =
                                dataModelClient.getCustomSqlByTblIdType(tableId, tableType);
                        if (resultEntity.code != ResultEnum.SUCCESS.getCode()) {
                            log.error("数仓建模获取自定义加载后sql失败！");
                            throw new FkException(ResultEnum.GET_CUSTOM_SQL_ERROR);
                        }
                        sqls = resultEntity.getData();

                        if (!CollectionUtils.isEmpty(sqls)) {
                            //按sequence 执行顺序排序
                            sqls.sort(Comparator.comparingInt(CustomScriptInfoDTO::getSequence));
                            //如果存在待执行的加载后sql语句,则通过dmp_dw的jdbc按顺序执行加载后语句
                            ResultEntity<DataSourceDTO> result = userClient.getFiDataDataSourceById(Integer.parseInt(dwId));
                            if (result.code != ResultEnum.SUCCESS.getCode()) {
                                log.error("userclient无法查询到dw库的连接信息");
                                throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
                            }
                            DataSourceDTO source = result.getData();
                            AbstractCommonDbHelper dbHelper = new AbstractCommonDbHelper();
                            connection = dbHelper.connection(source.getConStr(), source.getConAccount(), source.getConPassword(), source.getConType());
                            statement = connection.createStatement();

                            for (CustomScriptInfoDTO sql : sqls) {
                                //执行sql
                                log.info("执行自定义加载后sql: " + sql.getScript() + "执行顺序:" + sql.getSequence());
                                //不使用批处理原因：executeBatch不能执行select
                                statement.execute(sql.getScript());
                            }
                        }

                    }
                } catch (Exception e) {
                    //如果报错，
                    //1、像nifi报错后连接的错误处理组件一样，将报错信息存储到对应的数据库表（或者直接发送消息给报错中心）2、抛出异常，终止流程
                    log.error("数仓建模表执行自定义加载后sql失败:" + e);
                    //1、像nifi报错后连接的错误处理组件一样，将报错信息存储到对应的数据库表（或者直接发送消息给报错中心）
                    //单表同步
                    if (split.length == 6) {
                        PipelineTableLogPO pipelineTableLogPO = new PipelineTableLogPO();
                        pipelineTableLogPO.comment = "执行数仓自定义加载后sql失败，sql列表:" +
                                sqls.stream().map(CustomScriptInfoDTO::getScript).collect(Collectors.toList()) +
                                " 原因：" +
                                e;
                        pipelineTableLogPO.tableId = tableId;
                        pipelineTableLogPO.tableType = tableType;
                        pipelineTableLogPO.appId = appId;
                        //状态,未开始1,正在运行2,运行成功3,失败4
                        pipelineTableLogPO.state = 4;
                        //0手动 1管道调度
                        pipelineTableLogPO.dispatchType = 0;
                        // 指定日期时间格式
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            pipelineTableLogPO.startTime = dateFormat.parse(kafkaReceive.start_time);
                        } catch (Exception ex) {
                            log.error("时间格式转换异常");
                            pipelineTableLogPO.startTime = kafkaReceive.endTime;
                        }
                        pipelineTableLog.insert(pipelineTableLogPO);
                        log.info("新增的tb_pipeline_table_log的主键id为" + pipelineTableLogPO.getId());

                        //更新日志的创建时间，以供表批量同步日志页面捕获到数仓同步后加载sql的执行报错信息
                        pipelineTableLogService.update(
                                new LambdaUpdateWrapper<PipelineTableLogPO>()
                                        .set(PipelineTableLogPO::getCreateTime, kafkaReceive.endTime)
                                        .eq(PipelineTableLogPO::getId, pipelineTableLogPO.getId())
                        );
                        //管道调度
                    } else if (split.length == 7) {
                        PipelineTableLogPO pipelineTableLogPO = new PipelineTableLogPO();
                        pipelineTableLogPO.comment = "执行数仓自定义加载后sql失败，sql列表:" +
                                sqls.stream().map(CustomScriptInfoDTO::getScript).collect(Collectors.toList()) +
                                " 原因：" +
                                e;
                        pipelineTableLogPO.componentId = topicPO.getComponentId();
                        pipelineTableLogPO.tableId = tableId;
                        pipelineTableLogPO.tableType = tableType;
                        pipelineTableLogPO.appId = appId;
                        //状态,未开始1,正在运行2,运行成功3,失败4
                        pipelineTableLogPO.state = 4;
                        //0手动 1管道调度
                        pipelineTableLogPO.dispatchType = 1;
                        // 指定日期时间格式
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            pipelineTableLogPO.startTime = dateFormat.parse(kafkaReceive.start_time);
                        } catch (Exception ex) {
                            log.error("时间格式转换异常");
                            pipelineTableLogPO.startTime = kafkaReceive.endTime;
                        }
                        pipelineTableLog.insert(pipelineTableLogPO);

                        log.info("新增的tb_pipeline_table_log的主键id为" + pipelineTableLogPO.getId());

                        //更新日志的创建时间，以供表批量同步日志页面捕获到数仓同步后加载sql的执行报错信息
                        pipelineTableLogService.update(
                                new LambdaUpdateWrapper<PipelineTableLogPO>()
                                        .set(PipelineTableLogPO::getCreateTime, kafkaReceive.endTime)
                                        .eq(PipelineTableLogPO::getId, pipelineTableLogPO.getId())
                        );

                        //管道流程的话 数仓加载后语句执行报错则需要抛出异常
                        throw new FkException(ResultEnum.NIFI_EXCUTE_MODEL_CUSTOM_SQL_ERROR);
                    }

                } finally {
                    AbstractDbHelper.closeStatement(statement);
                    AbstractDbHelper.closeConnection(connection);
                }
                ////////////////////////////////////////////////////////////////////////////////////////////////////////

                if (!StringUtils.isEmpty(kafkaReceive.message)) {
                    DispatchExceptionHandlingDTO dto = buildDispatchExceptionHandling(kafkaReceive);
                    iPipelJobLog.exceptionHandlingLog(dto);
                    Map<Object, Object> hmJob = redisUtil.hmget(RedisKeyEnum.PIPEL_JOB_TRACE_ID.getName() + ":" + dto.pipelTraceId);
                    Map<Object, Object> hmTask = redisUtil.hmget(RedisKeyEnum.PIPEL_TASK_TRACE_ID.getName() + ":" + dto.pipelTraceId);
                    log.info("修改完的job与task结构:{},{}", JSON.toJSONString(hmJob), JSON.toJSONString(hmTask));
                }
                // 任务结束中心的topic为 : task.build.task.over
                log.info("my-topic服务发送到任务:{}", JSON.toJSONString(kafkaReceive));
                kafkaTemplateHelper.sendMessageAsync(MqConstants.QueueConstants.BUILD_TASK_OVER_FLOW, JSON.toJSONString(kafkaReceive));
            }
        } catch (Exception e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
            redisUtil.del("PipelLock:" + kafkaReceive.pipelTraceId);
            DispatchExceptionHandlingDTO dto = buildDispatchExceptionHandling(kafkaReceive);
            try {
                iPipelJobLog.exceptionHandlingLog(dto);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
