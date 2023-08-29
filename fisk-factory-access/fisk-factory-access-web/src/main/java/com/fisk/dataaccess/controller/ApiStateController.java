package com.fisk.dataaccess.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.config.SwaggerConfig;
import com.fisk.dataaccess.dto.apistate.ApiStateDTO;
import com.fisk.dataaccess.service.IApiStateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author lsj
 * @email TenLi@fisksoft.com
 * @date 2023-08-29
 */
@Api(tags = SwaggerConfig.API_STATE)
@RestController
@RequestMapping("/apiState")
public class ApiStateController {

    @Resource
    private IApiStateService apiStateService;


    /**
     * 编辑api开启状态    save or update
     *
     * @param dto
     * @return
     */
    @PostMapping("/editApiState")
    @ApiOperation(value = "编辑api开启状态")
    public ResultEntity<Boolean> editApiState(@RequestBody ApiStateDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,apiStateService.editApiState(dto));
    }

    /**
     * 回显api是否开启状态 get
     *
     * @return
     */
    @GetMapping("/getApiState")
    @ApiOperation(value = "回显api是否开启状态")
    public ResultEntity<ApiStateDTO> getApiState() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,apiStateService.getApiState());
    }
}
