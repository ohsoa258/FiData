package com.fisk.datagovernance.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.service.dataquality.IDataQualityClientManageService;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量服务接口API
 * @date 2022/3/22 16:17
 */
@Api(tags = {SwaggerConfig.DATA_QUALITY_CLIENT_CONTROLLER})
@RestController
@RequestMapping("/dataQualityClient")
public class DataQualityClientController {

    @Resource
    IDataQualityClientManageService service;

    /**
     * 查询数据质量表规则（含字段规则）
     *
     * @return 查询结果
     */
    @ApiOperation("查询数据质量表规则（含字段规则）")
    @GetMapping("/getTableRuleList")
    public  ResultEntity<TableRuleInfoDTO> getTableRuleList(@RequestParam("dataSourceId") int dataSourceId, @RequestParam("tableName") String tableName) {
        return service.getTableRuleList(dataSourceId,tableName);
    }

    /**
     * 查询数据质量所有数据源信息，含FiData系统数据源
     *
     * @return 查询结果
     */
    @ApiOperation("查询数据质量所有数据源信息，含FiData系统数据源")
    @GetMapping("/getAllDataSource")
    public  ResultEntity<List<DataSourceConVO>> getAllDataSource() {
        return service.getAllDataSource();
    }
}
