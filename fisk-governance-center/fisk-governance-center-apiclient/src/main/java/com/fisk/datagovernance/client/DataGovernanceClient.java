package com.fisk.datagovernance.client;

import com.fisk.common.core.constants.SystemConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataops.TableDataSyncDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckSyncDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckWebDTO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datagovernance.vo.dataquality.external.MetaDataQualityRuleVO;
import com.fisk.datamanagement.dto.standards.StandardsDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据治理服务接口
 * @date 2022/4/12 11:31
 */
@FeignClient("datagovernance-service")
public interface DataGovernanceClient {
    /**
     * 接口验证（同步前）
     *
     * @param dto 请求参数
     * @return 执行结果
     */
    @PostMapping("/datacheck/interfaceCheckData")
    ResultEntity<List<DataCheckResultVO>> interfaceCheckData(@Validated @RequestBody DataCheckWebDTO dto);

    /**
     * 接口验证（同步中）
     *
     * @param dto 请求参数
     * @return 执行结果
     */
    @PostMapping("/datacheck/syncCheckData")
    ResultEntity<List<DataCheckResultVO>> syncCheckData(@Validated @RequestBody DataCheckSyncDTO dto);

    /**
     * 查询数据质量规则（表、字段规则）
     * tableBusinessType：表业务类型 1：事实表、2：维度表、3、指标表  4、宽表
     *
     * @return 查询结果
     */
    @GetMapping("/dataQualityClient/getTableRuleList")
    ResultEntity<List<MetaDataQualityRuleVO>> getTableRuleList(@RequestParam("fiDataSourceId") int fiDataSourceId, @RequestParam("tableUnique") String tableUnique, @RequestParam("tableBusinessType") int tableBusinessType);

    /**
     * 查询数据质量所有数据源信息，含FiData系统数据源
     *
     * @return 查询结果
     */
    @GetMapping("/dataQualityClient/getAllDataSource")
    ResultEntity<List<DataSourceConVO>> getAllDataSource();

    /**
     * 数据检查-生成质量报告
     *
     * @return 操作结果
     */
    @GetMapping("/dataQualityClient/createQualityReport")
    ResultEntity<Object> createQualityReport(@RequestParam("id") int id);

    /**
     * 数据检查-删除数据检查日志
     *
     * @return 操作结果
     */
    @PostMapping("/datacheck/deleteDataCheckLogs")
    ResultEnum deleteDataCheckLogs(@RequestParam("ruleId") long ruleId);

    /**
     * 数据安全-生成智能发现报告
     *
     * @return 操作结果
     */
    @GetMapping("/intelligentdiscovery/createScanReport")
    ResultEntity<Object> createScanReport(@RequestParam("id") int id);

    /**
     * 数据运维，表数据同步
     *
     * @param dto
     * @return
     */
    @PostMapping("/datasource/tableDataSync")
    ResultEntity<Object> tableDataSync(@RequestBody TableDataSyncDTO dto);

    /**
     * 数仓建模-表数据同步
     *
     * @param dto
     * @return
     */
    @PostMapping("/datasource/tableDataSyncForModel")
    ResultEntity<Object> tableDataSyncForModel(@RequestBody TableDataSyncDTO dto);

    /**
     * 获取所有数据校验规则数量
     * @return
     */
    @GetMapping("/datacheck/getDataCheckRoleTotal")
    ResultEntity<Object> getDataCheckRoleTotal();

    @ApiOperation("修改数据校验数据元标准组(数据元更新同步)")
    @PostMapping("/datacheck/editDataCheckByStandards")
    ResultEntity<Object> editDataCheckByStandards(@RequestBody StandardsDTO dto,@RequestHeader(name = SystemConstants.HTTP_HEADER_AUTH) String token);

    @ApiOperation("根据menuId删除数据校验数据元标准组")
    @GetMapping("/datacheck/deleteDataCheckStandardsGroupByMenuId")
    ResultEntity<Object> deleteDataCheckStandardsGroupByMenuId(@RequestParam("menuId") Integer menuId,@RequestHeader(name = SystemConstants.HTTP_HEADER_AUTH) String token);
}
