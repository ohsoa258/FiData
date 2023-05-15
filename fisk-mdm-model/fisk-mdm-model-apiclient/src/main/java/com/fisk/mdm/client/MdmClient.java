package com.fisk.mdm.client;

import com.fisk.common.core.constants.SystemConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.mdm.dto.accessmodel.AccessPublishStatusDTO;
import com.fisk.mdm.dto.attribute.AttributeDomainDTO;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.attribute.AttributeStatusDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.dto.process.ApprovalDTO;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import com.fisk.mdm.vo.entity.EntityVO;
import com.fisk.mdm.vo.model.ModelInfoVO;
import com.fisk.task.dto.accessmdm.AccessAttributeDTO;
import com.fisk.task.dto.mdmconfig.AccessMdmConfigDTO;
import io.swagger.annotations.ApiOperation;
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
     * 根据实体id获取属性
     * @param id
     * @return
     */
    @GetMapping("/model/getEntityById")
    ResultEntity<ModelInfoVO> getEntityById(@RequestParam("id") Integer id);

    /**
     * 审批
     */
    @PostMapping("/process/approval")
    public ResultEntity<ResultEnum> approval(@RequestBody ApprovalDTO dto);

    /**
     * 审批
     */
    @PostMapping("/process/executeApproval")
    ResultEntity<ResultEnum> executeApproval(@RequestBody List<ApprovalDTO> dataDto,@RequestHeader(name = SystemConstants.HTTP_HEADER_AUTH) String token);

    /**
     * 根据id查询查询属性
     * @param id
     * @return
     */
    @GetMapping("/attribute/get")
    ResultEntity<AttributeVO> get(@RequestParam("id") Integer id);

    /**
     * 更新发布状态
     * @param dto
     */
    @PutMapping("/access/updateAccessPublishState")
    void updateAccessPublishState( @RequestBody AccessPublishStatusDTO dto);

    /**
     * 获取接入表默认预览sql
     * @param moudleId
     * @param entityId
     * @return
     */
    @GetMapping("/access/getAccessDefaultSql")
    ResultEntity<Object> getAccessDefaultSql(@RequestParam("moudleId")Integer moudleId,@RequestParam("entityId")Integer entityId);

    /**
     * 获取接入字段映射关系
     * @param accessId
     * @param entityId
     * @return
     */
    @GetMapping("/access/getAccessAttributeField")
    ResultEntity<List<AccessAttributeDTO>> getAccessAttributeField(@RequestParam("accessId")Integer accessId,@RequestParam("entityId") Integer entityId);
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
    ResultEntity<List<FiDataMetaDataDTO>> getMDMDataStructure(@RequestBody FiDataMetaDataReqDTO dto);

    /**
     * 刷新主数据结构
     * @return
     */
    @PostMapping("/model/setDataStructure")
    ResultEntity<Object> setMDMDataStructure(@RequestBody FiDataMetaDataReqDTO dto);

    /**
     * 数据访问配置
     * @param entityId
     * @param modelId
     * @return
     */
    @GetMapping("/access/dataAccessConfig")
    ResultEntity<AccessMdmConfigDTO> dataAccessConfig(@RequestParam("entityId") long entityId, @RequestParam("modelId") long modelId);

    /**
     * 获取所有实体表id
     *
     * @return list
     */
    @GetMapping("/access/getTableId")
    ResultEntity<List<ChannelDataDTO>> getTableId();
}
