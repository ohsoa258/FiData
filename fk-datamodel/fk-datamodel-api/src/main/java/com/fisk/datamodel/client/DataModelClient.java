package com.fisk.datamodel.client;

import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.dto.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.atomicindicator.AtomicIndicatorPushDTO;
import com.fisk.datamodel.dto.atomicindicator.DimensionTimePeriodDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.dataservice.dto.IndicatorDTO;
import com.fisk.dataservice.dto.IndicatorFeignDTO;
import com.fisk.dataservice.dto.TableDataDTO;
import com.fisk.dataservice.dto.isDimensionDTO;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
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
     * @param id
     * @return
     */
    @GetMapping("/AtomicIndicators/getAtomicIndicators")
    ResultEntity<Object> getAtomicIndicators(@RequestParam("id") int id);

    /**
     *根据同步方式id获取相关数据
     * @param id
     * @return
     */
    @GetMapping("/FactSyncMode/factSyncModePush")
    ResultEntity<Object> factSyncModePush(@PathVariable("id") int id);

    /**
     * 根据业务域id获取相关维度以及原子指标
     * @param id
     * @return
     */
    @GetMapping("/business/getBusinessAreaPublicData")
    ResultEntity<BusinessAreaGetDataDTO> getBusinessAreaPublicData(@RequestParam("id") int id);

    /**
     * 获取表字段
     *
     * @param id   id
     * @param type type
     * @return 执行结果
     */
    @GetMapping("/tableName/get")
    ResultEntity<TableDataDTO> getTableName(
            @RequestParam("id") Integer id,
            @RequestParam("type") DataDoFieldTypeEnum type,
            @RequestParam("field") String field);

    /**
     * 查询聚合条件
     * @param id
     * @return
     */
    @GetMapping("/tableName/getAggregation")
    ResultEntity<String> getAggregation(@RequestParam("id") Integer id);

    /**
     *根据业务过程获取业务域低
     * @param id
     * @return
     */
    @GetMapping("/businessProcess/getBusinessId/{id}")
    ResultEntity<Object> getBusinessId(@PathVariable("id")int id);

    /**
     * 获取指标
     * @param dto
     * @return
     */
    @PostMapping("/tableName/getIndicatorsLogic")
    ResultEntity<List<IndicatorDTO>> getIndicatorsLogic(@RequestBody IndicatorFeignDTO dto);

    /**
     * 判断维度与维度、事实与维度是否存在关联
     * @param dto
     * @return
     */
    @PostMapping("/dataService/isExistAssociate")
    ResultEntity<Boolean> isExistAssociate(@Validated @RequestBody isDimensionDTO dto);

    /**
     * 根据派生指标id,获取该业务域下日期维度以及字段
     * @param id
     * @return
     */
    @GetMapping("/dataService/getDimensionDate/{id}")
    ResultEntity<DimensionTimePeriodDTO> getDimensionDate(@PathVariable("id") int id);

    /**
     * 修改维度发布状态
     * @param dto
     */
    @PostMapping("/dimension/updateDimensionPublishStatus")
    void updateDimensionPublishStatus(@Validated @RequestBody ModelPublishStatusDTO dto);

    /**
     * 修改事实发布状态
     * @param dto
     */
    @PostMapping("/fact/updateFactPublishStatus")
    void updateFactPublishStatus(@Validated @RequestBody ModelPublishStatusDTO dto);



}
