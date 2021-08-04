package com.fisk.datamodel.client;

import com.fisk.common.response.ResultEntity;
import com.fisk.datamodel.dto.dimension.DimensionMetaDataDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author JianWenYang
 */
@FeignClient("datamodel-service")
public interface DimensionClient {

    /**
     * 获取维度表元数据字段
     *
     * @param id
     * @return 执行结果
     */
    @GetMapping("/attribute/getDimensionEntity")
    ResultEntity<DimensionMetaDataDTO> getDimensionEntity(@RequestParam("id") int id);

}
