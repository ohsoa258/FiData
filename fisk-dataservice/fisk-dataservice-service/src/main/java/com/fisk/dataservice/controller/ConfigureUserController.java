package com.fisk.dataservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.ApiFieldDataDTO;
import com.fisk.dataservice.dto.UserDTO;
import com.fisk.dataservice.entity.ConfigureUserPO;
import com.fisk.dataservice.service.ConfigureUserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/7/30 14:15
 */
@RestController
@RequestMapping("/User")
public class ConfigureUserController {

    @Resource
    private ConfigureUserService userService;

    @ApiOperation("分页查询所有字段")
    @GetMapping("/getAll")
    public ResultEntity<List<UserDTO>> listData(Page<ConfigureUserPO> page) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, userService.listData(page));
    }
}
