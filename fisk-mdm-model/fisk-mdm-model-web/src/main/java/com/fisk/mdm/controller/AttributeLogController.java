package com.fisk.mdm.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.attributelog.AttributeLogDTO;
import com.fisk.mdm.dto.attributelog.AttributeLogSaveDTO;
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
@Api(tags = {SwaggerConfig.TAG_3})
@RestController
@RequestMapping("/attributeLog")
public class AttributeLogController {

    @Resource
    AttributeLogService logService;

    @ApiOperation("视图组新增属性")
    @PostMapping("/addAttributelog")
    @ResponseBody
    public ResultEntity<ResultEnum> addAttribute(@RequestBody AttributeLogSaveDTO dto) {
        return ResultEntityBuild.build(logService.saveAttributeLog(dto));
    }

    @ApiOperation("删除属性日志(根据属性id)")
    @DeleteMapping("/deleteAttributelog")
    @ResponseBody
    public ResultEntity<ResultEnum> deleteAttribute(Integer attributeId) {
        return ResultEntityBuild.build(logService.deleteDataByAttributeId(attributeId));
    }

    @ApiOperation("删除属性日志(根据id)")
    @DeleteMapping("/deleteData")
    @ResponseBody
    public ResultEntity<ResultEnum> deleteData(Integer id) {
        return ResultEntityBuild.build(logService.deleteData(id));
    }

    @ApiOperation("根据id查询视图组")
    @GetMapping("/getDataByGroupId")
    @ResponseBody
    public ResultEntity<List<AttributeLogDTO>> getDataByGroupId(Integer attributeId) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,logService.queryDataByAttributeId(attributeId));
    }
}
