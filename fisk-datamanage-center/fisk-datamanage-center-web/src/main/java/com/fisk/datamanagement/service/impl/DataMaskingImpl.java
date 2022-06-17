package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.datamasking.DataMaskingSourceDTO;
import com.fisk.datamanagement.dto.datamasking.DataMaskingTargetDTO;
import com.fisk.datamanagement.dto.datamasking.SourceTableDataDTO;
import com.fisk.datamanagement.entity.MetadataMapAtlasPO;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.enums.TableTypeEnum;
import com.fisk.datamanagement.mapper.MetadataMapAtlasMapper;
import com.fisk.datamanagement.service.IDataMasking;
import com.fisk.datamanagement.vo.ConnectionInformationDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class DataMaskingImpl implements IDataMasking {

    @Resource
    EntityImpl entityImpl;
    @Resource
    DataAssetsImpl dataAssetImpl;
    @Resource
    MetadataMapAtlasMapper mapper;

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
            ConnectionInformationDTO connectionInformationDTO = dataAssetImpl.jointConnection(
                    attributes.getString("rdbms_type").toLowerCase(),
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
            log.error("getSourceDataConfig ex:", e);
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

    @Override
    public SourceTableDataDTO getTableData(SourceTableDataDTO dto)
    {
        try {
            //判断guid是否为空
            if (!StringUtils.isEmpty(dto.tableGuid))
            {
                QueryWrapper<MetadataMapAtlasPO> queryWrapper=new QueryWrapper<>();
                queryWrapper.lambda().eq(MetadataMapAtlasPO::getAtlasGuid,dto.tableGuid);
                MetadataMapAtlasPO po=mapper.selectOne(queryWrapper);
                if (po==null)
                {
                    throw new FkException(ResultEnum.DATA_NOTEXISTS);
                }
                dto.tableId=po.tableId;
                dto.tableName=getDbAndTableName(dto.tableGuid)[1];
                dto.tableTypeEnum=TableTypeEnum.getEnum(po.tableType);
            }
            else {
                QueryWrapper<MetadataMapAtlasPO> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(MetadataMapAtlasPO::getTableId, dto.tableId)
                        .eq(MetadataMapAtlasPO::getTableType,dto.tableTypeEnum.getValue())
                        .eq(MetadataMapAtlasPO::getType,EntityTypeEnum.RDBMS_TABLE.getValue())
                        .eq(MetadataMapAtlasPO::getColumnId, 0);
                MetadataMapAtlasPO po = mapper.selectOne(queryWrapper);
                dto.tableGuid = po.atlasGuid;
            }
            return dto;
        }
        catch (Exception e)
        {
            log.error("getTableData ex:", e);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR,e);
        }
    }

}
