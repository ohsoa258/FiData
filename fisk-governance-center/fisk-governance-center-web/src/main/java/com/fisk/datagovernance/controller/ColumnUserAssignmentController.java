package com.fisk.datagovernance.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.config.SwaggerConfig;
import com.fisk.datagovernance.dto.datasecurity.ColumnUserAssignmentDTO;
import com.fisk.datagovernance.service.datasecurity.ColumnUserAssignmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Api(tags = SwaggerConfig.COLUMN_User_Assignment)
@RestController
@RequestMapping("/columnuserassignment")
public class ColumnUserAssignmentController {

    @Autowired
    private ColumnUserAssignmentService service;

}
