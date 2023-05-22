package com.fisk.task.service.task.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.TraceConstant;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.mdc.MDCHelper;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.dto.TaskLogQuery;
import com.fisk.task.entity.TaskLogPO;
import com.fisk.task.enums.TaskStatusEnum;
import com.fisk.task.map.TaskLogMap;
import com.fisk.task.mapper.TaskLogMapper;
import com.fisk.task.service.task.IBuildKfkTaskService;
import com.fisk.task.utils.KafkaTemplateHelper;
import com.fisk.task.vo.TaskLogVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.KafkaException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

/**
 * @author gy
 */
@Service
@Slf4j
public class BuildKfkTaskServiceImpl extends ServiceImpl<TaskLogMapper, TaskLogPO> implements IBuildKfkTaskService {

    @Resource
    KafkaTemplateHelper kafkaTemplateHelper;
    @Resource
    UserHelper userHelper;
    @Resource
    TaskLogMapper taskLogMapper;


    /**
     * 创建同步数据nifi流程
     *
     * @param name     提示框内容
     * @param exchange MqConstants.ExchangeConstants.TASK_EXCHANGE_NAME
     * @param queue    MqConstants.QueueConstants.NifiTopicConstants.BUILD_NIFI_FLOW
     * @param data     data数据   BuildNifiFlowDTO
     * @return
     */
    @Override
    public ResultEntity<Object> publishTask(String name, String exchange, String queue, MQBaseDTO data) {
        log.info("MQBaseDTO的信息:{}", JSON.toJSONString(data));
        //task日志对象  tb_task_log
        TaskLogPO model = new TaskLogPO();
        String traceId = data.traceId;
        if (StringUtils.isNotEmpty(traceId)) {
            model.traceId = traceId;
        } else {
            setTraceID(data);
        }
        if (StringUtils.isNotEmpty(traceId)) {
            model.traceId = traceId;
        } else {
            model.traceId = UUID.randomUUID().toString();
        }
        //数据 对象转为字符串
        String str = JSON.toJSONString(data);
        model.taskName = name;
        model.taskExchange = exchange;
        model.taskQueue = queue;
        model.taskStatus = TaskStatusEnum.TASK_BUILD;
        model.createUser = String.valueOf(data.userId);

        int dataMaxLength = 2000;
        if (str.length() <= dataMaxLength) {
            model.taskData = str;
        }
        //保存日志
        this.save(model);

        data.logId = model.id;
        data.traceId = model.traceId;
        //数据 转为 json字符串
        str = JSON.toJSONString(data);
        try {
            //同步方式发送数据
            log.info("发送消息");
            kafkaTemplateHelper.sendMessageSync(queue, str);
            model.taskSendOk = true;
            this.updateById(model);
            return ResultEntityBuild.build(ResultEnum.SUCCESS);
        } catch (KafkaException ex) {
            log.error("【{}】消息发布失败，消息内容【{}】，ex：", queue, str, ex);
            model.taskSendOk = false;
            this.updateById(model);
            return ResultEntityBuild.build(ResultEnum.TASK_PUBLISH_ERROR);
        } catch (Exception ex) {
            log.error("系统报错，", ex);
            return ResultEntityBuild.build(ResultEnum.ERROR);
        }
    }

    @Override
    public Page<TaskLogVO> getUserAllMessage(TaskLogQuery query) {
        UserInfo userInfo = userHelper.getLoginUserInfo();
        query.userId = userInfo.id;
        Page<TaskLogPO> taskLogPo = taskLogMapper.listTaskLog(query.page, query);
        Page<TaskLogVO> page = new Page<TaskLogVO>();
        List<TaskLogPO> records = taskLogPo.getRecords();
        List<TaskLogVO> taskLogVos = TaskLogMap.INSTANCES.poToVo(records);
        for (TaskLogVO vo : taskLogVos) {
            vo.taskLogs = TaskLogMap.INSTANCES.listPoToDto(this.query().eq("trace_id", vo.traceId).list());
        }
        page.setSize(taskLogPo.getSize());
        page.setCurrent(taskLogPo.getCurrent());
        page.setTotal(taskLogPo.getTotal());
        page.setOptimizeCountSql(taskLogPo.optimizeCountSql());
        page.setOrders(taskLogPo.getOrders());
        page.setRecords(taskLogVos);
        return page;

    }

    private void setTraceID(MQBaseDTO data) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            data.traceId = request.getHeader(TraceConstant.HTTP_HEADER_TRACE);
            data.spanId = MDCHelper.setSpanId();
        }
    }


}
