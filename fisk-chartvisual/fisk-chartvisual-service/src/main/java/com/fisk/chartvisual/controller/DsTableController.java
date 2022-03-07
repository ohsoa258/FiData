package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.dto.DsTableDTO;
import com.fisk.chartvisual.service.DsTableService;
import com.fisk.common.response.ResultEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 可视化数据源优化
 * @author WangYan
 * @date 2022/3/4 11:23
 */
@RequestMapping("/ds")
@RestController
public class DsTableController {

    @Resource
    DsTableService service;

    @ApiOperation("获取表结构信息")
    @GetMapping("/getTableInfo")
    @ResponseBody
    public ResultEntity<DsTableDTO> getTableInfo(Integer id) {
        return service.getTableInfo(id);
    }
}
