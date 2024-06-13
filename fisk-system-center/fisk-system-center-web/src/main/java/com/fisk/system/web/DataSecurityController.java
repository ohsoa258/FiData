package com.fisk.system.web;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.dto.datasecurity.DataSecurityColumnsDTO;
import com.fisk.system.dto.datasecurity.DataSecurityRowsDTO;
import com.fisk.system.dto.datasecurity.DataSecurityTablesDTO;
import com.fisk.system.service.DataSecurityColumnsPOService;
import com.fisk.system.service.DataSecurityRowsPOService;
import com.fisk.system.service.DataSecurityTablesPOService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = {SwaggerConfig.DATA_SECURITY})
@RestController
@RequestMapping("/dataSecurity")
public class DataSecurityController {

    @Resource
    private DataSecurityTablesPOService tableService;

    @Resource
    private DataSecurityColumnsPOService columnService;

    @Resource
    private DataSecurityRowsPOService rowService;

    /**
     * 获取所有应用以及表、字段数据
     *
     * @return
     */
    @ApiOperation("获取所有应用以及表、字段数据")
    @GetMapping("/getAccessAppDetails")
    public ResultEntity<Object> getAccessAppDetails() {
        return tableService.getAccessAppDetails();
    }

    /**
     * 数据安全 表级安全 批量保存
     *
     * @param dtoList
     * @return
     */
    @ApiOperation("数据安全 表级安全 批量保存")
    @PostMapping("/saveTables")
    public ResultEntity<Object> saveTables(@RequestBody List<DataSecurityTablesDTO> dtoList) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableService.saveTables(dtoList));
    }

    /**
     * 数据安全 表级安全 回显
     *
     * @return
     */
    @ApiOperation("数据安全 表级安全 回显")
    @GetMapping("/getTables")
    public ResultEntity<List<DataSecurityTablesDTO>> getTables() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableService.getTables());
    }

    /**
     * 数据安全 表级安全 根据角色id获取该角色的表级安全权限
     *
     * @return
     */
    @ApiOperation("数据安全 表级安全 根据角色id获取该角色的表级安全权限")
    @GetMapping("/getTablesByRoleId")
    public ResultEntity<List<DataSecurityTablesDTO>> getTablesByRoleId(@RequestParam("roleId") Integer roleId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableService.getTablesByRoleId(roleId));
    }

    /**
     * 数据安全 列级安全 根据角色id获取该角色的列级安全权限
     *
     * @return
     */
    @ApiOperation("数据安全 列级安全 根据角色id获取该角色的列级安全权限")
    @GetMapping("/getColumnsByRoleId")
    public ResultEntity<List<DataSecurityColumnsDTO>> getColumnsByRoleId(@RequestParam("roleId") Integer roleId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, columnService.getColumnsByRoleId(roleId));
    }

    /**
     * 数据安全 行级安全 根据角色id获取该角色的行级安全权限
     *
     * @return
     */
    @ApiOperation("数据安全 行级安全 根据角色id获取该角色的行级安全权限")
    @GetMapping("/getRowsByRoleId")
    public ResultEntity<List<DataSecurityRowsDTO>> getRowsByRoleId(@RequestParam("roleId") Integer roleId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, rowService.getRowsByRoleId(roleId));
    }

    /**
     * 数据安全 表级安全 单个删除
     *
     * @return
     */
    @ApiOperation("数据安全 表级安全 单个删除")
    @DeleteMapping("/deleteTableSecurityById")
    public ResultEntity<Object> deleteTableSecurityById(@RequestParam("id") Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, tableService.deleteTableSecurityById(id));
    }

    /**
     * 数据安全 行级安全 批量保存
     *
     * @param dtoList
     * @return
     */
    @ApiOperation("数据安全 行级安全 批量保存")
    @PostMapping("/saveRows")
    public ResultEntity<Object> saveRows(@RequestBody List<DataSecurityRowsDTO> dtoList) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, rowService.saveRows(dtoList));
    }

    /**
     * 数据安全 行级安全 回显
     *
     * @return
     */
    @ApiOperation("数据安全 行级安全 回显")
    @GetMapping("/getRows")
    public ResultEntity<Object> getRows() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, rowService.getRows());
    }

    /**
     * 数据安全 行级安全 单个删除
     *
     * @return
     */
    @ApiOperation("数据安全 行级安全 单个删除")
    @DeleteMapping("/deleteRowSecurityById")
    public ResultEntity<Object> deleteRowSecurityById(@RequestParam("id") Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, rowService.deleteRowSecurityById(id));
    }

    /**
     * 数据安全 列级安全 批量保存
     *
     * @param dtoList
     * @return
     */
    @ApiOperation("数据安全 列级安全 批量保存")
    @PostMapping("/saveColumns")
    public ResultEntity<Object> saveColumns(@RequestBody List<DataSecurityColumnsDTO> dtoList) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, columnService.saveColumns(dtoList));
    }

    /**
     * 数据安全 列级安全 回显
     *
     * @return
     */
    @ApiOperation("数据安全 列级安全 回显")
    @GetMapping("/getColumns")
    public ResultEntity<List<DataSecurityColumnsDTO>> getColumns() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, columnService.getColumns());
    }

    /**
     * 数据安全 列级安全 单个删除
     *
     * @return
     */
    @ApiOperation("数据安全 列级安全 单个删除")
    @DeleteMapping("/deleteColumnSecurityById")
    public ResultEntity<Object> deleteColumnSecurityById(@RequestParam("id") Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, columnService.deleteColumnSecurityById(id));
    }

}
