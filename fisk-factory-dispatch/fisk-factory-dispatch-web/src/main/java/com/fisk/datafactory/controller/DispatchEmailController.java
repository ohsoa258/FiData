package com.fisk.datafactory.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.config.SwaggerConfig;
import com.fisk.datafactory.dto.customworkflow.DispatchEmailDTO;
import com.fisk.datafactory.service.IDispatchEmail;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author cfk
 */
@Api(tags = {SwaggerConfig.DispatchEmail})
@RestController
@RequestMapping("/DispatchEmail")
public class DispatchEmailController {
    @Resource
    IDispatchEmail iDispatchEmail;


    /**
     * 回显查询单个
     *
     * @param nifiCustomWorkflowId
     * @return
     */
    @GetMapping("/getDispatchEmail")
    @ApiOperation(value = "获取单个管道邮件配置")
    public ResultEntity<Object> getDrive(@RequestParam("nifiCustomWorkflowId") int nifiCustomWorkflowId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iDispatchEmail.getDispatchEmail(nifiCustomWorkflowId));
    }


    /**
     * 保存/修改
     *
     * @param dispatchEmail
     * @return
     */
    @PostMapping("/saveOrupdateDispatchEmail")
    @ApiOperation(value = "新增或修改任务数据源配置")
    public ResultEntity<Object> saveOrupdateDispatchEmail(@RequestBody DispatchEmailDTO dispatchEmail) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iDispatchEmail.saveOrupdate(dispatchEmail));
    }

    /**
     * 删除
     *
     * @param dispatchEmail
     * @return
     */
    @DeleteMapping("/deleteDispatchEmail")
    @ApiOperation(value = "删除单个管道组件")
    public ResultEntity<Object> deleteDispatchEmail(@RequestBody DispatchEmailDTO dispatchEmail) {
        return ResultEntityBuild.build(iDispatchEmail.deleteDispatchEmail(dispatchEmail));
    }


    /**
     * 调用邮件服务器发邮件的方法
     *
     * @param dispatchEmail
     * @return
     */
    @PostMapping("/pipelineSendEmails")
    @ApiOperation(value = "管道异常发邮件")
    public ResultEntity<Object> pipelineSendEmails(@RequestBody DispatchEmailDTO dispatchEmail) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iDispatchEmail.pipelineSendEmails(dispatchEmail));
    }


}
