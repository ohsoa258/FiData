package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.datamasking.DataMaskingSourceDTO;
import com.fisk.datamanagement.dto.datamasking.DataMaskingTargetDTO;
import com.fisk.datamanagement.service.IDataMasking;
import com.fisk.datamanagement.vo.ConnectionInformationDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
public class DataMaskingImpl implements IDataMasking {

    @Resource
    EntityImpl entityImpl;
    @Resource
    DataAssetsImpl dataAssetImpl;

    @Override
    public DataMaskingTargetDTO getSourceDataConfig(DataMaskingSourceDTO dto)
    {
        try {
            DataMaskingTargetDTO data=new DataMaskingTargetDTO();
            //获取表名以及库名
            String[] dbAndTableName = getDbAndTableName(dto.tableId);
            data.tableName=dbAndTableName[1];
            //获取数据源配置
            JSONObject entityDetails = entityImpl.getEntity(dto.datasourceId);
            //获取entity
            JSONObject entity = JSON.parseObject(entityDetails.getString("entity"));
            //获取attributes
            JSONObject attributes=JSON.parseObject(entity.getString("attributes"));
            //拼接连接字符串
            ConnectionInformationDTO connectionInformationDTO = dataAssetImpl.jointConnection(attributes.getString("rdbms_type"),
                    attributes.getString("hostname"),
                    attributes.getString("port"), dbAndTableName[0]);
            data.url=connectionInformationDTO.url;
            //获取账号、密码
            String[] comments = attributes.getString("comment").split("&");
            data.username=comments[0];
            data.password=comments[1];
            return data;
        }
        catch (Exception e)
        {
            throw new FkException(ResultEnum.SQL_ANALYSIS,e);
        }
    }

    public String[] getDbAndTableName(String tableId)
    {
        String[] strArray=new String[2];
        JSONObject entityDetails = entityImpl.getEntity(tableId);
        //获取entity
        JSONObject entity = JSON.parseObject(entityDetails.getString("entity"));
        //获取attributes
        JSONObject attributes=JSON.parseObject(entity.getString("attributes"));
        //获取relationshipAttributes
        JSONObject relationshipAttributes=JSON.parseObject(entity.getString("relationshipAttributes"));
        //获取dbName
        JSONObject db=JSON.parseObject(relationshipAttributes.getString("db"));
        strArray[0]=db.getString("displayText");
        //获取表名
        strArray[1]=attributes.getString("name");
        return strArray;
    }

}
