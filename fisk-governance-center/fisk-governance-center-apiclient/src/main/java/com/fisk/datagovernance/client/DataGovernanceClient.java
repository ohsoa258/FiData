package com.fisk.datagovernance.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckSyncDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckWebDTO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
     * 查询数据质量表规则（含字段规则）
     * tableBusinessType：表业务类型 1：事实表、2：维度表、3、指标表  4、宽表
     *
     * @return 查询结果
     */
    @GetMapping("/dataQualityClient/getTableRuleList")
    ResultEntity<TableRuleInfoDTO> getTableRuleList(@RequestParam("dataSourceId") int dataSourceId, @RequestParam("tableUnique") String tableUnique, @RequestParam("tableBusinessType") int tableBusinessType);

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
}
