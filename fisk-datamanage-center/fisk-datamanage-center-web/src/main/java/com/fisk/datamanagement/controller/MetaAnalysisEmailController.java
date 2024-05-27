package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.metaanalysisemailconfig.MetaAnalysisEmailConfigDTO;
import com.fisk.datamanagement.service.IMetaAnalysisEmailConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.META_ANALYTICS_EMAIL})
@RestController
@RequestMapping("/email")
public class MetaAnalysisEmailController {


    @Resource
    private IMetaAnalysisEmailConfigService emailConfigService;

    /**
     * 获取变更分析邮箱配置的详情
     */
    @ApiOperation("获取变更分析邮箱配置的详情")
    @GetMapping("/getMetaAnalysisEmailConfig")
    public ResultEntity<MetaAnalysisEmailConfigDTO> getMetaAnalysisEmailConfig() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, emailConfigService.getMetaAnalysisEmailConfig());
    }

    /**
     * 编辑变更分析邮箱配置的详情
     */
    @ApiOperation("编辑变更分析邮箱配置的详情")
    @PostMapping("/editMetaAnalysisEmailConfig")
    public ResultEntity<Object> editMetaAnalysisEmailConfig(@RequestBody MetaAnalysisEmailConfigDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, emailConfigService.editMetaAnalysisEmailConfig(dto));
    }

}
