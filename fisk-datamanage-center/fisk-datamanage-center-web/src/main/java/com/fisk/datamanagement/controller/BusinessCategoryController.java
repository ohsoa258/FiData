package com.fisk.datamanagement.controller;

import com.alibaba.fastjson.JSONObject;
import com.azure.core.annotation.Post;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.businessmetadata.BusinessMetaDataDTO;
import com.fisk.datamanagement.dto.classification.BusinessCategoryDTO;
import com.fisk.datamanagement.dto.classification.BusinessCategoryDefsDTO;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoDefsDTO;
import com.fisk.datamanagement.dto.classification.ClassificationDefsDTO;
import com.fisk.datamanagement.service.BusinessCategoryService;
import com.fisk.datamanagement.service.BusinessTargetinfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author xgf
 * @date 2023年11月20日 10:24
 */
@Api(tags = {SwaggerConfig.BUSINESS_Category})
@RestController
@RequestMapping("/BusinessCategory")
public class BusinessCategoryController {

    @Resource
    BusinessCategoryService businessCategoryService;

    @Resource
    BusinessTargetinfoService businessTargetinfoService;

    @ApiOperation("获取业务指标数据列表")
    @GetMapping("/getBusinessMetaDataList")
    public ResultEntity<Object> getBusinessMetaDataList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessCategoryService.getCategoryTree());
    }


    @ApiOperation("添加指标主题数据")
    @PostMapping("/addBusinessMetaData")
    public ResultEntity<Object> addBusinessMetaData(@Validated @RequestBody BusinessCategoryDTO dto) {
        return ResultEntityBuild.build(businessCategoryService.addCategory(dto));
    }


    @ApiOperation("根据指标主题id删除")
    @DeleteMapping("/deleteCategory/{CategoryId}")
    public ResultEntity<Object> deleteCategory(@PathVariable("CategoryId") String categoryId) {
        return ResultEntityBuild.build(businessCategoryService.deleteCategory(categoryId));
    }

    @ApiOperation("修改指标主题名称")
    @PutMapping("/updateCategory")
    public ResultEntity<Object> updateCategory(@Validated @RequestBody BusinessCategoryDTO dto) {
        return ResultEntityBuild.build(businessCategoryService.updateCategory(dto));
    }

    @ApiOperation("修改指标主题展示顺序")
    @PutMapping("/updateCategorySort")
    public ResultEntity<Object> updateCategorySort(@Validated @RequestBody List<String> dto) {
        return ResultEntityBuild.build(businessCategoryService.updateCategorySort(dto));
    }


    @ApiOperation("获取业务指标明细数据列表")
    @GetMapping("/getBusinessMetaDataDetailList")
    public ResultEntity<Object> getBusinessMetaDataDetailList(String pid) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessTargetinfoService.SelectClassification(pid));
    }

    @ApiOperation("获取业务指标明细类型数据列表")
    @GetMapping("/getBusinessMetaDataDetailTypeList")
    public ResultEntity<Object> getBusinessMetaDataDetailTypeList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessTargetinfoService.SelecttypeClassification());
    }

    @ApiOperation("添加指标主题明细数据")
    @PostMapping("/addBusinessMetaDataDetail")
    public ResultEntity<Object> addBusinessMetaDataDetail(@Validated @RequestBody BusinessTargetinfoDefsDTO dto) {
        return ResultEntityBuild.build(businessTargetinfoService.addTargetinfo(dto));
    }


    @ApiOperation("根据id删除指标明细数据")
    @DeleteMapping("/deleteTargetinfo/{Id}")
    public ResultEntity<Object> deleteTargetinfo(@PathVariable("Id") long id) {
        return ResultEntityBuild.build(businessTargetinfoService.deleteTargetinfo(id));
    }

    @ApiOperation("修改指标主题明细数据")
    @PutMapping("/updateTargetinfo")
    public ResultEntity<Object> updateTargetinfo(@Validated @RequestBody BusinessTargetinfoDefsDTO dto) {
        return ResultEntityBuild.build(businessTargetinfoService.updateTargetinfo(dto));
    }

//    @ApiOperation("导出指标明细数据")
//    @GetMapping("/downLoads")
//    public void downLoad(String id, HttpServletResponse response) {
//        String realPath = "D:\\java\\untitled2\\response\\src\\main\\resources\\下载图片.png";
//        businessTargetinfoService.downLoad(id, response);
//    }

    @ApiOperation("导出指标明细数据")
    @PostMapping("/downloadTargetinfo")
    @ControllerAOPConfig(printParams = false)
    public void downloadApprovalApply(@RequestBody JSONObject json, HttpServletResponse response) {
        String id = null;
        String indicatorname = null;

        if (!"".equals(json.getString("id"))) {
            id = json.getString("id");
        }
        if (!"".equals(json.getString("indicatorname"))) {
            indicatorname = json.getString("indicatorname");
        }
        businessTargetinfoService.downLoad(id, indicatorname, response);
    }

}
