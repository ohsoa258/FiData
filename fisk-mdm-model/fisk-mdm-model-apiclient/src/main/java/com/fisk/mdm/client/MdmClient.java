package com.fisk.mdm.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.attribute.AttributeUpdateDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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
    ResultEntity<EntityInfoVO> getAttributeById(@RequestParam("id") Integer id);

    /**
     * 根据id查询查询属性
     * @param id
     * @return
     */
    @GetMapping("/attribute/get")
    ResultEntity<AttributeVO> get(@RequestParam("id") Integer id);

    /**
     * 修改属性
     * @param attributeUpdateDTO
     * @return
     */
    @PutMapping("/attribute/update")
    ResultEntity<ResultEnum> update(@RequestBody AttributeUpdateDTO attributeUpdateDTO);

    /**
     * 修改实体
     * @param dto
     * @return
     */
    @PutMapping("/entity/update")
    ResultEntity<ResultEnum> update(@RequestBody UpdateEntityDTO dto);

    /**
     * 根据id集合查询属性信息
     * @param ids
     * @return
     */
    @PostMapping("/attribute/getByIds")
    ResultEntity<List<AttributeVO>> getByIds(@RequestBody List<Integer> ids);
}
