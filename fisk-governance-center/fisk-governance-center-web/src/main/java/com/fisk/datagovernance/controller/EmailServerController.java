package com.fisk.datagovernance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.dataquality.emailserver.EmailServerDTO;
import com.fisk.datagovernance.dto.dataquality.emailserver.EmailServerEditDTO;
import com.fisk.datagovernance.dto.dataquality.emailserver.EmailServerQueryDTO;
import com.fisk.datagovernance.service.dataquality.IEmailServerManageService;
import com.fisk.datagovernance.vo.dataquality.emailserver.EmailServerVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 邮件服务器
 * @date 2022/3/22 16:16
 */
@Api(tags = {SwaggerConfig.TAG_4})
@RestController
@RequestMapping("/emailserver")
public class EmailServerController {
    @Resource
    private IEmailServerManageService service;

    @ApiOperation("分页查询邮件服务器列表")
    @PostMapping("/page")
    public ResultEntity<Page<EmailServerVO>> getAll(@RequestBody EmailServerQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(dto));
    }

    @ApiOperation("添加邮件服务器")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody EmailServerDTO dto) {
        return ResultEntityBuild.build(service.addData(dto));
    }

    @ApiOperation("编辑邮件服务器")
    @PutMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody EmailServerEditDTO dto) {
        return ResultEntityBuild.build(service.editData(dto));
    }

    @ApiOperation("删除邮件服务器")
    @DeleteMapping("/delete/{id}")
    public ResultEntity<Object> deleteData(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteData(id));
    }
}
