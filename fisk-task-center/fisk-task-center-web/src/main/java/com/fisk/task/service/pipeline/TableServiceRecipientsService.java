package com.fisk.task.service.pipeline;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.task.dto.tableservice.TableServiceRecipientsDTO;
import com.fisk.task.entity.TableServiceRecipientsPO;
import com.fisk.task.vo.tableservice.TableServiceRecipientsVO;


/**
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2023-07-24 17:29:50
 */
public interface TableServiceRecipientsService extends IService<TableServiceRecipientsPO> {

    /**
     * 查询表服务日志订阅通知配置
     * @return
     */
    TableServiceRecipientsVO getTableServiceAlarmNotice();

    /**
     * 保存表服务日志订阅通知配置
     * @param tableServiceRecipientsDTO
     * @return
     */
    ResultEnum saveTableServiceAlarmNotice(TableServiceRecipientsDTO tableServiceRecipientsDTO);

    /**
     * 删除表服务日志订阅通知配置
     * @return
     */
    ResultEnum deleteTableServiceAlarmNotice();

    /**
     * 发送表服务日志订阅通知
     * @param content
     * @return
     */
    ResultEnum sendTableServiceSendEmails(String content);
}

