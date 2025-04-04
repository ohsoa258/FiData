package com.fisk.dataaccess.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.oraclecdc.CdcHeadConfigDTO;
import com.fisk.dataaccess.dto.table.AccessSyncDataDTO;
import com.fisk.dataaccess.dto.table.TableAccessDTO;
import com.fisk.dataaccess.dto.table.TableAccessNonDTO;
import com.fisk.dataaccess.dto.table.TableKeepNumberDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessQueryDTO;
import com.fisk.dataaccess.service.ITableAccess;
import com.fisk.dataaccess.vo.table.PhyTblAndApiTblVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.TAG_6})
@RestController
@RequestMapping("/v3/tableAccess")
public class TableAccessController {

    @Resource
    ITableAccess service;

    @PostMapping("/add")
    @ApiOperation(value = "添加")
    public ResultEntity<Object> addTableAccessData(@RequestBody TbTableAccessDTO dto) {

        return service.addTableAccessData(dto);
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显")
    public ResultEntity<TbTableAccessDTO> getTableAccessData(
            @PathVariable("id") long id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableAccessData(id));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "修改物理表信息&保存sql_script(ftp信息)")
    public ResultEntity<Object> editData(@RequestBody TbTableAccessDTO dto) {
        return ResultEntityBuild.build(service.updateTableAccessData(dto));
    }

    /**
     * hudi入仓配置 修改表的cdc状态
     * @param dto
     * @return
     */
    @PutMapping("/editHudiConfigCdc")
    @ApiOperation(value = "hudi入仓配置 修改表的cdc状态")
    public ResultEntity<Object> editHudiConfigCdc(@RequestBody TbTableAccessDTO dto) {
        return ResultEntityBuild.build(service.editHudiConfigCdc(dto));
    }

    @PostMapping("/getList")
    @ApiOperation(value = "根据appId获取物理表列表")
    public ResultEntity<Page<TbTableAccessDTO>> getTableAccessListData(@RequestBody TbTableAccessQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableAccessListData(dto));
    }

    /**
     * 获取所有物理表
     *
     * @return
     */
    @PostMapping("/getAllAccessTbls")
    @ApiOperation(value = "获取所有物理表")
    public ResultEntity<List<TableAccessDTO>> getAllAccessTbls() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAllAccessTbls());
    }

    @PostMapping("/getFieldList")
    @ApiOperation(value = "获取最新版sql脚本的表字段集合")
    public ResultEntity<Object> getFieldList(@RequestBody TableAccessNonDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFieldList(dto));
    }

    @ApiOperation("oracle-cdc脚本头配置")
    @PutMapping("/cdcHeadConfig")
    public ResultEntity<Object> buildFiDataTableMetaData(@RequestBody CdcHeadConfigDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.cdcHeadConfig(dto));
    }

    @GetMapping("/getUseExistTable")
    @ApiOperation(value = "获取现有表")
    public ResultEntity<Object> getUseExistTable() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getUseExistTable());
    }

    @PostMapping("/setKeepNumber")
    @ApiOperation(value = "设置stg数据保留天数")
    public ResultEntity<Object> setKeepNumber(@RequestBody TableKeepNumberDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.setKeepNumber(dto));
    }

    /**
     * 通过表名（带架构）获取表信息
     *
     * @param tableName
     * @return
     */
    @GetMapping("/getAccessTableByTableName")
    @ApiOperation(value = "通过表名（带架构）获取表信息")
    public ResultEntity<TableAccessDTO> getAccessTableByTableName(@RequestParam("tableName") String tableName) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAccessTableByTableName(tableName));
    }

    /**
     * 通过应用id获取所选应用下的所有表--仅供智能发布调用
     *
     * @param appId
     * @return
     */
    @GetMapping("/getTblByAppId")
    @ApiOperation(value = "通过应用id获取所选应用下的所有表--仅供智能发布调用")
    public ResultEntity<List<TableAccessDTO>> getTblByAppId(@RequestParam("appId") Integer appId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTblByAppIdForSmart(appId));
    }

    /**
     * 数接--回显统计当前数据接入总共有多少非实时表和实时api
     * 根据应用类型区分
     * 应用类型    (0:实时应用  1:非实时应用 2:CDC)
     *
     * @param appType 应用类型    (0:实时应用  1:非实时应用 2:CDC)
     * @return
     */
    @GetMapping("/countTbl")
    @ApiOperation(value = "数接--回显统计当前数据接入总共有多少非实时表和实时api")
    public ResultEntity<PhyTblAndApiTblVO> countTbl(@RequestParam("appType")Integer appType) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.countTbl(appType));
    }

    /**
     * 数据接入 同步表数据（触发nifi）
     * @param dto
     * @return
     */
    @PostMapping("/accessSyncData")
    @ApiOperation(value = "数据接入 同步表数据（触发nifi）")
    public ResultEntity<Object> accessSyncData(@RequestBody AccessSyncDataDTO dto) {
        return service.accessSyncData(dto);
    }

}
