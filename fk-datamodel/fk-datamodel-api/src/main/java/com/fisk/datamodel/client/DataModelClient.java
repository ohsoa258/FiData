package com.fisk.datamodel.client;

import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.dataservice.dto.TableDataDTO;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author JianWenYang
 */
@FeignClient("datamodel-service")
public interface DataModelClient {

    /**
     * 获取维度表元数据字段
     *
     * @param id
     * @return 执行结果
     */
    @GetMapping("/attribute/getDimensionEntity")
    ResultEntity<ModelMetaDataDTO> getDimensionEntity(@RequestParam("id") int id);

    /**
     * 获取事实表元数据字段
     *
     * @param id
     * @return 执行结果
     */
    @GetMapping("/factAttribute/getFactEntity")
    ResultEntity<ModelMetaDataDTO> getFactEntity(@RequestParam("id") int id);

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

}
