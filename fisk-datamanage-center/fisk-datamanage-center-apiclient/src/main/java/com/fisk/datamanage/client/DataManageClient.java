package com.fisk.datamanage.client;

import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.metadata.BusinessMetaDataInfoDTO;
import com.fisk.common.server.metadata.ClassificationInfoDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDeleteAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataEntityDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.datamanagement.dto.classification.BusinessExtendedfieldsDTO;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoDTO;
import com.fisk.datamanagement.dto.classification.FacttreeListDTO;
import com.fisk.datamanagement.dto.datamasking.DataMaskingSourceDTO;
import com.fisk.datamanagement.dto.datamasking.DataMaskingTargetDTO;
import com.fisk.datamanagement.dto.datamasking.SourceTableDataDTO;
import com.fisk.datamanagement.dto.dataquality.DataQualityDTO;
import com.fisk.datamanagement.dto.dataquality.UpperLowerBloodParameterDTO;
import com.fisk.datamanagement.dto.metadataentity.MetadataEntityDTO;
import com.fisk.datamanagement.dto.metadataentityoperationLog.MetaDataEntityOperationLogDTO;
import com.fisk.datamanagement.dto.modelAndIndex.ModelAndIndexMappingDTO;
import com.fisk.datamanagement.dto.standards.StandardsBeCitedDTO;
import com.fisk.datamanagement.dto.standards.StandardsDTO;
import com.fisk.datamanagement.dto.standards.StandardsMenuDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author JianWenYang
 */
@FeignClient("datamanagement-service")
public interface DataManageClient {

    /**
     * 数据源是否存在atlas
     *
     * @param dto
     * @return
     */
    @PostMapping("/DataQuality/existAtlas")
    ResultEntity<Object> existAtlas(@Validated @RequestBody DataQualityDTO dto);

    /**
     * 是否存在上下血缘
     *
     * @param dto
     * @return
     */
    @PostMapping("/DataQuality/existUpperLowerBlood")
    ResultEntity<Object> existUpperLowerBlood(@Validated @RequestBody UpperLowerBloodParameterDTO dto);

    /**
     * 数据脱敏，根据guid提供数据源信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/DataMasking/getSourceDataConfig")
    ResultEntity<DataMaskingTargetDTO> getSourceDataConfig(@Validated @RequestBody DataMaskingSourceDTO dto);

    /**
     * 根据guid获取数据工厂表信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/DataMasking/getTableData")
    ResultEntity<Object> getTableData(@Validated @RequestBody SourceTableDataDTO dto);

    /**
     * 元数据实时同步
     *
     * @param dto
     * @return
     */
    @PostMapping("/MetaData/metaData")
    ResultEntity<Object> metaData(@RequestBody MetaDataAttributeDTO dto);

    /**
     * 添加元数据实体
     *
     * @param dto
     * @return
     */
    @PostMapping("/MetaData/consumeMetaData")
    ResultEntity<Object> consumeMetaData(@Validated @RequestBody List<MetaDataInstanceAttributeDTO> dto);

    /**
     * 同步表级业务元数据
     *
     * @param dto
     */
    @PostMapping("/BusinessMetaData/synchronousTableBusinessMetaData")
    void synchronousTableBusinessMetaData(@Validated @RequestBody BusinessMetaDataInfoDTO dto);

    /**
     * 删除元数据实体
     *
     * @param dto
     * @return
     */
    @DeleteMapping("/MetaData/deleteMetaData")
    ResultEntity<Object> deleteMetaData(@Validated @RequestBody MetaDataDeleteAttributeDTO dto);

    /**
     * 单个元数据信息消费
     *
     * @param dto
     * @return
     */
    @PostMapping("/MetaData/addFiledAndUpdateFiled")
    ResultEntity<Object> addFiledAndUpdateFiled(@Validated @RequestBody List<MetaDataInstanceAttributeDTO> dto, @RequestParam ClassificationTypeEnum classificationTypeEnum);

    /**
     * 数据接入应用同步到业务分类
     *
     * @param dto
     * @return
     */
    @PostMapping("/Classification/appSynchronousClassification")
    ResultEntity<Object> appSynchronousClassification(@Validated @RequestBody ClassificationInfoDTO dto);

    /**
     * 日志记录
     *
     * @param dto
     * @return
     */
    @PostMapping("/OperateLog/addOperateLog")
    ResultEntity<Object> saveLog(@RequestBody MetaDataEntityOperationLogDTO dto);

    /**
     * 根据IDS删除字段
     *
     * @param ids
     * @return
     */
    @RequestMapping("/MetaData/fieldDelete")
    ResultEntity<Object> fieldDelete(@RequestParam("ids") List<Integer> ids);

    /**
     * 根据数据接入表ID和字段ID
     *
     * @param tableId
     * @param fldeId
     * @return
     */
    @GetMapping("/MetaData/queryMetadaFildes/{tableId}/{fldeId}")
    List<MetadataEntityDTO> queryMetadaFildes(@PathVariable("tableId") Integer tableId, @PathVariable("fldeId") Integer fldeId);


    /**
     * 元数据实时同步
     *
     * @param entityList
     * @return
     */
    @PostMapping("/MetaData/syncDataConsumptionMetaData")
    ResultEntity<Object> syncDataConsumptionMetaData(@RequestBody List<MetaDataEntityDTO> entityList);

    /**
     * 元数据实时同步
     *
     * @param entityList
     * @return
     */
    @PostMapping("/MetaData/deleteConsumptionMetaData")
    ResultEntity<Object> deleteConsumptionMetaData(@RequestBody List<MetaDataEntityDTO> entityList);

    @PostMapping("/Standards/getAllStandardsTree")
    ResultEntity<List<FiDataMetaDataTreeDTO>> getAllStandardsTree(@RequestParam("id") String id);

    /**
     * 数仓建模-关联字段和数据源标准
     *
     * @param dtos
     * @return
     */
    @PostMapping("/Standards/setStandardsByModelField")
    ResultEntity<Object> setStandardsByModelField(@RequestBody List<StandardsBeCitedDTO> dtos);

    /**
     * 关联数仓表字段和指标标准（维度表字段 指标粒度）
     * 维度表字段则关联 指标粒度
     * 事实表字段则关联 指标所属
     *
     * @param dtos
     * @return
     */
    @PutMapping("/BusinessCategory/setMetricGranularityByModelField")
    ResultEntity<Object> setMetricGranularityByModelField(@RequestBody List<ModelAndIndexMappingDTO> dtos);

    /**
     * 关联数仓表字段和指标标准（事实表字段 指标所属）
     * 维度表字段则关联 指标粒度
     * 事实表字段则关联 指标所属
     *
     * @param dtos
     * @return
     */
    @PutMapping("/BusinessCategory/setMetricBelongsByModelField")
    ResultEntity<Object> setMetricBelongsByModelField(@RequestBody List<ModelAndIndexMappingDTO> dtos);

    /**
     * 数仓建模获取所有业务指标 只获取id 名称
     *
     * @return
     */
    @GetMapping("/BusinessCategory/modelGetBusinessTargetInfoList")
    List<BusinessTargetinfoDTO> modelGetBusinessTargetInfoList();

    /**
     * 获取数仓字段和指标所属表里所有关联关系 只获取字段id 和指标id
     *
     * @return
     */
    @ApiOperation("获取数仓字段和指标关联表里所有关联关系 只获取字段id 和指标id")
    @GetMapping("/BusinessCategory/modelGetFactTreeList")
    List<FacttreeListDTO> modelGetFactTreeList(@RequestParam("tblId")Integer tblId);

    /**
     * 数仓建模-获取所有数据元标准 只获取数据元id 和中文名
     *
     * @return
     */
    @ApiOperation("数仓建模-获取所有数据元标准 只获取数据元id 和中文名")
    @GetMapping("/Standards/modelGetStandards")
    List<StandardsDTO> modelGetStandards();

    @ApiOperation("获取所有数据元标准menu-只要id和name")
    @GetMapping("/Standards/getStandardMenus")
    List<StandardsMenuDTO> getStandardMenus();

    @ApiOperation("获取数据标准")
    @GetMapping("/Standards/getStandards/{id}")
    ResultEntity<StandardsDTO> getStandards(@PathVariable("id") int id);

    /**
     * 数仓建模-获取所有数仓字段和数据元标准的关联关系 只获取字段id 和数据元标准id
     *
     * @return
     */
    @ApiOperation("数仓建模-获取所有数仓字段和数据元标准的关联关系")
    @GetMapping("/Standards/modelGetStandardsMap")
    List<StandardsBeCitedDTO> modelGetStandardsMap();

    /**
     * 数仓建模-获取所有数仓字段和数据元标准的关联关系 只获取字段id 和数据元标准id
     *
     * @return
     */
    @ApiOperation("主数据-获取所有主数据字段和数据元标准的关联关系")
    @GetMapping("/Standards/mdmGetStandardsMap")
    List<StandardsBeCitedDTO> mdmGetStandardsMap();


    /**
     * 获取数仓字段和指标所属表里所有关联关系 只获取字段id 和指标id
     *
     * @return
     */
    @ApiOperation("获取数仓字段和指标关联表里所有关联关系 只获取字段id 和指标id")
    @GetMapping("/BusinessCategory/modelGetMetricMapList")
    List<BusinessExtendedfieldsDTO> modelGetMetricMapList();

    /**
     * 根据数据元标准menuId获取所有standardsId(数据校验用)
     * @param menuId
     * @return
     */
    @ApiOperation("根据数据元标准menuId获取所有standardsId(数据校验用)")
    @GetMapping("/Standards/getStandardByMenuId")
    List<Integer> getStandardByMenuId(@RequestParam("menuId")Integer menuId);

    /**
     * 获指标标准数量
     * @return
     */
    @ApiOperation("获指标标准数量")
    @GetMapping("/BusinessCategory/getBusinessTargetinfoTotal")
    public ResultEntity<Object> getBusinessTargetinfoTotal();

    /**
     * 获取数据元标准数量
     * @return
     */
    @ApiOperation("获取数据元标准数量")
    @GetMapping("/Standards/getStandardTotal")
    ResultEntity<Object> getStandardTotal();

    /**
     * 获取业务术语数量
     * @return
     */
    @ApiOperation("获取业务术语数量")
    @GetMapping("/Glossary/getGlossaryTotal")
    ResultEntity<Object> getGlossaryTotal();
}
