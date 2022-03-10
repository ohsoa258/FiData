package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.dto.*;
import com.fisk.chartvisual.service.DsTableService;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 可视化数据源优化
 * @author WangYan
 * @date 2022/3/4 11:23
 */
@RequestMapping("/ds")
@RestController
public class DsTableController {

    @Resource
    DsTableService service;

    @ApiOperation("获取表结构信息")
    @GetMapping("/getTableInfo")
    @ResponseBody
    public ResultEntity<DsTableDTO> getTableInfo(Integer id) {
        return service.getTableInfo(id);
    }

    @ApiOperation("获取预览表数据")
    @PostMapping("/getData")
    @ResponseBody
    public ResultEntity<List<Map<String, Object>>> getData(@Validated @RequestBody ObtainTableDataDTO dto) {
        return service.getData(dto);
    }

    @ApiOperation("获取表字段结构")
    @PostMapping("/getTableStructure")
    @ResponseBody
    public ResultEntity<List<FieldInfoDTO>> getTableStructure(@Validated @RequestBody TableStructureDTO dto) {
        return service.getTableStructure(dto);
    }

    @ApiOperation("保存表信息")
    @PostMapping("/saveTableInfo")
    @ResponseBody
    public ResultEntity<ResultEnum> saveTableInfo(@Validated @RequestBody SaveDsTableDTO dto) {
        return service.saveTableInfo(dto);
    }

    @ApiOperation("根据数据源id查询表字段")
    @GetMapping("/selectByDataSourceId")
    @ResponseBody
    public ResultEntity<List<SaveDsTableDTO>> selectByDataSourceId(Integer id) {
        return service.selectByDataSourceId(id);
    }
}
