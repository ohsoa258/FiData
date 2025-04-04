package com.fisk.datamodel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.service.accessAndModel.AccessAndModelAppDTO;
import com.fisk.common.service.accessAndModel.ModelAreaAndFolderDTO;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.dbMetaData.dto.*;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.datamanagement.dto.metamap.MetaMapDTO;
import com.fisk.datamanagement.dto.metamap.MetaMapTblDTO;
import com.fisk.datamodel.config.SwaggerConfig;
import com.fisk.datamodel.dto.DwFieldQueryDTO;
import com.fisk.datamodel.dto.atomicindicator.IndicatorQueryDTO;
import com.fisk.datamodel.dto.businessarea.*;
import com.fisk.datamodel.service.IBusinessArea;
import com.fisk.datamodel.vo.DimAndFactCountVO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
@Api(tags = {SwaggerConfig.BUSINESS_AREA})
@RestController
@RequestMapping("/business")
@Slf4j
public class BusinessAreaController {

    @Resource
    private IBusinessArea service;

    @PostMapping("/add")
    @ApiOperation(value = "添加业务域[对象]")
    public ResultEntity<Object> addData(@RequestBody BusinessAreaDTO businessAreaDTO) {

        return ResultEntityBuild.build(service.addData(businessAreaDTO));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "回显数据: 根据id查询(url拼接)")
    public ResultEntity<BusinessAreaDTO> getData(@PathVariable("id") long id) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getData(id));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "业务域修改(对象)")
    public ResultEntity<Object> editData(@RequestBody BusinessAreaDTO businessAreaDTO) {

        return ResultEntityBuild.build(service.updateBusinessArea(businessAreaDTO));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除业务域(url拼接)")
    public ResultEntity<Object> deleteData(@PathVariable("id") long id) {
        return ResultEntityBuild.build(service.deleteBusinessArea(id));
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询(url拼接)")
    public ResultEntity<Page<Map<String, Object>>> queryByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "1") Integer rows) {

        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.queryByPage(key, page, rows));
    }

    @GetMapping("/getColumn")
    @ApiOperation(value = "获取业务域表字段")
    public ResultEntity<Object> getBusinessColumn() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessAreaColumn());
    }

    @PostMapping("/getDataList")
    @ApiOperation(value = "获取业务域数据列表")
    public ResultEntity<Page<BusinessPageResultDTO>> getDataList(@RequestBody BusinessQueryDTO query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataList(query));
    }

    @PostMapping("/getBusinessAreaPublicData")
    @ApiOperation(value = "根据事实字段集合,在Doris中创建相关表")
    public ResultEntity<Object> getBusinessAreaPublicData(@RequestBody IndicatorQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessAreaPublicData(dto));
    }

    @PostMapping("/getBusinessAreaTable")
    @ApiOperation(value = "获取业务域下维度/事实表")
    public ResultEntity<Page<PipelineTableLogVO>> getBusinessAreaTable(@RequestBody PipelineTableQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessAreaTable(dto));
    }

    @PostMapping("/getBusinessAreaTableDetail")
    @ApiOperation(value = "根据业务id、表类型、表id,获取表详情")
    public ResultEntity<BusinessAreaTableDetailDTO> getBusinessAreaTableDetail(@RequestBody BusinessAreaQueryTableDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessAreaTableDetail(dto));
    }

    @PostMapping("/redirect")
    @ApiOperation(value = "跳转页面: 查询出当前表具体在哪个管道中使用,并给跳转页面提供数据")
    public ResultEntity<Object> redirect(@Validated @RequestBody ModelRedirectDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.redirect(dto));
    }

    @PostMapping("/getDataStructure")
    @ApiOperation(value = "获取数据建模结构")
    public ResultEntity<List<FiDataMetaDataDTO>> getDataModelStructure(@RequestBody FiDataMetaDataReqDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataModelStructure(dto));
    }

    @PostMapping("/getDataTableStructure")
    @ApiOperation(value = "获取数据建模表结构")
    public ResultEntity<List<FiDataMetaDataTreeDTO>> getDataModelTableStructure(@RequestBody FiDataMetaDataReqDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataModelTableStructure(dto));
    }

    @PostMapping("/setDataStructure")
    @ApiOperation(value = "刷新数据建模结构")
    public ResultEntity<Object> setDataModelStructure(@RequestBody FiDataMetaDataReqDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.setDataModelStructure(dto));
    }

    /**
     * 数据质量左侧Tree接口-获取dw数据源文件夹表层级
     *
     * @return
     */
    @PostMapping("/dataQuality_GetDwFolderTableTree")
    @ApiOperation(value = "数据质量左侧Tree接口-获取dw数据源文件夹表层级")
    public ResultEntity<DataQualityDataSourceTreeDTO> getDwFolderTableTree() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDwFolderTableTree());
    }

    /**
     * 数据质量左侧Tree接口-根据表ID获取表下面的字段
     *
     * @return
     */
    @PostMapping("/dataQuality_GetDwTableFieldByTableId")
    @ApiOperation(value = "数据质量左侧Tree接口-根据表ID获取表下面的字段")
    public ResultEntity<List<DataQualityDataSourceTreeDTO>> getDwTableFieldByTableId(@RequestBody DwFieldQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDwTableFieldByTableId(dto));
    }

    @PostMapping("/getTableDataStructure")
    @ApiOperation(value = "获取数据建模表结构(数据标准用)")
    public ResultEntity<Object> getTableDataStructure(@RequestBody FiDataMetaDataReqDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTableDataStructure(dto));
    }

    @PostMapping("/getFieldDataStructure")
    @ApiOperation(value = "获取数据建模字段结构(数据标准用)")
    public ResultEntity<Object> getFieldDataStructure(@RequestBody ColumnQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFieldDataStructure(dto));
    }
    @GetMapping("/searchStandardBeCitedField")
    @ApiOperation(value = "搜数据建模数据元关联字段(数据标准用)")
    public ResultEntity<Object> searchStandardBeCitedField(@RequestParam("key")String key){
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.searchStandardBeCitedField(key));
    }
    @PostMapping("/getFiDataTableMetaData")
    @ApiOperation(value = "根据表信息/字段ID,获取表/字段基本信息")
    public ResultEntity<List<FiDataTableMetaDataDTO>> getFiDataTableMetaData(@RequestBody FiDataTableMetaDataReqDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFiDataTableMetaData(dto));
    }

    @GetMapping("/getBusinessAreaList")
    @ApiOperation(value = "获取业务域下拉列表")
    public ResultEntity<List<AppBusinessInfoDTO>> getBusinessAreaList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessAreaList());
    }

    @GetMapping("/getPublishSuccessTab/{businessId}")
    @ApiOperation(value = "获取业务域下发布成功表")
    public ResultEntity<Object> getPublishSuccessTab(@PathVariable("businessId") int businessId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getPublishSuccessTab(businessId));
    }

    @GetMapping("/getDataModelMetaData")
    @ApiOperation(value = "获取数据建模所有元数据")
    public ResultEntity<List<MetaDataInstanceAttributeDTO>> getDataModelMetaData() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataModelMetaData());
    }

    /**
     * 根据上次更新元数据的时间获取数据建模所有元数据
     *
     * @param lastSyncTime
     * @return
     */
    @GetMapping("/getDataModelMetaDataByLastSyncTime")
    @ApiOperation(value = "根据上次更新元数据的时间获取数据建模的元数据")
    public ResultEntity<List<MetaDataInstanceAttributeDTO>> getDataModelMetaDataByLastSyncTime(@RequestParam("lastSyncTime") String lastSyncTime) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getDataModelMetaDataByLastSyncTime(lastSyncTime));
    }

    @PostMapping("/buildDimensionKeyScript")
    @ApiOperation(value = "维度或者事实构建维度key脚本预览")
    public ResultEntity<Object> buildDimensionKeyScript(@RequestBody List<TableSourceRelationsDTO> dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.buildDimensionKeyScript(dto));
    }

    @GetMapping("/dataTypeList/{businessId}")
    @ApiOperation(value = "获取建模数据类型")
    public ResultEntity<Object> dataTypeList(@PathVariable("businessId") int businessId) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.dataTypeList(businessId));
    }

    @PostMapping("/overlayCodePreview")
    @ApiOperation(value = "覆盖方式预览代码")
    public ResultEntity<Object> overlayCodePreview(@RequestBody OverlayCodePreviewDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.overlayCodePreview(dto));
    }

    /**
     * 数仓建模首页--获取总共的维度表和事实表--不包含公共域维度
     *
     * @return
     */
    @GetMapping("/getTotalDimAndFactCount")
    @ApiOperation(value = "数仓建模首页--获取总共的维度表和事实表--不包含公共域维度")
    public ResultEntity<DimAndFactCountVO> getTotalDimAndFactCount() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getTotalDimAndFactCount());
    }

    /**
     * 获取数仓建模所有业务域和业务域下的所有表（包含事实表和维度表和应用下建的公共域维度表）
     *
     * @return
     */
    @ApiOperation("获取数仓建模所有业务域和业务域下的所有表（包含事实表和维度表和应用下建的公共域维度表）")
    @GetMapping("/getAllAreaAndTables")
    public ResultEntity<List<AccessAndModelAppDTO>> getAllAreaAndTables() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAllAreaAndTables());
    }

    /**
     * 为数仓etl树获取数仓建模所有业务域和业务域下的所有表
     *
     * @return
     */
    @ApiOperation("为数仓etl树获取数仓建模所有业务域和业务域下的所有表")
    @GetMapping("/getAllAreaAndTablesForEtlTree")
    public ResultEntity<List<AccessAndModelAppDTO>> getAllAreaAndTablesForEtlTree() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAllAreaAndTablesForEtlTree());
    }

    /**
     * 获取数仓建模所有业务域和业务域下文件夹
     *
     * @return
     */
    @ApiOperation("获取数仓建模所有业务域和业务域下文件夹")
    @GetMapping("/getAllAreaAndFolder")
    public ResultEntity<List<ModelAreaAndFolderDTO>> getAllAreaAndFolder() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAllAreaAndFolder());
    }

    @PostMapping("/getBusinessAreaByIds")
    @ApiOperation("根据业务id集合获取业务详情")
    public ResultEntity<List<BusinessAreaDTO>> getBusinessAreaByIds(@RequestBody List<Integer> ids) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessAreaByIds(ids));
    }

    /**
     * 获取元数据地图 数仓建模
     */
    @ApiOperation("获取元数据地图 数仓建模")
    @GetMapping("/modelGetMetaMap")
    public List<MetaMapDTO> modelGetMetaMap() {
        return service.modelGetMetaMap();
    }

    /**
     * 元数据地图 获取业务过程下的表
     *
     * @param processId   业务过程id或维度文件夹id
     * @param processType 类型 1维度文件夹 2业务过程
     * @return
     */
    @ApiOperation("元数据地图 获取业务过程下的表")
    @GetMapping("/modelGetMetaMapTableDetail")
    public List<MetaMapTblDTO> modelGetMetaMapTableDetail(
            @RequestParam("processId") Integer processId,
            @RequestParam("processType") Integer processType
    ) {
        return service.modelGetMetaMapTableDetail(processId, processType);
    }
    @ApiOperation("获取数仓业务域数量")
    @GetMapping("/getBusinessTotal")
    public ResultEntity<Object> getBusinessTotal() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessTotal());
    }

    @ApiOperation("获取数仓数据表数量")
    @GetMapping("/getBusinessTableTotal")
    public ResultEntity<Object> getBusinessTableTotal() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBusinessTableTotal());
    }
}
