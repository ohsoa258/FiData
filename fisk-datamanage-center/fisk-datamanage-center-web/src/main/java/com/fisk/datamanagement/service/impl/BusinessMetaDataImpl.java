package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.businessmetadata.BusinessMetaDataAttributeDefsDTO;
import com.fisk.datamanagement.dto.businessmetadata.BusinessMetaDataDTO;
import com.fisk.datamanagement.dto.businessmetadata.BusinessMetaDataOptionsDTO;
import com.fisk.datamanagement.dto.businessmetadata.BusinessMetadataDefsDTO;
import com.fisk.datamanagement.entity.BusinessMetadataConfigPO;
import com.fisk.datamanagement.mapper.BusinessMetadataConfigMapper;
import com.fisk.datamanagement.service.IBusinessMetaData;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class BusinessMetaDataImpl implements IBusinessMetaData {

    @Resource
    AtlasClient atlasClient;
    @Resource
    BusinessMetadataConfigMapper mapper;

    @Value("${atlas.typedefs}")
    private String typedefs;
    @Value("${atlas.delTypeDefs}")
    private String delTypeDefs;

    @Override
    public BusinessMetaDataDTO getBusinessMetaDataList() {
        BusinessMetaDataDTO data;
        try {
            ResultDataDTO<String> result = atlasClient.get(typedefs + "?type=business_metadata");
            data= JSONObject.parseObject(result.data,BusinessMetaDataDTO.class);
            //根据时间升序排序
            data.businessMetadataDefs.sort(Comparator.comparing(BusinessMetadataDefsDTO::getCreateTime));
            //集合反转
            Collections.reverse(data.businessMetadataDefs);
            return data;
        }
        catch (Exception e)
        {
            log.error("getBusinessMetaDataList ex:"+e);
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }
    }

    @Override
    public ResultEnum addBusinessMetaData(BusinessMetaDataDTO dto)
    {
        String jsonParameter= JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.post(typedefs + "?type=business_metadata", jsonParameter);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum updateBusinessMetaData(BusinessMetaDataDTO dto)
    {
        String jsonParameter= JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.put(typedefs + "?type=business_metadata", jsonParameter);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum deleteBusinessMetaData(String businessMetaDataName) {
        ResultDataDTO<String> result = atlasClient.delete(delTypeDefs + "/" + businessMetaDataName);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum synchronousBusinessMetaData() {
        //获取配置数据列表
        QueryWrapper<BusinessMetadataConfigPO> queryWrapper = new QueryWrapper<>();
        List<BusinessMetadataConfigPO> list = mapper.selectList(queryWrapper);
        //定义对象
        BusinessMetaDataDTO dto = new BusinessMetaDataDTO();
        List<BusinessMetadataDefsDTO> businessMetadataDefs = new ArrayList<>();
        //分组业务元数据名称分组
        Map<String, List<BusinessMetadataConfigPO>> collect = list.stream()
                .collect(Collectors.groupingBy(BusinessMetadataConfigPO::getBusinessMetadataName));
        for (String businessMetaDataName : collect.keySet()) {
            BusinessMetadataDefsDTO data = new BusinessMetadataDefsDTO();
            List<BusinessMetadataConfigPO> attributeList = collect.get(businessMetaDataName);
            data.name = businessMetaDataName;
            data.description = attributeList.get(0).businessMetadataCnName;
            data.category = "BUSINESS_METADATA";
            List<BusinessMetaDataAttributeDefsDTO> attributeDefs = new ArrayList<>();
            for (BusinessMetadataConfigPO item : attributeList) {
                BusinessMetaDataAttributeDefsDTO attribute = new BusinessMetaDataAttributeDefsDTO();
                attribute.cardinality = "SINGLE";
                attribute.name = item.attributeName;
                attribute.isIndexable = true;
                attribute.isOptional = true;
                attribute.isUnique = false;
                String[] split = item.suitableType.split(",");
                JSONArray jsonArray = new JSONArray(Arrays.asList(split));
                attribute.typeName = item.attributeType;
                attribute.multiValueSelect = item.multipleValued ? "true" : null;
                attribute.searchWeight = "5";
                attribute.options = new BusinessMetaDataOptionsDTO();
                attribute.options.applicableEntityTypes = jsonArray.toString();
                attribute.options.maxStrLength = "100";
                attributeDefs.add(attribute);
            }
            data.attributeDefs = attributeDefs;
            businessMetadataDefs.add(data);
        }
        dto.businessMetadataDefs = businessMetadataDefs;
        return addBusinessMetaData(dto);
    }

}
