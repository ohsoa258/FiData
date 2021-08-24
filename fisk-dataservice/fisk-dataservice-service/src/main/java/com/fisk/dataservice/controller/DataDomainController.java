package com.fisk.dataservice.controller;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import com.fisk.dataservice.dto.DataDoFieldDTO;
import com.fisk.dataservice.service.DataDomainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author WangYan
 * @date 2021/8/23 16:33
 */
@Api(tags = {SwaggerConfig.TAG_3})
@RestController
@RequestMapping("/DataDomain")
public class DataDomainController {

    @Resource
    private DataDomainService domainService;

    @ApiOperation("拼接sql")
    @PostMapping("/getAll")
    public ResultEntity<List<Map>> listData(@RequestBody List<DataDoFieldDTO> apiConfigureFieldList, Integer currentPage, Integer pageSize) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, domainService.query(apiConfigureFieldList,currentPage,pageSize));
    }
}
