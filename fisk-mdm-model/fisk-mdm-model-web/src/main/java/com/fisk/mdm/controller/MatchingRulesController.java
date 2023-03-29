package com.fisk.mdm.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.masterdatalog.MasterDataLogQueryDTO;
import com.fisk.mdm.dto.mathingrules.MatchingRulesDto;
import com.fisk.mdm.service.IMasterDataLog;
import com.fisk.mdm.service.IMatchingRulesService;
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
@Api(tags = {SwaggerConfig.TAG_12})
@RestController
@RequestMapping("/matchingRules")
public class MatchingRulesController {
    @Resource
    IMatchingRulesService service;

    @ApiOperation("设置匹配规则")
    @PostMapping("/save")
    public ResultEntity<ResultEnum> save(@Validated @RequestBody MatchingRulesDto dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.save(dto));
    }
}
