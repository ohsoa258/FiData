package com.fisk.mdm.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.attribute.AttributeDTO;
import com.fisk.mdm.dto.attribute.AttributeUpdateDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
    @GetMapping("/entity/getAttributeById")
    ResultEntity<EntityVO> getAttributeById(@RequestParam("id") Integer id);

    /**
     * 根据id查询查询属性
     * @param id
     * @return
     */
    @GetMapping("/attribute/get")
    ResultEntity<AttributeVO> get(@RequestParam("id") Integer id);

    /**
     * 根据id查询查询属性
     * @param attributeUpdateDTO
     * @return
     */
    @PostMapping("/attribute/update")
    ResultEntity<ResultEnum> update(@RequestBody AttributeUpdateDTO attributeUpdateDTO);

    /**
     * 修改实体
     * @param dto
     * @return
     */
    @PostMapping("/entity/updateData")
    ResultEntity<ResultEnum> updateData(@RequestBody UpdateEntityDTO dto);
}
