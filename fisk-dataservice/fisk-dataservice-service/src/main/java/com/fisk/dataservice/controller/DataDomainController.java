package com.fisk.dataservice.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.SlicerDTO;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.DataDoFieldDTO;
import com.fisk.dataservice.service.BuildSqlService;
import com.fisk.dataservice.service.DataDomainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/23 16:33
 */
@Api(tags = {SwaggerConfig.TAG_4})
@RestController
@RequestMapping("/DataDomain")
public class DataDomainController {

    @Resource
    private DataDomainService domainService;
    @Resource
    private BuildSqlService buildSqlService;

    @ApiOperation("拼接sql")
    @PostMapping("/getAll")
    public Object listData(@RequestBody List<DataDoFieldDTO> apiConfigureFieldList, Integer currentPage, Integer pageSize) {
        return domainService.query(apiConfigureFieldList,currentPage,pageSize);
    }

    @ApiOperation("获取切片器数据")
    @PostMapping("/getSLICER")
    public ResultEntity<Object> getSlicer(@RequestBody SlicerDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, domainService.getSlicer(dto));
    }

    @ApiOperation("拼接sql白泽数据源")
    @PostMapping("/getSqlSource")
    public Object getSqlSource(@RequestBody List<DataDoFieldDTO> apiConfigureFieldList) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, buildSqlService.query(apiConfigureFieldList));
    }
}
