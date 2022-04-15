package com.fisk.mdm.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.mdm.dto.attribute.AttributeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/4/15 15:37
 */
@FeignClient("mdmmodel-service")
public interface MdmClient {

    /**
     * 根据实体id获取属性
     * @param id
     * @return
     */
    @GetMapping("/getAttributeById")
    ResultEntity<List<AttributeDTO>> getAttributeById(Integer id);
}
