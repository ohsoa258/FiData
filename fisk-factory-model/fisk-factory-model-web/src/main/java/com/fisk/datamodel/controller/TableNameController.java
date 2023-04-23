package com.fisk.datamodel.controller;

import com.fisk.chartvisual.dto.chartvisual.IndicatorDTO;
import com.fisk.chartvisual.dto.chartvisual.IndicatorFeignDTO;
import com.fisk.chartvisual.dto.chartvisual.TableDataDTO;
import com.fisk.chartvisual.enums.DataDoFieldTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.service.ITableName;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
@Api(tags = { SwaggerConfig.TableName})
@RestController
@RequestMapping("/tableName")
public class TableNameController {

    @Resource
    private ITableName service;

    @GetMapping("/get")
    public ResultEntity<TableDataDTO> getTableName(
            @RequestParam("id") Integer id,
            @RequestParam("type")DataDoFieldTypeEnum type,
            @RequestParam("field")String field) {

        return service.getTableName(id,type,field);
    }

    @GetMapping("/getAggregation")
    public ResultEntity<String> getAggregation(@RequestParam("id") Integer id) {

        return service.getAggregation(id);
    }

    @PostMapping("/getIndicatorsLogic")
    public ResultEntity<List<IndicatorDTO>> getIndicatorsLogic(@RequestBody IndicatorFeignDTO dto){

        return service.getIndicatorsLogic(dto);
    }
}
