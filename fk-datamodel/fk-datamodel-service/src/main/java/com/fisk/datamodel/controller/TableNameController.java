package com.fisk.datamodel.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.service.ITableName;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@RestController
@RequestMapping("/tableName")
public class TableNameController {

    @Resource
    private ITableName service;

    @GetMapping("/get/{id}")
    public ResultEntity<String> getTableName(@PathVariable("id") Integer id, DataDoFieldTypeEnum type) {

        return service.getTableName(id,type);
    }

}
