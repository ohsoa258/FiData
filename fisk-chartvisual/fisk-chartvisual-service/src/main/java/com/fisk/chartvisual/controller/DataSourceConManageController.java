package com.fisk.chartvisual.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.dto.DataSourceConDTO;
import com.fisk.chartvisual.dto.DataSourceConEditDTO;
import com.fisk.chartvisual.dto.DataSourceConQuery;
import com.fisk.chartvisual.service.IDataSourceCon;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 数据源管理
 * @author gy
 */
@RestController
@RequestMapping("/dscon")
public class DataSourceConManageController {

    @Autowired
    private IDataSourceCon service;

    @GetMapping("/get")
    public ResultEntity<Page<DataSourceConVO>> getData(Page<DataSourceConVO> page, DataSourceConQuery query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listDataSourceCons(page, query));
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
