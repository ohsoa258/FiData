package com.fisk.datafactory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.dto.customworkflow.DispatchEmailDTO;
import com.fisk.datafactory.dto.customworkflow.RecipientsDtO;
import com.fisk.datafactory.entity.DispatchEmailPO;

/**
 * @author cfk
 */
public interface IDispatchEmail extends IService<DispatchEmailPO> {

    /**
     * 回显查询单个
     *
     * @param nifiCustomWorkflowId
     * @return
     */
    DispatchEmailDTO getDispatchEmail(int nifiCustomWorkflowId);


    /**
     * 保存/修改
     *
     * @param dispatchEmail
     * @return
     */
    ResultEnum saveOrupdate(DispatchEmailDTO dispatchEmail);


    /**
     * 删除  通过邮件服务器id删,或者管道id删,一般是管道id
     *
     * @param dispatchEmail
     * @return
     */
    ResultEnum deleteDispatchEmail(DispatchEmailDTO dispatchEmail);


    /**
     * 调用邮件服务器发邮件的方法
     *
     * @param dispatchEmail
     * @return
     */
    ResultEnum pipelineSendEmails(DispatchEmailDTO dispatchEmail);

    /*
    * 设置通知
    * */
    ResultEnum setNotification(RecipientsDtO dto);


}
