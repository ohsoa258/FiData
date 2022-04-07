package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.businessmetadata.BusinessMetaDataDTO;
import com.fisk.datamanagement.dto.businessmetadata.BusinessMetadataDefsDTO;
import com.fisk.datamanagement.enums.AtlasResultEnum;
import com.fisk.datamanagement.service.IBusinessMetaData;
import com.fisk.datamanagement.utils.atlas.AtlasClient;
import com.fisk.datamanagement.vo.ResultDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class BusinessMetaDataImpl implements IBusinessMetaData {

    @Resource
    AtlasClient atlasClient;

    @Value("${atlas.typedefs}")
    private String typedefs;
    @Value("${atlas.delTypeDefs}")
    private String delTypeDefs;

    @Override
    public BusinessMetaDataDTO getBusinessMetaDataList()
    {
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
            e.printStackTrace();
            log.error("getBusinessMetaDataList ex:"+e);
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }
    }

    @Override
    public ResultEnum addBusinessMetaData(BusinessMetaDataDTO dto)
    {
        String jsonParameter= JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.post(typedefs + "?type=business_metadata", jsonParameter);
        return result.code== AtlasResultEnum.REQUEST_SUCCESS?ResultEnum.SUCCESS:ResultEnum.BAD_REQUEST;
    }

    @Override
    public ResultEnum updateBusinessMetaData(BusinessMetaDataDTO dto)
    {
        String jsonParameter= JSONArray.toJSON(dto).toString();
        ResultDataDTO<String> result = atlasClient.put(typedefs + "?type=business_metadata", jsonParameter);
        return atlasClient.newResultEnum(result);
    }

    @Override
    public ResultEnum deleteBusinessMetaData(String businessMetaDataName)
    {
        ResultDataDTO<String> result = atlasClient.delete(delTypeDefs + "/" + businessMetaDataName);
        return atlasClient.newResultEnum(result);
    }

}
