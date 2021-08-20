package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.service.ITableName;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@RestController
@RequestMapping("/tableName")
public class TableNameController {

    @Resource
    private ITableName service;

    @GetMapping("/get")
    public ResultEntity<String> getTableName(@RequestParam("id") Integer id, @RequestParam("type")DataDoFieldTypeEnum type) {

        return service.getTableName(id,type);
    }

}
