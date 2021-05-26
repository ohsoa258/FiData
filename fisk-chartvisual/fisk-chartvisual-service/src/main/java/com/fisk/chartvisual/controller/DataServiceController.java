package com.fisk.chartvisual.controller;


import com.fisk.chartvisual.dto.DataSourceConDTO;
import com.fisk.chartvisual.service.IDataSourceCon;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/data")
public class DataServiceController {

    @Autowired
    private IDataSourceCon service;

    @GetMapping("/get")
    public ResultEntity<Object> getData() {
        List<DataSourceConVO> data = service.listDataSourceCons();
        return ResultEntityBuild.build(ResultEnum.SUCCESS, data);
    }

    @PostMapping("/add")
    public ResultEntity<Object> addData(@Validated @RequestBody DataSourceConDTO dto) throws Exception {
        return ResultEntityBuild.build(service.saveDataSourceCon(dto));
    }

}
