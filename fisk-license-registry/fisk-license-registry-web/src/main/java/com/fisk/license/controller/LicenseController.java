package com.fisk.license.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.license.config.SwaggerConfig;
import com.fisk.license.dto.LicenceDTO;
import com.fisk.license.dto.VerifyLicenceDTO;
import com.fisk.license.service.ILicenseService;
import com.fisk.license.vo.LicenceVO;
import com.fisk.license.vo.VerifyLicenceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation("校验Url是否有访问权限")
    @PostMapping("/verifyLicenceByUrl")
    public ResultEntity<VerifyLicenceVO> verifyLicenceByUrl(@RequestBody VerifyLicenceDTO dto) {
        return service.verifyLicenceByUrl(dto);
    }

    @ApiOperation("生成许可证")
    @PostMapping("/createLicence")
    public ResultEntity<LicenceVO> createLicence(@RequestBody LicenceDTO dto) {
        return service.createLicence(dto);
    }
}
