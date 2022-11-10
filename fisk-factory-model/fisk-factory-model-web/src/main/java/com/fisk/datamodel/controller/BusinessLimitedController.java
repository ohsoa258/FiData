package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.businesslimited.BusinessLimitedDataAddDTO;
import com.fisk.datamodel.dto.businesslimited.BusinessLimitedDataDTO;
import com.fisk.datamodel.dto.businesslimited.BusinessLimitedUpdateDTO;
import com.fisk.datamodel.dto.businesslimitedattribute.BusinessLimitedAttributeDataDTO;
import com.fisk.datamodel.entity.businesslimited.BusinessLimitedPO;
import com.fisk.datamodel.service.IBusinessLimited;
import com.fisk.datamodel.service.IBusinessLimitedAttribute;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    @DeleteMapping("/deleteBusinessLimitedById/{id}")
    @ApiOperation(value = "删除业务限定记录")
    public ResultEntity<Object> deleteBusinessLimitedById(@PathVariable("id") String id) {
        return ResultEntityBuild.build(iBusinessLimited.deleteBusinessLimitedById(id));
    }

    @GetMapping("/getBusinessLimitedList/{factId}")
    @ApiOperation(value = "获取业务限定下拉数据")
    public ResultEntity<List<BusinessLimitedPO>> getBusinessLimitedList(@PathVariable("factId") String factId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iBusinessLimited.getBusinessLimitedList(factId));
    }

    @GetMapping("/getBusinessLimitedAndAttributeList/{businessLimitedId}")
    @ApiOperation(value = "根获取据业务限定id获取业务限定字段列表以及业务限定详情")
    public ResultEntity<BusinessLimitedDataDTO> getBusinessLimitedAndAttributeList(@PathVariable("businessLimitedId") int businessLimitedId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, iBusinessLimited.getBusinessLimitedAndAttributeList(businessLimitedId));
    }

    @ApiOperation("添加业务限定")
    @PostMapping("/BusinessLimitedAdd")
    public ResultEntity<Object> businessLimitedAdd(@Validated @RequestBody BusinessLimitedDataAddDTO dto) {
        return ResultEntityBuild.build(iBusinessLimited.businessLimitedAdd(dto));
    }

    @ApiOperation("更新业务限定")
    @PutMapping("/businessLimitedUpdate")
    public ResultEntity<Object> businessLimitedUpdate(@Validated @RequestBody BusinessLimitedUpdateDTO dto) {
        return ResultEntityBuild.build(iBusinessLimited.businessLimitedUpdate(dto));
    }

    @ApiOperation("更新业务限定字段")
    @PutMapping("/businessLimitedAttributeUpdate")
    public ResultEntity<Object> businessLimitedAttributeUpdate(@Validated @RequestBody BusinessLimitedAttributeDataDTO dto) {
        return ResultEntityBuild.build(iBusinessLimitedAttribute.updateBusinessLimitedAttribute(dto));
    }

    @DeleteMapping("/deleteBusinessLimitedAttribute/{id}")
    @ApiOperation(value = "删除业务限定字段")
    public ResultEntity<Object> delBusinessLimitedAttribute(@PathVariable("id") int id) {
        return ResultEntityBuild.build(iBusinessLimitedAttribute.delBusinessLimitedAttribute(id));
    }

    @ApiOperation("添加业务限定字段")
    @PostMapping("/addBusinessLimitedAttribute")
    public ResultEntity<Object> addBusinessLimitedAttribute(@Validated @RequestBody BusinessLimitedAttributeDataDTO dto) {
        return ResultEntityBuild.build(iBusinessLimitedAttribute.addBusinessLimitedAttribute(dto));
    }

}
