package com.fisk.datafactory.controller;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.Dto.cron.NextCronTimeDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datafactory.dto.customworkflowdetail.DeleteTableDetailDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.customworkflowdetail.WorkflowTaskGroupDTO;
import com.fisk.datafactory.service.INifiCustomWorkflowDetail;
import com.fisk.datafactory.vo.customworkflowdetail.NifiCustomWorkflowDetailVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.NifiCustomWorkListDTO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
@RestController
@RequestMapping("/nifiCustomWorkflowDetail")
@Slf4j
public class NifiCustomWorkflowDetailController {
    @Resource
    INifiCustomWorkflowDetail service;
    @Resource
    PublishTaskClient publishTaskClient;

    @ApiOperation("添加单个管道组件")
    @PostMapping("/add")
    public ResultEntity<NifiCustomWorkflowDetailDTO> addData(@RequestBody NifiCustomWorkflowDetailDTO dto) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, service.addData(dto));
    }

    @ApiOperation("添加管道组件集合")
    @PostMapping("/addList")
    public ResultEntity<List<NifiCustomWorkflowDetailDTO>> addDataList(@RequestBody List<NifiCustomWorkflowDetailDTO> list) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, service.addDataList(list));
    }

    @ApiOperation("修改管道组件集合")
    @PostMapping("/editList")
    public ResultEntity<Object> editDataList(@RequestBody List<NifiCustomWorkflowDetailDTO> list) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, service.editDataList(list));
    }

    @GetMapping("/getComponentList/{id}")
    @ApiOperation(value = "查询当前任务下的组件详情集合")
    public ResultEntity<List<NifiCustomWorkflowDetailDTO>> getComponentList(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getComponentList(id));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "查询单个管道组件")
    public ResultEntity<NifiCustomWorkflowDetailDTO> getData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }


    @PutMapping("/edit")
    @ApiOperation(value = "修改管道详情")
    public ResultEntity<Object> editData(@RequestBody NifiCustomWorkflowDetailVO dto) {
        ResultEntity<NifiCustomWorkListDTO> data = service.editData(dto);
        NifiCustomWorkListDTO workListDTO = data.data;
        if (workListDTO == null) {
            return ResultEntityBuild.buildData(data.code, data.msg);
        }

        // 保存成功且前端发布
        if (data.code == 0 && dto.flag) {
            // TODO: 调用nifi生成流程
            log.info("nifi: 管道开始创建");
            Map<Map, Map> externalStructure = workListDTO.externalStructure;
            Map<Map, Map> structure = workListDTO.structure;
            workListDTO.externalStructure1 = externalStructure.toString();
            workListDTO.structure1 = structure.toString();
            publishTaskClient.publishBuildNifiCustomWorkFlowTask(workListDTO);
            log.info(JSON.toJSONString(workListDTO));
            log.info("nifi: 管道创建成功");
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS, workListDTO);
    }

    @PutMapping("/editComponent")
    @ApiOperation(value = "修改单个管道组件")
    public ResultEntity<Object> editData(@RequestBody NifiCustomWorkflowDetailDTO dto) {

        return ResultEntityBuild.build(service.editWorkflow(dto));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除单个管道组件")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(service.deleteData(id));
    }

    @DeleteMapping("/deleteTaskGroup")
    @ApiOperation(value = "删除单个任务组")
    public ResultEntity<Object> deleteDataList(@RequestBody WorkflowTaskGroupDTO dto) {

        return ResultEntityBuild.build(service.deleteDataList(dto));
    }

    @ApiOperation(value = "根据不同的类型,获取不同tree")
    @PostMapping("/getTableId")
    public ResultEntity<Object> getTableId(@RequestBody NifiComponentsDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableIds(dto));
    }


    @ApiOperation("access or model 删除操作时,task要同步删除这些数据")
    @PostMapping("/editByDeleteTable")
    public ResultEntity<Object> editByDeleteTable(@Validated @RequestBody List<DeleteTableDetailDTO> list) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, service.editDataByDeleteTable(list));
    }

    @GetMapping("/getExternalDataSourceList")
    @ApiOperation(value = "获取外部数据源")
    public ResultEntity<Object> getExternalDataSourceList() {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getExternalDataSourceList());
    }

    @PostMapping("/getNextCronExeTime")
    @ApiOperation(value = "获取cron下次执行时间")
    public ResultEntity<Object> getNextCronExeTime(@Validated @RequestBody NextCronTimeDTO dto) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getNextCronExeTime(dto));
    }

}
