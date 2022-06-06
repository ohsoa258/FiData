package com.fisk.datamodel.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.taskschedule.ComponentIdDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.service.IDataFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.DATAFACTORY})
@RestController
@RequestMapping("/DataFactory")
public class DataFactoryController {
    @Resource
    IDataFactory service;

    @PostMapping("/getTableId")
    public ResultEntity<List<ChannelDataDTO>> getTableId(@RequestBody NifiComponentsDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableIds(dto));
    }

    @ApiOperation("根据appId和tableId 获取appName和tableName")
    @PostMapping("/getAppNameAndTableName")
    public ResultEntity<Object> getAppNameAndTableName(@RequestBody DataAccessIdsDTO dto) {
        ResultEntity<ComponentIdDTO> result = service.getBusinessAreaNameAndTableName(dto);
        return ResultEntityBuild.build(ResultEnum.SUCCESS, result);
    }

}
