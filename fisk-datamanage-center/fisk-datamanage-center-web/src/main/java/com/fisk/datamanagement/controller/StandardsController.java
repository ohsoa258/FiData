package com.fisk.datamanagement.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.advice.ControllerAOPConfig;
import com.fisk.common.service.dbMetaData.dto.ColumnQueryDTO;
import com.fisk.common.service.dbMetaData.dto.DataQualityDataSourceTreeDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.dto.category.CategoryQueryDTO;
import com.fisk.datamanagement.dto.standards.*;
import com.fisk.datamanagement.service.StandardsBeCitedService;
import com.fisk.datamanagement.service.StandardsMenuService;
import com.fisk.datamanagement.service.StandardsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-11-20
 * @Description:
 */
@Api(tags = {SwaggerConfig.STANDARDS})
@RestController
@RequestMapping("/Standards")
public class StandardsController {
    @Resource
    StandardsMenuService standardsMenuService;
    @Resource
    StandardsService standardsService;
    @Resource
    StandardsBeCitedService standardsBeCitedService;

    @ApiOperation("查看数据标准树形标签")
    @GetMapping("/getStandardsTree")
    public ResultEntity<List<StandardsTreeDTO>> getStandardsTree() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsMenuService.getStandardsTree());
    }

    @ApiOperation("查看数据标准树形标签(数据校验用)")
    @GetMapping("/getStandardsTreeByCheck")
    public ResultEntity<List<StandardsTreeDTO>> getStandardsTreeByCheck() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsMenuService.getStandardsTreeByCheck());
    }

    @ApiOperation("查看数据标准树形标签--非懒加载")
    @GetMapping("/getStandardsAllTree")
    public ResultEntity<List<StandardsTreeDTO>> getStandardsAllTree() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsMenuService.getStandardsAllTree());
    }

    @ApiOperation("添加或修改数据标准标签")
    @PostMapping("/addStandardsMenu")
    public ResultEntity<Object> addorUpdateStandardsMenu(@RequestBody StandardsMenuDTO dto) {
        return ResultEntityBuild.build(standardsMenuService.addorUpdateStandardsMenu(dto));
    }

    @ApiOperation("删除数据标准标签")
    @PostMapping("/delStandardsMenu")
    public ResultEntity<Object> delStandardsMenu(@RequestBody List<Integer> ids) {
        return ResultEntityBuild.build(standardsMenuService.delStandardsMenu(ids));
    }

    @ApiOperation("获取数据标准")
    @GetMapping("/getStandards/{id}")
    public ResultEntity<StandardsDTO> getStandards(@PathVariable("id") int id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.getStandards(id));
    }

    @ApiOperation("添加数据标准")
    @PostMapping("/addStandards")
    public ResultEntity<Object> addStandards(@RequestBody StandardsDTO dto) {
        return ResultEntityBuild.build(standardsService.addStandards(dto));
    }

    @ApiOperation("修改数据标准")
    @PostMapping("/updateStandards")
    public ResultEntity<Object> updateStandards(@RequestBody StandardsDTO dto) {
        return ResultEntityBuild.build(standardsService.updateStandards(dto));
    }

    @ApiOperation("删除数据标准")
    @DeleteMapping("/delStandards/{id}")
    public ResultEntity<Object> delStandards(@PathVariable("id") int id) {
        return ResultEntityBuild.build(standardsService.delStandards(id));
    }

    @ApiOperation("获取表字段信息")
    @PostMapping("/getColumn")
    public ResultEntity<Object> getColumn(@RequestBody ColumnQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.getColumn(dto));
    }

    @ApiOperation("查看数据源结构树")
    @PostMapping("/getDataSourceTree")
    public ResultEntity<List<DataSourceInfoDTO>> getDataSourceTree() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.getDataSourceTree());
    }

    @ApiOperation("预览数据详情")
    @PostMapping("/preview")
    public ResultEntity<QueryResultDTO> preview(@RequestBody QueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.preview(dto));
    }

    @ApiOperation("导出数据标准")
    @PostMapping("/exportStandards")
    @ControllerAOPConfig(printParams = false)
    public void exportStandards(@RequestBody List<Integer> ids, HttpServletResponse response) {
        standardsService.exportStandards(ids, response);
    }

    @ApiOperation("数据标准排序更新")
    @PostMapping("/standardsSort")
    public ResultEntity<Object> standardsSort(@RequestBody StandardsSortDTO dto) {
        return ResultEntityBuild.build(standardsService.standardsSort(dto));
    }

    @ApiOperation("数据标准分页查询数据元")
    @PostMapping("/standardsQuery")
    public ResultEntity<Object> standardsQuery(@RequestBody StandardsQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.standardsQuery(dto));
    }

    @ApiOperation("数据标准根据menuId查询数据元")
    @GetMapping("/getStandardsDetailMenuList")
    public ResultEntity<Object> getStandardsDetailMenuList(@RequestParam("menuId") String menuId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.getStandardsDetailMenuList(menuId));
    }
    @ApiOperation("数据标准根据关键字查询数据元")
    @GetMapping("/getStandardsDetailListByKeyWord")
    public ResultEntity<Object> getStandardsDetailListByKeyWord(@RequestParam(value = "keyWord",required = false) String keyWord) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.getStandardsDetailListByKeyWord(keyWord));
    }

    @ApiOperation(value = "筛选器")
    @PostMapping("/pageFilter")
    public ResultEntity<List<StandardsDetailDTO>> pageFilter(@RequestBody CategoryQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.pageFilter(dto));
    }

    @ApiOperation("根据数据源信息查询数据标准基本属性")
    @GetMapping("/getStandardsBySource")
    public ResultEntity<List<StandardsDTO>> getStandardsBySource(Integer fieldMetadataId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.getStandardsBySource(fieldMetadataId));
    }

    @ApiOperation("数据元导入基本属性")
    @PostMapping("/importExcelStandards")
    @ResponseBody
    @ControllerAOPConfig(printParams = false)
    public ResultEntity<Object> importExcelStandards(long menuId, @RequestParam("file") MultipartFile file) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.importExcelStandards(menuId, file));
    }

    @ApiOperation("获取所有数据标准树形结构(数据校验用)")
    @PostMapping("/dataQuality_GetAllStandardsTree")
    public ResultEntity<DataQualityDataSourceTreeDTO> dataQuality_GetAllStandardsTree() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.dataQuality_GetAllStandardsTree());
    }

    /**
     * 数仓建模-关联字段和数据源标准
     *
     * @param dtos
     * @return
     */
    @ApiOperation("数仓建模-关联字段和数据源标准")
    @PostMapping("/setStandardsByModelField")
    public ResultEntity<Object> setStandardsByModelField(@RequestBody List<StandardsBeCitedDTO> dtos) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.setStandardsByModelField(dtos));
    }

    /**
     * 数仓建模-获取所有数据元标准 只获取数据元id 和中文名、menuid
     *
     * @return
     */
    @ApiOperation("数仓建模-获取所有数据元标准 只获取数据元id 和中文名、menuid")
    @GetMapping("/modelGetStandards")
    public List<StandardsDTO> modelGetStandards() {
        return standardsService.modelGetStandards();
    }

    /**
     * 数仓建模-获取所有数仓字段和数据元标准的关联关系 数仓建模-获取所有数仓字段和数据元标准的关联关系 只获取字段id 和数据元标准id
     *
     * @return
     */
    @ApiOperation("数仓建模-获取所有数仓字段和数据元标准的关联关系")
    @GetMapping("/modelGetStandardsMap")
    public List<StandardsBeCitedDTO> modelGetStandardsMap() {
        return standardsService.modelGetStandardsMap();
    }

    /**
     * 主数据-获取所有主数据字段和数据元标准的关联关系 只获取字段id 和数据元标准id
     *
     * @return
     */
    @ApiOperation("主数据-获取所有主数据字段和数据元标准的关联关系")
    @GetMapping("/mdmGetStandardsMap")
    public List<StandardsBeCitedDTO> mdmGetStandardsMap() {
        return standardsService.mdmGetStandardsMap();
    }

    @ApiOperation("获取所有数据元标准menu-只要id和name")
    @GetMapping("/getStandardMenus")
    public List<StandardsMenuDTO> getStandardMenus() {
        return standardsMenuService.getStandardMenus();
    }

    @ApiOperation("校验数据元标准关联字段是否已经存在")
    @GetMapping("/checkStandardBeCited")
    public ResultEntity<Object> checkStandardBeCited(@RequestParam("standardsId") Integer standardsId,
                                                     @RequestParam("dbId") Integer dbId,
                                                     @RequestParam("tableId") Integer tableId,
                                                     @RequestParam("fieldId") Integer fieldId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsBeCitedService.checkStandardBeCited(standardsId, dbId, tableId, fieldId));
    }


    @ApiOperation("根据数据元标准menuId获取所有standardsId(数据校验用)")
    @GetMapping("/getStandardByMenuId")
    public List<Integer> getStandardByMenuId(@RequestParam("menuId") Integer menuId) {
        return standardsMenuService.getStandardByMenuId(menuId);
    }

    /**
     * 数据资产 - 资产目录 按数据元标准分类
     *
     * @return
     */
    @ApiOperation("数据资产 - 资产目录 按数据元标准分类")
    @GetMapping("/getStandardsForAssetCatalog")
    public ResultEntity<List<StandardsForAssetCatalogDTO>> getStandardsForAssetCatalog() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsMenuService.getStandardsForAssetCatalog());
    }

    @ApiOperation("获取数据元标准数量")
    @GetMapping("/getStandardTotal")
    public ResultEntity<Object> getStandardTotal() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.getStandardTotal());
    }

    @ApiOperation("搜索数据元关联字段")
    @GetMapping("/searchStandardBeCitedField")
    public ResultEntity<Object> searchStandardBeCitedField(@RequestParam("key") String key) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, standardsService.searchStandardBeCitedField(key));
    }
}
