package com.fisk.license.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.license.config.SwaggerConfig;
import com.fisk.license.dto.AddLicenceDTO;
import com.fisk.license.dto.AuthorizeLicenceDTO;
import com.fisk.license.dto.VerifyLicenceDTO;
import com.fisk.license.service.ILicenseService;
import com.fisk.license.vo.LicenceVO;
import com.fisk.license.vo.VerifyLicenceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
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

    /*
     * 扩展逻辑
     * 访问模式 新增表字段 LicenceType,UserInfo(名称、手机号、姓名)
     *         Licence的Mac使用客户端IP
     * */

    @Resource
    private ILicenseService service;

//    @ApiOperation("生成许可证（Company）")
//    @PostMapping("/createCompanyLicence")
//    public ResultEntity<String> createCompanyLicence(@Validated @RequestBody AddLicenceDTO dto) {
//        return service.createCompanyLicence(dto);
//    }

    @ApiOperation("设置许可证（Company）")
    @PostMapping("/setCompanyLicence")
    public ResultEntity<String> setCompanyLicence(@Validated @RequestBody AuthorizeLicenceDTO dto) {
        return service.setCompanyLicence(dto);
    }

    @ApiOperation("查询许可证（Company）")
    @GetMapping("/getCompanyLicence")
    public ResultEntity<LicenceVO> getCompanyLicence() {
        return service.getCompanyLicence();
    }

    @ApiOperation("验证访问权限（Company）")
    @PostMapping("/verifyCompanyLicenceByUrl")
    public ResultEntity<VerifyLicenceVO> verifyCompanyLicenceByUrl(@Validated @RequestBody VerifyLicenceDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.verifyCompanyLicenceByUrl(dto));
    }

    @ApiOperation("获取计算机Mac地址")
    @GetMapping("/getMacAddress")
    public ResultEntity<String> getMacAddress() {
        return service.getMacAddress();
    }

}
