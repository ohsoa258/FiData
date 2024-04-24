package com.fisk.task.task;

import com.fisk.task.service.dispatchLog.IPipelLog;
import com.fisk.task.service.dispatchLog.IPipelTaskLog;
import com.fisk.task.service.pipeline.PipelLogRecipientsService;
import com.fisk.task.service.pipeline.TableServiceRecipientsService;
import com.fisk.task.vo.statistics.PipelLineDetailVO;
import com.fisk.task.vo.tableservice.TableServiceDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 定时任务调度
 */
@Slf4j
@Component("SubscribeEmailTask")
public class SubscribeEmailTask {
    @Resource
    PipelLogRecipientsService pipelLogRecipientsService;

    @Resource
    TableServiceRecipientsService tableServiceRecipientsService;
    @Resource
    IPipelLog pipelLog;

    @Resource
    IPipelTaskLog pipelTaskLog;

    /**
     * 管道订阅邮件
     *
     * @return
     * @throws Exception
     */
    public void pipelLogSubscribeEmail() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<PipelLineDetailVO> detailLog = pipelLog.getDetailLog();
        StringBuilder content = new StringBuilder("<html><head></head><body>");
        content.append("<br />");
        content.append("以下是昨日至今日管道运行状况:\n");
        content.append("<table border=\"5\" style=\"border:solid 1px #E8F2F9;font-size=12px;;font-size:18px;\">");
        content.append("<tr style=\"background-color: #428BCA; color:#ffffff\"><th>活动名称</th><th>执行开始时间</th><th>执行结束时间</th><th>执行持续时间</th><th>执行状态</th><th>执行结果</th></tr>");
        int k = 0;
        for (PipelLineDetailVO detailVO : detailLog) {
            k++;
            content.append("<tr>");
            //活动名称
            content.append("<td align=\"center\">").append(detailVO.workflowName).append("</td>");
            //执行开始时间
            if (detailVO.startDateTime != null){
                content.append("<td align=\"center\">").append(format.format(detailVO.startDateTime)).append("</td>");
            }else {
                content.append("<td align=\"center\">").append("暂无").append("</td>");
            }
            //执行结束时间
            if (detailVO.endDateTime != null){
                content.append("<td align=\"center\">").append(format.format(detailVO.endDateTime)).append("</td>");
            }else {
                content.append("<td align=\"center\">").append("暂无").append("</td>");
            }
            //执行持续时间
            if (detailVO.runningTime != null){
                content.append("<td align=\"center\">").append(detailVO.runningTime).append("</td>");
            }else {
                content.append("<td align=\"center\">").append("暂无").append("</td>");
            }
            //执行zhuangt
            content.append("<td align=\"center\">").append(detailVO.runningStatus).append("</td>");
            //执行结果
            if ("成功".equals(detailVO.runningResult)) {
                content.append("<td align=\"center\" style=\"background-color: #006400; color:#ffffff\">").append(detailVO.runningResult).append("</td>");
            } else if ("失败".equals(detailVO.runningResult)) {
                content.append("<td align=\"center\" style=\"background-color: #fd1717; color:#ffffff\">").append(detailVO.runningResult).append("</td>");
            } else if ("暂无".equals(detailVO.runningResult)) {
                content.append("<td align=\"center\" style=\"background-color: #ffba5c; color:#ffffff\">").append(detailVO.runningResult).append("</td>");
            }
            content.append("</tr>");
        }
        content.append("</table>");
        content.append("</body></html>");
        pipelLogRecipientsService.sendPipelLogSendEmails(content.toString());
    }

    /**
     * 表服务订阅邮件
     *
     * @return
     * @throws Exception
     */
    public void tableServiceSubscribeEmail() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<TableServiceDetailVO> detailLog = pipelTaskLog.getDetailLog();
        StringBuilder content = new StringBuilder("<html><head></head><body>");
        content.append("<br />");
        content.append("以下是昨日至今日表服务运行状况:\n");
        content.append("<table border=\"5\" style=\"border:solid 1px #E8F2F9;font-size=12px;;font-size:18px;\">");
        content.append("<tr style=\"background-color: #428BCA; color:#ffffff\"><th>活动名称</th><th>执行开始时间</th><th>执行结束时间</th><th>执行持续时间</th><th>执行状态</th><th>执行结果</th></tr>");
        int k = 0;
        for (TableServiceDetailVO detailVO : detailLog) {
            k++;
            content.append("<tr>");
            //活动名称
            content.append("<td align=\"center\">").append(detailVO.tableServiceName).append("</td>");
            //执行开始时间
            content.append("<td align=\"center\">").append(format.format(detailVO.startDateTime)).append("</td>");
            //执行结束时间
            content.append("<td align=\"center\">").append(format.format(detailVO.endDateTime)).append("</td>");
            //执行持续时间
            content.append("<td align=\"center\">").append(format(Integer.parseInt(detailVO.runningTime))).append("</td>");
            //执行zhuangt
            content.append("<td align=\"center\">").append(detailVO.runningStatus).append("</td>");
            //执行结果
            if ("成功".equals(detailVO.runningResult)) {
                content.append("<td align=\"center\" style=\"background-color: #006400; color:#ffffff\">").append(detailVO.runningResult).append("</td>");
            } else if ("失败".equals(detailVO.runningResult)) {
                content.append("<td align=\"center\" style=\"background-color: #fd1717; color:#ffffff\">").append(detailVO.runningResult).append("</td>");
            } else if ("暂无".equals(detailVO.runningResult)) {
                content.append("<td align=\"center\" style=\"background-color: #ffba5c; color:#ffffff\">").append(detailVO.runningResult).append("</td>");
            }
            content.append("</tr>");
        }
        content.append("</table>");
        content.append("</body></html>");
        tableServiceRecipientsService.sendTableServiceSendEmails(content.toString());
    }

    public String format(int seconds) {
        StringBuilder content = new StringBuilder();
        // 获取小时数
        int hours = seconds / 3600;
        // 获取分钟数
        int minutes = (seconds % 3600) / 60;
        // 获取剩余的秒数
        int remainingSeconds = seconds % 60;
        content.append(String.format("%02ds", remainingSeconds));
        if (minutes != 0) {
            content.insert(0, String.format("%02dm", minutes));
        }
        if (hours != 0) {
            content.insert(0, String.format("%02dh", hours));
        }
        return content.toString();
    }
}