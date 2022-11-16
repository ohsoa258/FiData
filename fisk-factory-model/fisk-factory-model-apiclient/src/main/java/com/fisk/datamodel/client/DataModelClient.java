package com.fisk.datamodel.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.dto.chartvisual.IndicatorDTO;
import com.fisk.chartvisual.dto.chartvisual.IndicatorFeignDTO;
import com.fisk.chartvisual.dto.chartvisual.IsDimensionDTO;
import com.fisk.chartvisual.dto.chartvisual.TableDataDTO;
import com.fisk.chartvisual.enums.DataDoFieldTypeEnum;
import com.fisk.chartvisual.vo.DataDomainVO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleInfoDTO;
import com.fisk.common.server.ocr.dto.businessmetadata.TableRuleParameterDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataReqDTO;
import com.fisk.dataaccess.dto.taskschedule.DataAccessIdsDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datamodel.dto.atomicindicator.DimensionTimePeriodDTO;
import com.fisk.datamodel.dto.businessarea.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.businessarea.BusinessAreaQueryTableDTO;
import com.fisk.datamodel.dto.businessarea.BusinessAreaTableDetailDTO;
import com.fisk.datamodel.dto.dataops.DataModelTableInfoDTO;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.dto.syncmode.GetTableBusinessDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.pipeline.PipelineTableLogVO;
import com.fisk.task.dto.query.PipelineTableQueryDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author JianWenYang
 */
@FeignClient("datamodel-service")
public interface DataModelClient {

    /**
     * 获取维度表元数据字段--用于DW建表
     *
     * @param id
     * @return 执行结果
     */
    @GetMapping("/attribute/getDimensionEntity")
    ResultEntity<Object> getDimensionEntity(@RequestParam("id") int id);

    /**
     * 获取维度表元数据列表以及字段--用于Doris
     *
     * @param businessAreaId
     * @return 执行结果
     */
    /*@GetMapping("/attribute/getDimensionListEntity")
    ResultEntity<Object> getDimensionListEntity(@RequestParam("businessAreaId") int businessAreaId);*/

    /**
     * 获取事实表元数据字段
     *
     * @param id
     * @return 执行结果
     */
    @GetMapping("/factAttribute/getFactEntity")
    ResultEntity<Object> getFactEntity(@RequestParam("id") int id);

    /**
     * 获取业务过程id,获取业务过程下相关事实
     *
     * @param id
     * @return 执行结果
     */
    @GetMapping("/businessProcess/getBusinessProcessFact/{id}")
    ResultEntity<Object> getBusinessProcessFact(@PathVariable("id") int id);

    /**
     * 获取事实表下所有原子指标以及事实表关联维度
     *
     * @param id
     * @return
     */
    @GetMapping("/AtomicIndicators/getAtomicIndicators")
    ResultEntity<Object> getAtomicIndicators(@RequestParam("id") int id);

    /**
     * 根据同步方式id获取相关数据
     *
     * @param id
     * @return
     */
    @GetMapping("/FactSyncMode/factSyncModePush")
    ResultEntity<Object> factSyncModePush(@PathVariable("id") int id);

    /**
     * 根据业务域id获取相关维度以及原子指标
     *
     * @param id
     * @return
     */
    @GetMapping("/business/getBusinessAreaPublicData")
    ResultEntity<BusinessAreaGetDataDTO> getBusinessAreaPublicData(@RequestParam("id") int id);

    /**
     * 获取表字段
     *
     * @param id    id
     * @param type  type
     * @param field field
     * @return 执行结果
     */
    @GetMapping("/tableName/get")
    ResultEntity<TableDataDTO> getTableName(
            @RequestParam("id") Integer id,
            @RequestParam("type") DataDoFieldTypeEnum type,
            @RequestParam("field") String field);

    /**
     * 查询聚合条件
     *
     * @param id
     * @return
     */
    @GetMapping("/tableName/getAggregation")
    ResultEntity<String> getAggregation(@RequestParam("id") Integer id);

    /**
     * 根据业务过程获取业务域低
     *
     * @param id
     * @return
     */
    @GetMapping("/businessProcess/getBusinessId/{id}")
    ResultEntity<Object> getBusinessId(@PathVariable("id") int id);

    /**
     * 获取指标
     *
     * @param dto
     * @return
     */
    @PostMapping("/tableName/getIndicatorsLogic")
    ResultEntity<List<IndicatorDTO>> getIndicatorsLogic(@RequestBody IndicatorFeignDTO dto);

    /**
     * 判断维度与维度、事实与维度是否存在关联
     *
     * @param dto
     * @return
     */
    @PostMapping("/dataService/isExistAssociate")
    ResultEntity<Boolean> isExistAssociate(@Validated @RequestBody IsDimensionDTO dto);

    /**
     * 根据派生指标id,获取该业务域下日期维度以及字段
     *
     * @param id
     * @return
     */
    @GetMapping("/dataService/getDimensionDate/{id}")
    ResultEntity<DimensionTimePeriodDTO> getDimensionDate(@PathVariable("id") int id);

    /**
     * 根据时间维度表名称获取所有字段
     *
     * @param tableName
     * @return
     */
    @GetMapping("/dataService/getDimensionFieldNameList/{tableName}")
    ResultEntity<List<String>> getDimensionFieldNameList(@PathVariable("tableName") String tableName);

    /**
     * 修改维度发布状态
     *
     * @param dto
     */
    @PutMapping("/dimension/updateDimensionPublishStatus")
    void updateDimensionPublishStatus(@RequestBody ModelPublishStatusDTO dto);

    /**
     * 修改事实发布状态
     *
     * @param dto
     */
    @PutMapping("/fact/updateFactPublishStatus")
    void updateFactPublishStatus(@RequestBody ModelPublishStatusDTO dto);

    /**
     * 修改宽表发布状态
     *
     * @param dto
     */
    @PutMapping("/wideTable/updateWideTablePublishStatus")
    void updateWideTablePublishStatus(@RequestBody ModelPublishStatusDTO dto);

    /**
     * 根据appId和tableId 获取appName和tableName
     *
     * @param dto
     * @return
     */
    @PostMapping("/DataFactory/getAppNameAndTableName")
    ResultEntity<Object> getAppNameAndTableName(@RequestBody DataAccessIdsDTO dto);

    /**
     * 获取数据建模表数据
     *
     * @param publishStatus
     * @return
     */
    @GetMapping("/DataManagement/getDataModelTable/{publishStatus}")
    ResultEntity<Object> getDataModelTable(@PathVariable("publishStatus") int publishStatus);

    /**
     * 获取数仓中每个表中的业务元数据配置
     *
     * @param dto
     * @return
     */
    @PostMapping("/DataManagement/setTableRule")
    ResultEntity<TableRuleInfoDTO> setTableRule(@RequestBody TableRuleParameterDTO dto);

    /**
     * 获取白泽数据源
     *
     * @return
     */
    @GetMapping("/Datamation/getAll")
    ResultEntity<List<DataDomainVO>> getAll();

    /**
     * 获取表增量配置信息
     *
     * @param tableId
     * @param tableType
     * @return
     */
    @GetMapping("/TableBusiness/getTableBusiness")
    ResultEntity<GetTableBusinessDTO> getTableBusiness(@RequestParam("tableId") int tableId, @RequestParam("tableType") int tableType);

    /**
     * 根据维度id获取维度字段及其关联详情(nifi)
     *
     * @param dimensionId
     * @return
     */
    @GetMapping("/attribute/selectDimensionAttributeList")
    ResultEntity<List<ModelPublishFieldDTO>> selectDimensionAttributeList(@RequestParam("dimensionId") int dimensionId);


    /**
     * 根据事实id获取事实字段及其关联详情(nifi)
     *
     * @param factId
     * @return
     */
    @GetMapping("/factAttribute/selectAttributeList")
    ResultEntity<List<ModelPublishFieldDTO>> selectAttributeList(@RequestParam("factId") int factId);

    /**
     * 根据不同类型获取数仓对应的表
     *
     * @param dto dto
     * @return 结果
     */
    @PostMapping("/DataFactory/getTableId")
    ResultEntity<List<ChannelDataDTO>> getTableId(@RequestBody NifiComponentsDTO dto);

    /**
     * 获取业务域下维度/事实表
     *
     * @param dto
     * @return
     */
    @PostMapping("/business/getBusinessAreaTable")
    ResultEntity<Page<PipelineTableLogVO>> getBusinessAreaTable(@RequestBody PipelineTableQueryDTO dto);

    /**
     * 根据业务id、表类型、表id,获取表详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/business/getBusinessAreaTableDetail")
    ResultEntity<BusinessAreaTableDetailDTO> getBusinessAreaTableDetail(@RequestBody BusinessAreaQueryTableDTO dto);

    /**
     * 刷新数据建模结构
     *
     * @param dto
     * @return
     */
    @PostMapping("/business/setDataStructure")
    ResultEntity<Object> setDataModelStructure(@RequestBody FiDataMetaDataReqDTO dto);

    @PostMapping("/business/getFiDataTableMetaData")
    @ApiOperation(value = "根据表信息/字段ID,获取表/字段基本信息")
    ResultEntity<List<FiDataTableMetaDataDTO>> getFiDataTableMetaData(@RequestBody FiDataTableMetaDataReqDTO dto);

    /**
     * 获取业务域下拉列表
     *
     * @return
     */
    @GetMapping("/business/getBusinessAreaList")
    @ApiOperation(value = "获取业务域下拉列表")
    ResultEntity<List<AppBusinessInfoDTO>> getBusinessAreaList();

    /**
     * 根据维度名称获取维度文件夹详情
     *
     * @param tableName
     * @return
     */
    @PostMapping("/dimensionFolder/getDimensionFolderByTableName")
    ResultEntity<DimensionFolderDTO> getDimensionFolderByTableName(@Validated @RequestBody String tableName);

    /**
     * 根据表名获取接入表信息
     *
     * @param tableName
     * @return
     */
    @ApiOperation("根据表名获取接入表信息")
    @PostMapping("/DataOps/getTableInfo")
    ResultEntity<DataModelTableInfoDTO> getTableInfo(@Validated @RequestBody String tableName);

}
