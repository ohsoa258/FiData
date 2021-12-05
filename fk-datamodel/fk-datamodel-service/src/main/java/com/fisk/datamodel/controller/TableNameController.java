package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.dataservice.dto.IndicatorDTO;
import com.fisk.dataservice.dto.IndicatorFeignDTO;
import com.fisk.dataservice.dto.TableDataDTO;
import com.fisk.datamodel.service.ITableName;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
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
