package com.fisk.mdm.controller;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.dbBEBuild.datamodel.dto.RelationDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.access.*;
import com.fisk.mdm.dto.accessmodel.AccessPublishStatusDTO;
import com.fisk.mdm.service.AccessDataService;
import com.fisk.task.dto.accessmdm.AccessAttributeDTO;
import com.fisk.task.dto.mdmconfig.AccessMdmConfigDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = SwaggerConfig.TAG_14)
@RestController
@RequestMapping("/access")
public class AccessDataController {
    @Autowired
    private AccessDataService accessDataService;

    @ApiOperation("获取接入字段表列表")
    @GetMapping("/getAccessAttributeList")
    public ResultEntity<Object> getAccessAttributeList(@RequestParam("moudleId")Integer moudleId,@RequestParam("entityId")Integer entityId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, accessDataService.getAccessAttributeList(moudleId,entityId));
    }

    @ApiOperation("获取接入表默认预览sql")
    @GetMapping("/getAccessDefaultSql")
    public ResultEntity<Object> getAccessDefaultSql(@RequestParam("moudleId")Integer moudleId,@RequestParam("entityId")Integer entityId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, accessDataService.getAccessDefaultSql(moudleId,entityId));
    }
    @ApiOperation("修改接入sql脚本")
    @PutMapping("/editAccessSql")
    public ResultEntity<Object> editDimensionSql(@Validated @RequestBody AccessSqlDTO dto) {
        return ResultEntityBuild.build(accessDataService.updateAccessSql(dto));
    }

    @ApiOperation("添加修改接入属性字段")
    @PostMapping("/addAttribute")
    public ResultEntity<Object> addAttribute(@Validated @RequestBody AccessAttributeAddDTO dto)
    {
        return ResultEntityBuild.build(accessDataService.addOrUpdateAccessAttribute(dto));
    }

    @ApiOperation("更新发布状态")
    @PutMapping("/updateAccessPublishState")
    public void updateAccessPublishState( @RequestBody AccessPublishStatusDTO dto) {
        accessDataService.updateAccessPublishState(dto);
    }

    @ApiOperation("获取接入字段映射关系")
    @GetMapping("/getAccessAttributeField")
    public ResultEntity<List<AccessAttributeDTO>> getAccessAttributeField(@RequestParam("accessId")Integer accessId,@RequestParam("entityId") Integer entityId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, accessDataService.getAccessAttributeField(accessId,entityId));
    }

    @PostMapping("/overlayCodePreview")
    @ApiOperation(value = "覆盖方式预览代码")
    public ResultEntity<Object> overlayCodePreview(@RequestBody OverlayCodePreviewAccessDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, accessDataService.mdmOverlayCodePreview(dto));
    }

    @GetMapping("/dataAccessConfig")
    @ApiOperation(value = "数据访问配置")
    public ResultEntity<AccessMdmConfigDTO> dataAccessConfig(
            @RequestParam("entityId") long entityId, @RequestParam("modelId") long modelId) {

        return accessDataService.dataAccessConfig(entityId, modelId);
    }

    @GetMapping("/getTableId")
    @ApiOperation(value = "获取实体表id")
    public ResultEntity<List<ChannelDataDTO>> getTableId(){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,accessDataService.getTableId());
    }

    @ApiOperation("获取表发布历史列表")
    @GetMapping("/getTableHistoryList")
    public ResultEntity<Object> getTableHistoryList(@RequestParam("tableId") Integer tableId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, accessDataService.getTableHistoryList(tableId));
    }

    @GetMapping("/getEntityTable")
    @ApiOperation(value = "获取实体表列表")
    public ResultEntity<List<EntityTableDTO>> getEntityTable(@RequestParam("modelId") long modelId){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,accessDataService.getEntityTable(modelId));
    }

    @GetMapping("/getEntityStgTable")
    @ApiOperation(value = "获取当前stg表")
    public ResultEntity<EntityTableDTO> getEntityStgTable(@RequestParam("entityId") long entityId, @RequestParam("modelId") long modelId){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,accessDataService.getEntityStgTable(entityId,modelId));
    }

    @PostMapping("/buildDomainUpdateScript")
    @ApiOperation(value = "构建基于域更新脚本预览")
    public ResultEntity<Object> buildDomainUpdateScript(@RequestBody List<RelationDTO> list) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, accessDataService.buildDomainUpdateScript(list));
    }
}
