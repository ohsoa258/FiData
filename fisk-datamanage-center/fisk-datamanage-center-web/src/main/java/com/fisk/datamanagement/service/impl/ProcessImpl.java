package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.entity.EntityAttributesDTO;
import com.fisk.datamanagement.dto.entity.EntityDTO;
import com.fisk.datamanagement.dto.entity.EntityIdAndTypeDTO;
import com.fisk.datamanagement.dto.entity.EntityTypeDTO;
import com.fisk.datamanagement.dto.lineagemaprelation.LineageMapRelationDTO;
import com.fisk.datamanagement.dto.metadataentity.MetadataEntityDTO;
import com.fisk.datamanagement.dto.process.*;
import com.fisk.datamanagement.entity.LineageMapRelationPO;
import com.fisk.datamanagement.entity.MetadataEntityPO;
import com.fisk.datamanagement.entity.MetadataLineageMapPO;
import com.fisk.datamanagement.entity.MetadataMapAtlasPO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.enums.ProcessTypeEnum;
import com.fisk.datamanagement.mapper.LineageMapRelationMapper;
import com.fisk.datamanagement.mapper.MetadataEntityMapper;
import com.fisk.datamanagement.mapper.MetadataLineageMapper;
import com.fisk.datamanagement.mapper.MetadataMapAtlasMapper;
import com.fisk.datamanagement.service.IProcess;
import com.fisk.datamanagement.synchronization.pushmetadata.impl.MetaDataImpl;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Convert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
public class ProcessImpl implements IProcess {

    @Resource
    AtlasClient atlasClient;
    @Resource
    MetaDataImpl metaData;
    @Resource
    MetadataMapAtlasMapper metadataMapAtlasMapper;

    @Resource
    MetadataEntityMapper metadataEntityMapper;

    @Resource
    LineageMapRelationImpl lineageMapRelation;

    @Resource
    EntityImpl entityImpl;

    @Resource
    MetadataEntityImpl metadataEntity;

    @Value("${atlas.entity}")
    private String entity;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;

    @Override
    public ProcessDTO getProcess(String processGuid) {
        ProcessDTO dto = new ProcessDTO();
        /*ResultDataDTO<String> getDetail = atlasClient.get(entityByGuid + "/" + processGuid);
        if (getDetail.code != AtlasResultEnum.REQUEST_SUCCESS) {
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
        if (!CollectionUtils.isEmpty(delInputGuidList)) {
            dto.entity.relationshipAttributes.inputs = dto.entity.relationshipAttributes.inputs.stream()
                    .filter(e -> !delInputGuidList.contains(e.guid)).collect(Collectors.toList());
            dto.entity.attributes.inputs = dto.entity.attributes.inputs
                    .stream()
                    .filter(e -> !delInputGuidList.contains(e.guid)).collect(Collectors.toList());
        }
        //过滤输出已删除实体或血缘连线
        List<String> delOutGuidList=dto.entity.relationshipAttributes.outputs
                .stream()
                .filter(e->EntityTypeEnum.DELETED.getName().equals(e.entityStatus)
                        || EntityTypeEnum.DELETED.getName().equals(e.relationshipStatus))
                .map(e->e.getGuid()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(delOutGuidList)) {
            dto.entity.relationshipAttributes.outputs = dto.entity.relationshipAttributes.outputs.stream()
                    .filter(e -> !delInputGuidList.contains(e.guid)).collect(Collectors.toList());
            dto.entity.attributes.outputs = dto.entity.attributes.outputs
                    .stream()
                    .filter(e -> !delInputGuidList.contains(e.guid)).collect(Collectors.toList());

        }*/

        //根据ID查询抽取详情以及上游来源id集合+下游id集合
        MetadataEntityDTO metadataEntityPO = metadataEntityMapper.getProcess(processGuid);
        if(metadataEntityPO!=null){
            //数据传输
            ProcessEntityDTO processEntityDTO = new ProcessEntityDTO();

            //抽取详情封装
            ProcessAttributesDTO processAttributesDTO = new ProcessAttributesDTO();

            processAttributesDTO.name=metadataEntityPO.name;
            processAttributesDTO.description=metadataEntityPO.description;

            //定义接收单个来源id和输出源id
            ProcessAttributesPutDTO input=null;
            ProcessAttributesPutDTO out=null;

            //定义接收来源和输出来源id集合
            List<ProcessAttributesPutDTO> inputs=new ArrayList<>();
            List<ProcessAttributesPutDTO> outputs=new ArrayList<>();

            for ( LineageMapRelationDTO relationDTO : metadataEntityPO.getRelationDTOList()) {
                //使用时创建
                input=new ProcessAttributesPutDTO();
                out=new ProcessAttributesPutDTO();
                input.guid=relationDTO.getFromEntityId().toString();
                inputs.add(input);
                out.guid=relationDTO.getToEntityId().toString();
                outputs.add(out);
            }
            //输入源去重保存
            processAttributesDTO.setInputs(inputs.stream().distinct().collect(Collectors.toList()));
            //输出源去重保存
            processAttributesDTO.setOutputs(outputs.stream().distinct().collect(Collectors.toList()));
            processEntityDTO.attributes=processAttributesDTO;

            dto.entity=processEntityDTO;
        }

        return dto;
    }

    @Override
    public ResultEnum addProcess(AddProcessDTO dto)
    {
        //创建中间流程节点
        MetadataEntityPO metadataEntityPO=new MetadataEntityPO();
        metadataEntityPO.setName(dto.processName);
        metadataEntityPO.setDescription(dto.description);
        metadataEntityPO.setQualifiedName(dto.description);
        metadataEntityPO.setTypeId(7);
        metadataEntityMapper.insert(metadataEntityPO);
        //创建流程连线
        List<LineageMapRelationPO> lineageMapRelationPOS=new ArrayList<>();
        for(EntityIdAndTypeDTO entityIdAndTypeDTO: dto.inputList){
            LineageMapRelationPO lineageMapRelationPO =new LineageMapRelationPO();
            lineageMapRelationPO.setMetadataEntityId((int)metadataEntityPO.id);
            lineageMapRelationPO.setFromEntityId(Integer.parseInt(entityIdAndTypeDTO.guid));
            lineageMapRelationPO.setToEntityId(Integer.parseInt(dto.outGuid));
            lineageMapRelationPO.setProcessType(ProcessTypeEnum.SQL_PROCESS.getValue());
            lineageMapRelationPOS.add(lineageMapRelationPO);
        }

        lineageMapRelation.saveBatch(lineageMapRelationPOS);
        return ResultEnum.SUCCESS;
    }



    @Override
    public ResultEnum updateProcess(EditProcessDto dto)
    {
        LambdaUpdateWrapper<MetadataEntityPO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MetadataEntityPO::getId,dto.guid)
                .set(MetadataEntityPO::getName,dto.getProcessName())
                .set(MetadataEntityPO::getDisplayName,dto.getDescription());
        metadataEntity.update(updateWrapper);

        LambdaQueryWrapper<LineageMapRelationPO> deleteWrapper=new LambdaQueryWrapper<>();
        deleteWrapper.eq(LineageMapRelationPO::getMetadataEntityId,dto.guid);
        lineageMapRelation.remove(deleteWrapper);

        List<LineageMapRelationPO> lineageMapRelationPOS=new ArrayList<>();
        for(String inputId: dto.inputList){
            LineageMapRelationPO lineageMapRelationPO =new LineageMapRelationPO();
            lineageMapRelationPO.setMetadataEntityId(Integer.parseInt(dto.getGuid()));
            lineageMapRelationPO.setFromEntityId(Integer.parseInt(inputId));
            lineageMapRelationPO.setToEntityId(Integer.parseInt(dto.outGuid));
            lineageMapRelationPO.setProcessType(ProcessTypeEnum.SQL_PROCESS.getValue());
            lineageMapRelationPOS.add(lineageMapRelationPO);
        }

        lineageMapRelation.saveBatch(lineageMapRelationPOS);

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteProcess(String guid) {
        List<String> guidList = new ArrayList<>();
        if (StringUtils.isEmpty(guid)) {
            QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("atlas_guid").lambda().eq(MetadataMapAtlasPO::getType, EntityTypeEnum.PROCESS);
            guidList = (List) metadataMapAtlasMapper.selectObjs(queryWrapper);
        } else {
            guidList.add(guid);
        }
        for (String item : guidList) {
            entityImpl.deleteEntity(item);
        }
        return ResultEnum.SUCCESS;
    }

}
