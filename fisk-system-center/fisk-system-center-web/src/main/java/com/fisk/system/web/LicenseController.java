package com.fisk.system.web;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.dto.license.LicenceDTO;
import com.fisk.system.service.ILicenseService;
import com.fisk.system.vo.license.QueryLicenceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author dick
 * @version 1.0
 * @description 许可证
 * @date 2022/3/22 16:15
 */
@Api(tags = {SwaggerConfig.LICENSE_CONTROLLER})
@RestController
@RequestMapping("/license")
public class LicenseController {

    @Resource
    private ILicenseService service;

    @ApiOperation("查询许可证（Company）")
    @GetMapping("/getCompanyLicence")
    public ResultEntity<QueryLicenceVO> getCompanyLicence(@PathVariable("keyWord") String keyWord) {
        return service.getCompanyLicence(keyWord);
    }

    @ApiOperation("新增许可证（Company）")
    @PostMapping("/addCompanyLicence")
    public ResultEntity<Object> addCompanyLicence(@RequestBody LicenceDTO dto) {
        return ResultEntityBuild.build(service.addCompanyLicence(dto));
    }

    @ApiOperation("编辑许可证（Company）")
    @PutMapping("/editCompanyLicence")
    public ResultEntity<Object> editCompanyLicence(@RequestBody LicenceDTO dto) {
        return ResultEntityBuild.build(service.editCompanyLicence(dto));
    }

    @ApiOperation("删除许可证（Company）")
    @DeleteMapping("/deleteCompanyLicence/{id}")
    public ResultEntity<Object> deleteCompanyLicence(@PathVariable("id") int id) {
        return ResultEntityBuild.build(service.deleteCompanyLicence(id));
    }
}
