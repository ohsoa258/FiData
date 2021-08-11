package com.fisk.datamodel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedAddDTO;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedDTO;
import com.fisk.datamodel.dto.businessLimited.BusinessLimitedQueryDTO;
import com.fisk.datamodel.service.IBusinessLimited;
import com.fisk.datamodel.service.IBusinessLimitedAttribute;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
/**
 * @author cfk
 */
@Api(tags = {SwaggerConfig.BUSINES_LIMITE})
@RestController
@RequestMapping("/BusinessLimited")
@Slf4j
public class BusinessLimitedController {
    @Resource
    public IBusinessLimited iBusinessLimited;
    @Resource
    public IBusinessLimitedAttribute iBusinessLimitedAttribute;

    @ApiOperation("更新业务限定")
    @PutMapping("/updateBusinessLimitedAttribute")
    public ResultEntity<Object> updateBusinessLimitedAttribute(@Validated @RequestBody BusinessLimitedAddDTO businessLimitedAddDTO) {
        return ResultEntityBuild.build(iBusinessLimitedAttribute.updateBusinessLimitedAttribute(businessLimitedAddDTO));
    }

    @PostMapping("/getBusinessLimitedDTOPage")
    @ApiOperation(value = "获取业务限定数据列表")
    public ResultEntity<Page<BusinessLimitedDTO>> getBusinessLimitedDTOPage(@RequestBody BusinessLimitedQueryDTO businessLimitedQueryDTO) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iBusinessLimited.getBusinessLimitedDTOPage(businessLimitedQueryDTO));

    }

    @GetMapping("/getBusinessLimitedAttribute/{id}")
    @ApiOperation(value = "获取业务限定详情")
    public ResultEntity<BusinessLimitedAddDTO> getBusinessLimitedAttribute(@PathVariable("id") String id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iBusinessLimitedAttribute.getBusinessLimitedAttribute(id));
    }

    @DeleteMapping("/deleteBusinessLimitedById/{id}")
    @ApiOperation(value = "删除业务限定记录")
    public ResultEntity<Object> deleteBusinessLimitedById(@PathVariable("id") String id) {
        return ResultEntityBuild.build(iBusinessLimited.deleteBusinessLimitedById(id));
    }


}
