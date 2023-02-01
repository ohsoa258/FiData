package com.fisk.system.web;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.entity.SystemLogoInfoDTO;
import com.fisk.system.service.SystemLogoInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author SongJianJian
 */
@Api(tags = {SwaggerConfig.SYSTEM})
@RestController
@RequestMapping("/systemlogo")
@Slf4j
public class SystemLogoInfoController {

    @Resource
    private SystemLogoInfoService systemLogoService;

    /**
     * 新增系统logo及系统名称
     *
     * @param title
     * @param file
     * @return
     */
    @ApiOperation("新增系统logo及title")
    @PostMapping("/saveLogoInfo")
    @ControllerAOPConfig(printParams = false)
    @ResponseBody
    public ResultEntity<Object> saveLogoInfo(String title, @RequestParam(value = "file", required = true) MultipartFile file) {
        return ResultEntityBuild.build(systemLogoService.saveLogoInfo(title, file));
    }

    /**
     * 更新系统logo及系统名称
     *
     * @param systemLogoInfoDTO
     * @param file
     * @return
     */
    @ApiOperation("更新系统logo或title")
    @PutMapping("/updateLogoInfo")
    @ControllerAOPConfig(printParams = false)
    @ResponseBody
    public ResultEntity<Object> updateLogoInfo(SystemLogoInfoDTO systemLogoInfoDTO, @RequestParam(value = "file", required = false) MultipartFile file) {
        return ResultEntityBuild.build(systemLogoService.updateLogoInfo(systemLogoInfoDTO, file));
    }

    /**
     * 获取系统logo及系统名称
     * @return
     */
    @ApiOperation("获取系统logo图片及title")
    @GetMapping("/getLogoInfo")
    public ResultEntity<Object> getLogoInfo(){
        return systemLogoService.getLogoInfo();
    }
}
