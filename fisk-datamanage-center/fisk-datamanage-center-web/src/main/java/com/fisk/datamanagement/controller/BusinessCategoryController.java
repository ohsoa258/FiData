package com.fisk.datamanagement.controller;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.businessclassification.BusinessCategorySortDTO;
import com.fisk.datamanagement.dto.category.BusinessCategoryAssignmentDTO;
import com.fisk.datamanagement.dto.category.CategoryQueryDTO;
import com.fisk.datamanagement.dto.category.IndexForAssetCatalogDTO;
import com.fisk.datamanagement.dto.classification.*;
import com.fisk.datamanagement.dto.modelAndIndex.ModelAndIndexMappingDTO;
import com.fisk.datamanagement.entity.BusinessExtendedfieldsPO;
import com.fisk.datamanagement.entity.FactTreePOs;
import com.fisk.datamanagement.service.*;
import com.fisk.datamodel.dto.businessprocess.BusinessQueryDataParamDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
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
    BusinessExtendedfieldsService businessExtendedfieldsService;

    @Resource
    FactTreeListExtendedfieldsService factTreeListExtendedfieldsService;

    @Resource
    BusinessTargetinfoService businessTargetinfoService;

    @Resource
    BusinessHistoryService businessHistoryService;

    @ApiOperation("获取业务指标数据树状列表")
    @GetMapping("/getBusinessMetaDataList")
    public ResultEntity<Object> getBusinessMetaDataList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessCategoryService.getCategoryTree());
    }


    /**
     * 获取维度tree
     * @return
     */
    @ApiOperation("获取业务指标数据列表")
    @GetMapping("/dimension/getDimension/{name}")
    public ResultEntity<Object> getDimensionList(@PathVariable("name") String name) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessTargetinfoService.getDimensionList(name));
    }

    /**
     * 获取维度tree
     *
     * @return
     */
    @ApiOperation("获取维度tree")
    @GetMapping("/dimension/getDimensionTree")
    public ResultEntity<Object> getDimensionTreeList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessCategoryService.getDimensionTreeList());
    }

    @ApiOperation("获取事实tree")
    @GetMapping("/dimension/getFactTrees")
    public ResultEntity<Object> getFactTreeList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessCategoryService.getFactTreeList());
    }


    @ApiOperation("根据指标id展示新增维度数据")
    @GetMapping("/dimension/getDimensionTreeShow/{indexid}")
    public List<BusinessExtendedfieldsPO> addBusinessExtendedfields(@PathVariable("indexid") String indexid) {
        return businessExtendedfieldsService.addBusinessExtendedfields(indexid);
    }


    @ApiOperation("根据底层表id展示新增事实数据")
    @GetMapping("/dimension/getFactTreeListShow/{id}")
    public List<FactTreePOs> addFactTreeListExtendedfields(@PathVariable("pid") String pid) {
        return factTreeListExtendedfieldsService.addFactTreeListExtendedfields(pid);
    }

    @ApiOperation("添加指标主题数据")
    @PostMapping("/addBusinessMetaData")
    public ResultEntity<Object> addBusinessMetaData(@Validated @RequestBody BusinessCategoryDTO dto) {
        return ResultEntityBuild.build(businessCategoryService.addCategory(dto));
    }

    @ApiOperation("获取指标主题浏览权限数据")
    @GetMapping("/getBusinessCategoryAssignment")
    public ResultEntity<Object> getBusinessCategoryAssignment(@RequestParam("pid") String pid) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessCategoryService.getBusinessCategoryAssignment(pid));
    }

    @ApiOperation("添加指标主题浏览权限数据")
    @PostMapping("/addBusinessCategoryAssignment")
    public ResultEntity<Object> addBusinessCategoryAssignment(@RequestBody BusinessCategoryAssignmentDTO dto) {
        return ResultEntityBuild.build(businessCategoryService.addBusinessCategoryAssignment(dto));
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


    @ApiOperation("获取业务指标明细数据列表(目录)")
    @GetMapping("/getBusinessMetaDataDetailMenuList")
    public ResultEntity<Object> getBusinessMetaDataDetailMenuList(@RequestParam("pid") String pid) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessTargetinfoService.getBusinessMetaDataDetailMenuList(pid));
    }

    @ApiOperation("获取业务指标明细数据列表(数据)")
    @GetMapping("/getBusinessMetaDataNameList")
    public ResultEntity<Object> getBusinessMetaDataNameList(@RequestParam(value = "key", required = false) String key) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessTargetinfoService.getBusinessMetaDataNameList(key));
    }

    @ApiOperation("获取业务指标明细数据")
    @GetMapping("/getBusinessMetaDataDetail")
    public ResultEntity<Object> getBusinessMetaDataDetailList(@RequestParam("id") String id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessTargetinfoService.SelectClassification(id));
    }

    @ApiOperation("根据属性表明和字段获取业务指标粒度明细数据列表")
    @GetMapping("/getBusinessMetaDataDetailLists")
    public ResultEntity<Object> getBusinessMetaDataDetailLists(Integer fieldMetadataId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessTargetinfoService.SelectClassifications(fieldMetadataId));
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
        String type = null;
        List<String> ids = null;

        if (!"".equals(json.getString("type"))) {
            type = json.getString("type");
        }
        if (!"".equals(json.getString("ids"))) {
            ids = Arrays.asList(json.getString("ids").split(","));
        }
        businessTargetinfoService.downLoad(type,ids, response);
    }

    @ApiOperation("查询历史指标主题historyId")
    @GetMapping("/getTargetinfoHistoryId/{id}")
    public ResultEntity<Object> getTargetinfoHistoryId(@PathVariable("id") Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessHistoryService.getHistoryId(id));
    }

    @ApiOperation("查询历史指标主题明细数据")
    @GetMapping("/getTargetinfoHistory/{historyId}")
    public ResultEntity<Object> getTargetinfoHistory(@PathVariable("historyId") String historyId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessTargetinfoService.getTargetinfoHistory(historyId));
    }

    @ApiOperation("获取上级指标数据树状列表")
    @GetMapping("/getParentBusinessDataList")
    public ResultEntity<Object> getParentBusinessDataList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessCategoryService.getParentBusinessDataList());
    }

    @ApiOperation("获取业务指标数据树状列表(目录和数据)")
    @GetMapping("/getAllBusinessMetaDataList")
    public ResultEntity<Object> getAllBusinessMetaDataList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessCategoryService.getAllBusinessMetaDataList());
    }

    /**
     * 关联数仓表字段和指标标准（维度表字段 指标粒度）
     * 维度表字段则关联 指标粒度
     * 事实表字段则关联 指标所属
     *
     * @param dtos
     * @return
     */
    @ApiOperation("关联数仓表字段和指标标准（维度表字段 指标粒度）")
    @PutMapping("/setMetricGranularityByModelField")
    public ResultEntity<Object> setMetricGranularityByModelField(@RequestBody List<ModelAndIndexMappingDTO> dtos) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessExtendedfieldsService.setMetricGranularityByModelField(dtos));
    }

    /**
     * 关联数仓表字段和指标标准（事实表字段 指标所属）
     * 维度表字段则关联 指标粒度
     * 事实表字段则关联 指标所属
     *
     * @param dtos
     * @return
     */
    @ApiOperation("关联数仓表字段和指标标准（事实表字段 指标所属）")
    @PutMapping("/setMetricBelongsByModelField")
    public ResultEntity<Object> setMetricBelongsByModelField(@RequestBody List<ModelAndIndexMappingDTO> dtos) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, factTreeListExtendedfieldsService.setMetricBelongsByModelField(dtos));
    }

    /**
     * 数仓建模获取所有业务指标 只获取id 名称
     *
     * @return
     */
    @ApiOperation("数仓建模获取所有业务指标 只获取id 名称")
    @GetMapping("/modelGetBusinessTargetInfoList")
    public List<BusinessTargetinfoDTO> modelGetBusinessTargetInfoList() {
        return businessTargetinfoService.modelGetBusinessTargetInfoList();
    }

    /**
     * 获取数仓字段和指标所属表里所有关联关系 只获取字段id 和指标id
     *
     * @return
     */
    @ApiOperation("获取数仓字段和指标所属表里所有关联关系 只获取字段id 和指标id")
    @GetMapping("/modelGetFactTreeList")
    public List<FacttreeListDTO> modelGetFactTreeList(@RequestParam("tblId") Integer tblId) {
        return businessTargetinfoService.modelGetFactTreeList(tblId);
    }

    /**
     * 获取数仓字段和指标粒度表里所有关联关系 只获取字段id 和指标id
     *
     * @return
     */
    @ApiOperation("获取数仓字段和指标所属表里所有关联关系 只获取字段id 和指标id")
    @GetMapping("/modelGetMetricMapList")
    public List<BusinessExtendedfieldsDTO> modelGetMetricMapList() {
        return businessTargetinfoService.modelGetMetricMapList();
    }

    /**
     * 数据资产 - 资产目录 按指标标准分类
     *
     * @return
     */
    @ApiOperation("数据资产 - 资产目录 按指标标准分类")
    @GetMapping("/getIndexForAssetCatalog")
    public ResultEntity<List<IndexForAssetCatalogDTO>> getIndexForAssetCatalog() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessTargetinfoService.getIndexForAssetCatalog());
    }


    @ApiOperation("获指标标准数量")
    @GetMapping("/getBusinessTargetinfoTotal")
    public ResultEntity<Object> getBusinessTargetinfoTotal() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessTargetinfoService.getBusinessTargetinfoTotal());
    }

    @ApiOperation(value = "筛选器")
    @PostMapping("/pageFilter")
    public ResultEntity<List<BusinessTargetinfoMenuDTO>> pageFilter(@RequestBody CategoryQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, businessTargetinfoService.pageFilter(dto));
    }

    @ApiOperation("指标管理排序更新")
    @PostMapping("/businessCategorySort")
    public ResultEntity<Object> businessCategorySort(@RequestBody BusinessCategorySortDTO dto) {
        return ResultEntityBuild.build(businessCategoryService.businessCategorySort(dto));
    }

    @ApiOperation("获取指标数据查询参数")
    @GetMapping("/getBusinessQueryDataParam")
    public ResultEntity<BusinessQueryDataParamDTO> getBusinessQueryDataParam(@RequestParam("fieldId") Integer fieldId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,businessTargetinfoService.getBusinessQueryDataParam(fieldId));
    }
}
