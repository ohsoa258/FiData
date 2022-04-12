package com.fisk.datamanagement.service.impl;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamanagement.dto.dataquality.DataSourceConfigDTO;
import com.fisk.datamanagement.service.IDataQuality;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author JianWenYang
 */
@Service
public class DataQualityImpl implements IDataQuality {

    @Value("${fidata.database.hostname}")
    private String fiDataHostName;
    @Value("${fidata.database.port}")
    private String fiDataPort;
    @Value("${fidata.database.rdbmstype}")
    private String fiDataRdbmsType;
    @Value("${fidata.database.username}")
    private String fiDataUserName;
    @Value("${fidata.database.password}")
    private String fiDataPassword;
    @Value("${fidata.database.db}")
    private String db;

    @Override
    public DataSourceConfigDTO getDataSourceConfig(int index)
    {
        try {
            DataSourceConfigDTO dto=new DataSourceConfigDTO();
            dto.hostName=fiDataHostName.split(",")[index];
            dto.port=fiDataPort.split(",")[index];
            dto.dbName=db.split(",")[index];
            dto.userName=fiDataUserName.split(",")[index];
            dto.password=fiDataPassword.split(",")[index];
            dto.rdbmsType=fiDataRdbmsType.split(",")[index];
            return dto;
        }
        catch (Exception e)
        {
            throw new FkException(ResultEnum.DATA_SOURCE_CONFIG);
        }
    }

    //type：获取上游、下游、上下游
    //表名
    //配置文件索引

}
