package com.fisk.datagovernance.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckSyncDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckWebDTO;
import com.fisk.datagovernance.dto.dataquality.rule.QueryRuleDTO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datagovernance.vo.dataquality.rule.TableRuleInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量服务接口
 * @date 2022/4/12 11:31
 */
@FeignClient("datagovernance-service")
public interface DataQualityClient {
    /**
     * 数据校验 界面/接口验证
     *
     * @param dto 请求参数
     * @return 执行结果
     */
    @PostMapping("/datacheck/interfaceCheckData")
    ResultEntity<List<DataCheckResultVO>> interfaceCheckData(@Validated @RequestBody DataCheckWebDTO dto);

    /**
     * 数据校验 同步验证
     *
     * @param dto 请求参数
     * @return AtlasEntityDbTableColumnDTO
     */
    @PostMapping("/datacheck/syncCheckData")
    ResultEntity<List<DataCheckResultVO>> syncCheckData(@Validated @RequestBody DataCheckSyncDTO dto);

    /**
     * 查询数据质量表规则（含字段规则）
     *
     * @param requestDTO 请求DTO
     * @return 查询结果
     */
    @GetMapping("/dataQualityClient/getTableRuleList")
    ResultEntity<TableRuleInfoVO> getTableRuleList(@RequestBody QueryRuleDTO requestDTO);

    /**
     * 查询数据质量所有数据源信息，含FiData系统数据源
     *
     * @return 查询结果
     */
    @GetMapping("/dataQualityClient/getAllDataSource")
    ResultEntity<List<DataSourceConVO>> getAllDataSource();
}
