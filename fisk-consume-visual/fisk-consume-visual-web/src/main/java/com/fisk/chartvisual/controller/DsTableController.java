package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.dto.chartvisual.ObtainTableDataDTO;
import com.fisk.chartvisual.dto.chartvisual.TableInfoDTO;
import com.fisk.chartvisual.dto.dstable.*;
import com.fisk.chartvisual.service.DsTableService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
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
    public ResultEntity<List<DsTableDTO>> getTableInfo(Integer id) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,service.getTableInfo(id));
    }

    @ApiOperation("获取预览表数据")
    @PostMapping("/getData")
    @ResponseBody
    public ResultEntity<List<Map<String, Object>>> getData(@Validated @RequestBody ObtainTableDataDTO dto) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,service.getData(dto));
    }

    @ApiOperation("获取字段结构")
    @PostMapping("/getTableStructure")
    @ResponseBody
    public ResultEntity<List<TableInfoDTO>> getTableStructure(@Validated @RequestBody TableStructureDTO dto) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,service.getTableStructure(dto));
    }

    @ApiOperation("保存或修改表信息")
    @PostMapping("/saveOrUpdateTableInfo")
    @ResponseBody
    public ResultEntity<ResultEnum> saveTableInfo(@Validated @RequestBody List<UpdateDsTableDTO> dtoList) {
        return service.saveTableInfo(dtoList);
    }

    @ApiOperation("根据数据源id查询库里表字段")
    @GetMapping("/selectByDataSourceId")
    @ResponseBody
    public ResultEntity<List<DsFiledDTO>> selectByDataSourceId(Integer id) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,service.selectByDataSourceId(id));
    }

    @ApiOperation("获取表结构信息状态")
    @GetMapping("/getTableInfoStatus")
    @ResponseBody
    public ResultEntity<List<ShowDsTableDTO>> getTableInfoStatus(Integer id) {
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,service.getTableInfoStatus(id));
    }
}
