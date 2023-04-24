package com.fisk.mdm.controller;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.access.AccessAttributeAddDTO;
import com.fisk.mdm.dto.access.AccessSqlDTO;
import com.fisk.mdm.service.AccessDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = SwaggerConfig.TAG_14)
@RestController
@RequestMapping("/access")
public class AccessDataController {
    @Autowired
    private AccessDataService accessDataService;

    @ApiOperation("获取接入字段表列表")
    @GetMapping("/getAccessAttributeList")
    public ResultEntity<Object> getAccessAttributeList(Integer moudleId,Integer entityId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, accessDataService.getAccessAttributeList(moudleId,entityId));
    }

    @ApiOperation("修改接入sql脚本")
    @PutMapping("/editAccessSql")
    public ResultEntity<Object> editDimensionSql(@Validated @RequestBody AccessSqlDTO dto) {
        return ResultEntityBuild.build(accessDataService.updateAccessSql(dto));
    }

    @ApiOperation("添加修改接入属性字段")
    @PostMapping("/addAttribute")
    public ResultEntity<Object> addAttribute(@Validated @RequestBody AccessAttributeAddDTO dto)
    {
        return ResultEntityBuild.build(accessDataService.addOrUpdateAccessAttribute(dto));
    }
}
