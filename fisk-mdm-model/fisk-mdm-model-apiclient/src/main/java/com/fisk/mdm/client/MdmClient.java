package com.fisk.mdm.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.mdm.dto.attribute.AttributeDomainDTO;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.attribute.AttributeStatusDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import com.fisk.mdm.vo.entity.EntityVO;
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
    ResultEntity<EntityInfoVO> getAttributeById(@RequestParam("id") Integer id,@RequestParam("name") String name);

    /**
     * 根据id查询查询属性
     * @param id
     * @return
     */
    @GetMapping("/attribute/get")
    ResultEntity<AttributeVO> get(@RequestParam("id") Integer id);

    /**
     * 修改属性状态
     * @param statusDto
     * @return
     */
    @PutMapping("/attribute/updateStatus")
    ResultEntity<ResultEnum> updateStatus(@RequestBody AttributeStatusDTO statusDto);

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
    ResultEntity<List<AttributeInfoDTO>> getByIds(@RequestBody List<Integer> ids);

    /**
     * 根据domainId查询属性
     * @param dto
     * @return
     */
    @PostMapping("/attribute/getByDomainId")
    ResultEntity<AttributeInfoDTO> getByDomainId(@RequestBody AttributeDomainDTO dto);

    /**
     * 删除属性
     * @param id
     * @return
     */
    @DeleteMapping("/attribute/delete")
    ResultEntity<ResultEnum> delete(@RequestParam("id") Integer id);

    /**
     * 根据id获取实体
     * @param id
     * @return
     */
    @GetMapping("/entity/getDataById")
    ResultEntity<EntityVO> getDataById(@RequestParam("id") Integer id);

    /**
     * 获取主数据结构
     * @return
     */
    @PostMapping("/model/getDataStructure")
    ResultEntity<List<FiDataMetaDataDTO>> getMDMDataStructure();

    /**
     * 刷新主数据结构
     * @return
     */
    @PostMapping("/model/setDataStructure")
    ResultEntity<Object> setMDMDataStructure();
}
