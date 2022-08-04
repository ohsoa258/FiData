package com.fisk.datagovernance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeDTO;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeEditDTO;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeQueryDTO;
import com.fisk.datagovernance.service.dataquality.INoticeManageService;
import com.fisk.datagovernance.vo.dataquality.notice.NoticeDetailVO;
import com.fisk.datagovernance.vo.dataquality.notice.NoticeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author dick
 * @version 1.0
 * @description 告警通知
 * @date 2022/3/22 16:15
 */
@Api(tags = {SwaggerConfig.NOTICE_CONTROLLER})
@RestController
@RequestMapping("/notice")
public class NoticeController {
    @Resource
    private INoticeManageService service;

    @ApiOperation("分页查询告警通知模板组件")
    @PostMapping("/page")
    public ResultEntity<Page<NoticeVO>> getAll(@RequestBody NoticeQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(dto));
    }

    @ApiOperation("添加告警通知模板组件")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody NoticeDTO dto) {
        return ResultEntityBuild.build(service.addData(dto));
    }

    @ApiOperation("编辑告警通知模板组件")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody NoticeEditDTO dto) {
        return ResultEntityBuild.build(service.editData(dto));
    }

    @ApiOperation("删除告警通知模板组件")
    @DeleteMapping("/delete/{id}")
    public ResultEntity<Object> deleteData(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteData(id));
    }

    @ApiOperation("测试发送邮件通知")
    @PostMapping("/testSend")
    public ResultEntity<Object> testSend(@RequestBody NoticeDTO dto) {
        return service.sendEmialNotice(dto);
    }

    @ApiOperation("查询通知下的规则")
    @GetMapping("/getNoticeRuleInfo")
    public ResultEntity<NoticeDetailVO> getNoticeRuleInfo(@PathVariable("id") int id) {
        return service.getNoticeRuleInfo(id);
    }
}
