package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.dto.entity.EntityAttributesDTO;
import com.fisk.datamanagement.dto.entity.EntityDTO;
import com.fisk.datamanagement.dto.entity.EntityIdAndTypeDTO;
import com.fisk.datamanagement.dto.entity.EntityTypeDTO;
import com.fisk.datamanagement.dto.process.*;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.service.IProcess;
import com.fisk.datamanagement.synchronization.fidata.SynchronizationPgKinShip;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import org.omg.PortableServer.LIFESPAN_POLICY_ID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class ProcessImpl implements IProcess {

    @Resource
    AtlasClient atlasClient;
    @Resource
    SynchronizationPgKinShip synchronizationPgKinShip;

    @Value("${atlas.entity}")
    private String entity;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;

    @Override
    public ProcessDTO getProcess(String processGuid)
    {
        ProcessDTO dto=new ProcessDTO();
        ResultDataDTO<String> getDetail = atlasClient.Get(entityByGuid + "/" + processGuid);
        if (getDetail.code !=ResultEnum.REQUEST_SUCCESS)
        {
            return dto;
        }
        //序列化数据
        dto=JSONObject.parseObject(getDetail.data,ProcessDTO.class);
        //过滤输入已删除实体或血缘连线
        List<String> delInputGuidList=dto.entity.relationshipAttributes.inputs
                .stream()
                .filter(e->EntityTypeEnum.DELETED.getName().equals(e.entityStatus)
                        || EntityTypeEnum.DELETED.getName().equals(e.relationshipStatus))
                .map(e->e.getGuid()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(delInputGuidList))
        {
            dto.entity.relationshipAttributes.inputs=dto.entity.relationshipAttributes.inputs.stream()
                    .filter(e->!delInputGuidList.contains(e.guid)).collect(Collectors.toList());
            dto.entity.attributes.inputs=dto.entity.attributes.inputs
                    .stream()
                    .filter(e->!delInputGuidList.contains(e.guid)).collect(Collectors.toList());
        }
        //过滤输出已删除实体或血缘连线
        List<String> delOutGuidList=dto.entity.relationshipAttributes.outputs
                .stream()
                .filter(e->EntityTypeEnum.DELETED.getName().equals(e.entityStatus)
                        || EntityTypeEnum.DELETED.getName().equals(e.relationshipStatus))
                .map(e->e.getGuid()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(delOutGuidList))
        {
            dto.entity.relationshipAttributes.outputs=dto.entity.relationshipAttributes.outputs.stream()
                    .filter(e->!delInputGuidList.contains(e.guid)).collect(Collectors.toList());
            dto.entity.attributes.outputs=dto.entity.attributes.outputs
                    .stream()
                    .filter(e->!delInputGuidList.contains(e.guid)).collect(Collectors.toList());
        }
        //获取process输出血缘

        return dto;
    }

    @Override
    public ResultEnum addProcess(AddProcessDTO dto)
    {
        //获取实体详情
        ResultDataDTO<String> getDetail = atlasClient.Get(entityByGuid + "/" + dto.outGuid);
        if (getDetail.code !=ResultEnum.REQUEST_SUCCESS)
        {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //解析数据
        JSONObject jsonObj = JSON.parseObject(getDetail.data);
        JSONObject entityObject= JSON.parseObject(jsonObj.getString("entity"));
        JSONObject relationShip=JSON.parseObject(entityObject.getString("relationshipAttributes"));
        JSONArray relationShipAttribute=JSON.parseArray(relationShip.getString("outputFromProcesses"));
        //条数为0,则添加process
        if (relationShipAttribute.size()==0)
        {
            return process(dto);
        }
        return ResultEnum.SUCCESS;
    }

    public ResultEnum process(AddProcessDTO dto)
    {
        //组装参数
        EntityDTO entityDTO=new EntityDTO();
        EntityTypeDTO entityTypeDTO=new EntityTypeDTO();
        entityTypeDTO.typeName= EntityTypeEnum.PROCESS.getName();
        EntityAttributesDTO attributesDTO=new EntityAttributesDTO();
        attributesDTO.comment=dto.processName;
        attributesDTO.description=dto.description;
        attributesDTO.owner="root";
        attributesDTO.qualifiedName="";
        attributesDTO.contact_info=dto.contactInfo;
        attributesDTO.name=dto.processName;
        //输入参数
        attributesDTO.inputs=dto.inputList;
        //输出参数
        List<EntityIdAndTypeDTO> dtoList=new ArrayList<>();
        EntityIdAndTypeDTO out=new EntityIdAndTypeDTO();
        out.typeName=dto.entityTypeEnum.getName().toLowerCase();
        out.guid=dto.outGuid;
        dtoList.add(out);
        attributesDTO.outputs=dtoList;
        entityTypeDTO.attributes=attributesDTO;
        //检验输入和输出参数是否有值
        if (CollectionUtils.isEmpty(attributesDTO.inputs) || CollectionUtils.isEmpty(attributesDTO.outputs))
        {
            return ResultEnum.PARAMTER_ERROR;
        }
        entityDTO.entity=entityTypeDTO;
        String jsonParameter= JSONArray.toJSON(entityDTO).toString();
        //调用atlas添加血缘
        ResultDataDTO<String> addResult = atlasClient.Post(entity, jsonParameter);
        return addResult.code==ResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateProcess(ProcessDTO dto)
    {
        //判断input输入guid
        List<String> inputGuid=dto.entity.attributes.inputs.stream().map(e->e.getGuid()).collect(Collectors.toList());
        List<String> outGuid=dto.entity.relationshipAttributes.inputs.stream().map(e->e.getGuid()).collect(Collectors.toList());
        //获取差集
        inputGuid.removeAll(outGuid);
        if (!CollectionUtils.isEmpty(inputGuid))
        {
            //循环添加input参数
            for (String guid:inputGuid)
            {
                //获取新增input限定名称信息
                Optional<ProcessAttributesPutDTO> first = dto.entity.attributes.inputs.stream().filter(e -> e.guid.equals(guid)).findFirst();
                if (!first.isPresent())
                {
                    continue;
                }
                //添加血缘关系连线
                String relationShipGuid = synchronizationPgKinShip.addRelationShip(dto.entity.guid,
                        dto.entity.attributes.qualifiedName,
                        guid,
                        first.get().uniqueAttributes.qualifiedName);
                if (relationShipGuid=="")
                {
                    continue;
                }
                ProcessRelationshipAttributesPutDTO inputDTO=new ProcessRelationshipAttributesPutDTO();
                inputDTO.guid=guid;
                inputDTO.typeName=first.get().typeName;
                inputDTO.entityStatus=EntityTypeEnum.ACTIVE.getName();
                //表、字段名称
                inputDTO.displayText=first.get().tableName;
                inputDTO.relationshipType=EntityTypeEnum.DATASET_PROCESS_INPUTS.getName();
                //生成的relationShip
                inputDTO.relationshipGuid=relationShipGuid;
                inputDTO.relationshipStatus=EntityTypeEnum.ACTIVE.getName();
                ProcessRelationShipAttributesTypeNameDTO attributesDTO=new ProcessRelationShipAttributesTypeNameDTO();
                attributesDTO.typeName=EntityTypeEnum.DATASET_PROCESS_INPUTS.getName();
                inputDTO.relationshipAttributes=attributesDTO;
                dto.entity.relationshipAttributes.inputs.add(inputDTO);
            }
        }
        //修改process
        String jsonParameter= JSONArray.toJSON(dto).toString();
        //调用atlas修改实例
        ResultDataDTO<String> result = atlasClient.Post(entity, jsonParameter);
        return result.code==ResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

}
