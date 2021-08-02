package com.fisk.dataservice.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.ConfigureUserDTO;
import com.fisk.dataservice.service.ApiFieldService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author WangYan
 * @date 2021/7/13 13:26
 */
@RestController
@RequestMapping("/api")
public class ApiFieldController {

    @Resource
    private ApiFieldService configureFieldService;

    @ApiOperation("根据路径查询")
    @RequestMapping("/query/{apiRoute}")
    public ResultEntity<List<Map>> queryData(@PathVariable("apiRoute") String apiRoute,
                                             Integer currentPage, Integer pageSize,
                                             @RequestBody ConfigureUserDTO user) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,configureFieldService.queryField(apiRoute,currentPage,pageSize,user));
    }
}
