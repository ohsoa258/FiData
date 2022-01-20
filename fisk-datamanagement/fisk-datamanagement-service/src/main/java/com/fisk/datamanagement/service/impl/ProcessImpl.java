package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.dto.entity.EntityAttributesDTO;
import com.fisk.datamanagement.dto.entity.EntityDTO;
import com.fisk.datamanagement.dto.entity.EntityIdAndTypeDTO;
import com.fisk.datamanagement.dto.entity.EntityTypeDTO;
import com.fisk.datamanagement.dto.process.AddProcessDTO;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.service.IProcess;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JianWenYang
 */
@Service
public class ProcessImpl implements IProcess {

    @Resource
    AtlasClient atlasClient;

    @Value("${atlas.entity}")
    private String entity;
    @Value("${atlas.entityByGuid}")
    private String entityByGuid;

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

}
