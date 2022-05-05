package com.fisk.mdm.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.config.SwaggerConfig;
import com.fisk.mdm.service.IMasterDataService;
import com.fisk.mdm.vo.masterdata.ExportResultVO;
import com.fisk.mdm.vo.resultObject.ResultObjectVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

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
    public MasterDataController(HttpServletResponse response) {
        this.response = response;
    }

    @ApiOperation("分页查询所有model")
    @GetMapping("/list")
    public ResultEntity<ResultObjectVO> getAll(Integer entityId , Integer modelVersionId) {
        return service.getAll(entityId ,modelVersionId);
    }

    @ApiOperation("下载模板")
    @GetMapping("/downloadTemplate/{entityId}")
    public ResultEntity<Object> downloadTemplate(@PathVariable("entityId") Integer entityId){
        return ResultEntityBuild.build(service.downloadTemplate(entityId,response));
    }


}
