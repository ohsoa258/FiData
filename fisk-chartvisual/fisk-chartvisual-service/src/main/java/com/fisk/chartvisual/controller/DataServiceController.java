package com.fisk.chartvisual.controller;


import com.fisk.chartvisual.dto.DataSourceConDTO;
import com.fisk.chartvisual.dto.DataSourceConEditDTO;
import com.fisk.chartvisual.service.IDataSourceCon;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/data")
public class DataServiceController {

    @Autowired
    private IDataSourceCon service;

    @GetMapping("/get")
    public ResultEntity<List<DataSourceConVO>> getData() {
        List<DataSourceConVO> data = service.listDataSourceCons();
        return ResultEntityBuild.build(ResultEnum.SUCCESS, data);
    }

    @PostMapping("/add")
    public ResultEntity<Object> addData(@Validated @RequestBody DataSourceConDTO dto) {
        return ResultEntityBuild.build(service.saveDataSourceCon(dto));
    }

    @PutMapping("/edit")
    public ResultEntity<Object> editData(@Validated @RequestBody DataSourceConEditDTO dto) {
        return ResultEntityBuild.build(service.updateDataSourceCon(dto));
    }

    @DeleteMapping("/delete")
    public ResultEntity<Object> deleteData(int id) {
        return ResultEntityBuild.build(service.deleteDataSourceCon(id));
    }

}
