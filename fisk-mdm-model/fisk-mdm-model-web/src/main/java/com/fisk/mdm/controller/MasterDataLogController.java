package com.fisk.mdm.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.masterdata.MasterDataDTO;
import com.fisk.mdm.dto.masterdatalog.MasterDataLogQueryDTO;
import com.fisk.mdm.service.IMasterDataLog;
import com.fisk.mdm.vo.masterdatalog.MasterDataLogPageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.TAG_9})
@RestController
@RequestMapping("/masterDataLog")
public class MasterDataLogController {

    @Resource
    IMasterDataLog service;

    @ApiOperation("分页查询主数据维护日志")
    @PostMapping("/listMasterDataLog")
    public ResultEntity<MasterDataLogPageVO> listMasterDataLog(@Validated @RequestBody MasterDataLogQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.listMasterDataLog(dto));
    }

    @ApiOperation("主数据维护日志回滚")
    @PostMapping("/rollBackMasterData")
    public ResultEntity<ResultEnum> rollBackMasterData(@Validated @RequestBody MasterDataDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.rollBackMasterData(dto));
    }

}
