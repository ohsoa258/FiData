package com.fisk.dataaccess.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.access.OperateTableDTO;
import com.fisk.dataaccess.dto.access.OverlayCodePreviewAccessDTO;
import com.fisk.dataaccess.dto.table.*;
import com.fisk.dataaccess.service.ITableFields;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.TAG_5})
@RestController
@RequestMapping("/tableFields")
@Slf4j
public class TableFieldsController {
    @Resource
    public ITableFields service;

    @PostMapping("/getTableField")
    @ApiOperation(value = "查询表字段")
    public ResultEntity<TableFieldsDTO> getTableField(@RequestParam("id") int id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableField(id));
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加物理表字段--保存&发布")
    public ResultEntity<Object> addData(@Validated @RequestBody TableAccessNonDTO dto) {
        return ResultEntityBuild.build(service.addData(dto));
    }

    /**
     * 保存&发布
     *
     * @param dto
     * @return
     */
    @PutMapping("/edit")
    @ApiOperation(value = "修改物理表字段--保存&发布")
    public ResultEntity<Object> editData(@Validated @RequestBody TableAccessNonDTO dto) {
        return ResultEntityBuild.build(service.updateData(dto));
    }

    /**
     * 保存&发布  hudi(hive)建立doris外部目录
     *
     * @param dto
     * @return
     */
    @PutMapping("/editForHive")
    @ApiOperation(value = "保存&发布  hudi(hive)建立doris外部目录")
    public ResultEntity<Object> editForHive(@Validated @RequestBody TableAccessNonDTO dto) {
        return ResultEntityBuild.build(service.editForHive(dto));
    }

    @PostMapping("/loadDepend")
    @ApiOperation(value = "对表进行操作时,查询依赖")
    public ResultEntity<Object> loadDepend(@RequestBody OperateTableDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.loadDepend(dto));
    }

    /*
      2023 09-20 李世纪：接口已弃用
     */
    @PostMapping("/previewCoverCondition")
    @ApiOperation(value = "预览业务时间覆盖")
    public ResultEntity<Object> previewCoverCondition(@Validated @RequestBody TableBusinessDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.previewCoverCondition(dto));
    }

    @PostMapping("/delTableVersion")
    @ApiOperation(value = "删除表版本")
    public ResultEntity<Object> delVersionData(@Validated @RequestBody TableVersionDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.delVersionData(dto.getKeyStr()));
    }

    @PostMapping("/batchPublish")
    @ApiOperation(value = "批量发布")
    public ResultEntity<Object> batchPublish(@Validated @RequestBody BatchPublishDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.batchPublish(dto));
    }

    @PostMapping("/addFile")
    @ApiOperation(value = "新增单个字段")
    public ResultEntity<Object> addFile(@Validated @RequestBody TableFieldsDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.addFile(dto));
    }

    @DeleteMapping("/delFile/{id}/{tableId}/{userId}")
    @ApiOperation(value = "删除单个字段&&元数据字段异步删除")
    public ResultEntity<Object> delFile(@PathVariable("id") long id, @PathVariable("tableId") long tableId, @PathVariable("userId") long userId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.delFile(id, tableId, userId));
    }

    @PutMapping("/updateFile")
    @ApiOperation(value = "编辑单个字段")
    public ResultEntity<Object> updateFile(@Validated @RequestBody TableFieldsDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.updateFile(dto));
    }
//
//    @PostMapping("/overlayCodePreview")
//    @ApiOperation(value = "覆盖方式预览代码")
//    public ResultEntity<Object> overlayCodePreviewTest(@RequestBody OverlayCodePreviewAccessDTO dto) {
//        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.overlayCodePreview(dto));
//    }

    @PostMapping("/overlayCodePreview")
    @ApiOperation(value = "覆盖方式预览代码")
    public ResultEntity<Object> overlayCodePreview(@RequestBody OverlayCodePreviewAccessDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.accessOverlayCodePreview(dto));
    }

    /**
     * 根据字段id集合获取字段详情集合
     *
     * @param fieldIds
     * @return
     */
    @GetMapping("/getFieldInfosByIds")
    @ApiOperation(value = "根据字段id集合获取字段详情集合")
    public ResultEntity<List<TableFieldsDTO>> getFieldInfosByIds(@RequestParam("fieldIds") List<Integer> fieldIds) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFieldInfosByIds(fieldIds));
    }

}
