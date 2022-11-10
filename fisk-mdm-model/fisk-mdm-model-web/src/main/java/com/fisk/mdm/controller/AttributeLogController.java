package com.fisk.mdm.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.attributelog.AttributeLogDTO;
import com.fisk.mdm.dto.attributelog.AttributeLogSaveDTO;
import com.fisk.mdm.dto.attributelog.AttributeLogUpdateDTO;
import com.fisk.mdm.dto.attributelog.AttributeRollbackDTO;
import com.fisk.mdm.service.AttributeLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/6/14 14:59
 * @Version 1.0
 */
@Api(tags = {SwaggerConfig.TAG_10})
@RestController
@RequestMapping("/attributeLog")
public class AttributeLogController {

    @Resource
    AttributeLogService logService;

    @ApiOperation("保存属性日志")
    @PostMapping("/addAttributeLog")
    @ResponseBody
    public ResultEntity<ResultEnum> addAttributeLog(@RequestBody AttributeLogSaveDTO dto) {
        return ResultEntityBuild.build(logService.saveAttributeLog(dto));
    }

    @ApiOperation("删除属性日志(根据属性id)")
    @DeleteMapping("/deleteByAttributeId")
    @ResponseBody
    public ResultEntity<ResultEnum> deleteByAttributeId(Integer attributeId) {
        return ResultEntityBuild.build(logService.deleteDataByAttributeId(attributeId));
    }

    @ApiOperation("删除属性日志(根据id)")
    @DeleteMapping("/deleteDataById")
    @ResponseBody
    public ResultEntity<ResultEnum> deleteDataById(Integer id) {
        return ResultEntityBuild.build(logService.deleteData(id));
    }

    @ApiOperation("查询日志数据(根据属性id)")
    @GetMapping("/getDataByAttributeId")
    @ResponseBody
    public ResultEntity<List<AttributeLogDTO>> getDataByAttributeId(Integer attributeId) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,logService.queryDataByAttributeId(attributeId));
    }

    @ApiOperation("回滚数据")
    @PostMapping("/rollbackData")
    @ResponseBody
    public ResultEntity<ResultEnum> rollbackData(@RequestBody AttributeRollbackDTO dto) {
        return ResultEntityBuild.build(logService.rollbackData(dto));
    }

    @ApiOperation("属性日志修改接口")
    @PutMapping("/updateAttributeLog")
    @ResponseBody
    public ResultEntity<ResultEnum> updateAttributeLog(@RequestBody AttributeLogUpdateDTO dto) {
        return ResultEntityBuild.build(logService.updateAttributeLog(dto));
    }
}
