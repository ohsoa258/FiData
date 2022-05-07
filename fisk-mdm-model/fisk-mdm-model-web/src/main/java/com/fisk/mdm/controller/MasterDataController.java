package com.fisk.mdm.controller;


import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.dto.masterdata.ImportDataQueryDTO;
import com.fisk.mdm.dto.masterdata.ImportParamDTO;
import com.fisk.mdm.service.IMasterDataService;
import com.fisk.mdm.vo.resultObject.ResultObjectVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 主数据控制器
 *
 * @author ChenYa
 * @date 2022/04/27
 */
@Api(tags = {SwaggerConfig.TAG_5})
@RestController
@RequestMapping("/masterData")
public class MasterDataController {
    @Resource
    private IMasterDataService service;

    /**
     * 基于构造器注入
     */
    private final HttpServletResponse response;
    private final HttpServletRequest request;
    public MasterDataController(HttpServletResponse response,HttpServletRequest request) {

        this.response = response;
        this.request=request;
    }

    @ApiOperation("分页查询所有model")
    @GetMapping("/list")
    public ResultEntity<ResultObjectVO> getAll(Integer entityId , Integer modelVersionId) {
        return service.getAll(entityId ,modelVersionId);
    }

    @ApiOperation("下载模板")
    @GetMapping("/downloadTemplate")
    public ResultEntity<Object> downloadTemplate(Integer entityId){
        return ResultEntityBuild.build(service.downloadTemplate(entityId,response));
    }

    @ApiOperation("导入模板")
    @PostMapping("/importExcel")
    @ResponseBody
    @ControllerAOPConfig(printParams=false)
    public ResultEntity<Object> importExcel(ImportParamDTO dto, @RequestParam("file") MultipartFile file){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.importTemplateData(dto,file));
    }

    @ApiOperation("模板数据分页")
    @PostMapping("/importDataQuery")
    @ResponseBody
    public ResultEntity<Object> importDataQuery(@Validated @RequestBody ImportDataQueryDTO dto){
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.importDataQuery(dto));
    }

}
