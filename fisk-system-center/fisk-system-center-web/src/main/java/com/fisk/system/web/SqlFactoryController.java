package com.fisk.system.web;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.dto.SqlCheckDTO;
import com.fisk.system.service.SqlFactoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-27 13:40
 * @description
 */

@Api(tags = {SwaggerConfig.SQLFACTORY_CONTROLLER})
@RestController
@RequestMapping("/sqlFactroy")
public class SqlFactoryController {
    @Resource
    private SqlFactoryService sqlFactoryService;

    @PostMapping("/sqlCheck")
    @ApiOperation("SQL语句校验接口")
    public ResultEntity<List<TableMetaDataObject>> sqlCheck(@RequestBody SqlCheckDTO sqlCheckDTO) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, sqlFactoryService.sqlCheck(sqlCheckDTO));
    }
}
