package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import com.fisk.common.service.dbMetaData.dto.ColumnQueryDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.standards.*;
import com.fisk.datamanagement.service.StandardsMenuService;
import com.fisk.datamanagement.service.StandardsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-11-20
 * @Description:
 */
@Api(tags = {SwaggerConfig.STANDARDS})
@RestController
@RequestMapping("/Standards")
public class StandardsController {
    @Resource
    StandardsMenuService standardsMenuService;
    @Resource
    StandardsService standardsService;

    @ApiOperation("查看数据标准树形标签")
    @GetMapping("/getStandardsTree")
    public ResultEntity<List<StandardsTreeDTO>> getStandardsTree() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsMenuService.getStandardsTree());
    }

    @ApiOperation("添加或修改数据标准标签")
    @PostMapping("/addStandardsMenu")
    public ResultEntity<Object> addorUpdateStandardsMenu(@RequestBody StandardsMenuDTO dto) {
        return ResultEntityBuild.build(standardsMenuService.addorUpdateStandardsMenu(dto));
    }

    @ApiOperation("删除数据标准标签")
    @PostMapping("/delStandardsMenu")
    public ResultEntity<Object> delStandardsMenu(@RequestBody List<Integer> ids) {
        return ResultEntityBuild.build(standardsMenuService.delStandardsMenu(ids));
    }

    @ApiOperation("获取数据标准")
    @GetMapping("/getStandards/{id}")
    public ResultEntity<StandardsDTO> getStandards(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.getStandards(id));
    }

    @ApiOperation("添加数据标准")
    @PostMapping("/addStandards")
    public ResultEntity<Object> addStandards(@RequestBody StandardsDTO dto) {
        return ResultEntityBuild.build(standardsService.addStandards(dto));
    }

    @ApiOperation("修改数据标准")
    @PostMapping("/updateStandards")
    public ResultEntity<Object> updateStandards(@RequestBody StandardsDTO dto) {
        return ResultEntityBuild.build(standardsService.updateStandards(dto));
    }

    @ApiOperation("删除数据标准")
    @DeleteMapping("/delStandards/{id}")
    public ResultEntity<Object> delStandards(@PathVariable("id") int id) {
        return ResultEntityBuild.build(standardsService.delStandards(id));
    }

    @ApiOperation("获取表字段信息")
    @PostMapping("/getColumn")
    public ResultEntity<Object> getColumn(@RequestBody ColumnQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.getColumn(dto));
    }

    @ApiOperation("查看数据源结构树")
    @PostMapping("/getDataSourceTree")
    public ResultEntity<List<DataSourceInfoDTO>> getDataSourceTree() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.getDataSourceTree());
    }

    @ApiOperation("预览数据详情")
    @PostMapping("/preview")
    public ResultEntity<QueryResultDTO> preview(@RequestBody QueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.preview(dto));
    }

    @ApiOperation("导出数据标准")
    @PostMapping("/exportStandards")
    @ControllerAOPConfig(printParams = false)
    public void exportStandards(@RequestBody List<Integer> ids, HttpServletResponse response) {
        standardsService.exportStandards(ids, response);
    }

    @ApiOperation("数据标准排序更新")
    @PostMapping("/standardsSort")
    public ResultEntity<Object> standardsSort(@RequestBody StandardsSortDTO dto) {
        return ResultEntityBuild.build(standardsService.standardsSort(dto));
    }

    @ApiOperation("数据标准分页查询数据元")
    @PostMapping("/standardsQuery")
    public ResultEntity<Object> standardsQuery(@RequestBody StandardsQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.standardsQuery(dto));
    }
    @ApiOperation("根据数据源信息查询数据标准基本属性")
    @GetMapping("/getStandardsBySource")
    public ResultEntity<List<StandardsDTO>> getStandardsBySource(Integer fieldMetadataId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.getStandardsBySource(fieldMetadataId));
    }
    @ApiOperation("数据元导入基本属性")
    @PostMapping("/importExcelStandards")
    @ResponseBody
    @ControllerAOPConfig(printParams = false)
    public ResultEntity<Object> importExcelStandards(long menuId, @RequestParam("file") MultipartFile file){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,standardsService.importExcelStandards(menuId,file));
    }
    @ApiOperation("获取所有数据标准树形结构(数据校验用)")
    @PostMapping("/getAllStandardsTree")
    public ResultEntity<Object> getAllStandardsTree(@RequestParam("id") String id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,standardsService.getAllStandardsTree(id));
    }
}
