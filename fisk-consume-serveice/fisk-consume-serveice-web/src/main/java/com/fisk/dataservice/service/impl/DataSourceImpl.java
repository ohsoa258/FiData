package com.fisk.dataservice.service.impl;

import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.core.utils.dbutils.utils.MySqlConUtils;
import com.fisk.common.core.utils.dbutils.utils.OracleUtils;
import com.fisk.common.core.utils.dbutils.utils.PgSqlUtils;
import com.fisk.common.core.utils.dbutils.utils.SqlServerUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.dataservice.dto.datasource.DataSourceInfoDTO;
import com.fisk.dataservice.service.IDataSource;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Slf4j
@Service
public class DataSourceImpl implements IDataSource {

    @Resource
    UserClient client;

    @Override
    public List<DataSourceInfoDTO> getTableInfoList() {
        //获取数据源
        List<DataSourceDTO> allFiDataDataSource = getAllFiDataDataSource();
        if (CollectionUtils.isEmpty(allFiDataDataSource)) {
            return new ArrayList<>();
        }
        //过滤数据
        List<DataSourceDTO> dataList = allFiDataDataSource.stream()
                .filter(e -> e.sourceBusinessType == SourceBusinessTypeEnum.DW
                        || e.sourceBusinessType == SourceBusinessTypeEnum.ODS)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(dataList)) {
            return new ArrayList<>();
        }

        List<DataSourceInfoDTO> list = new ArrayList<>();

        for (DataSourceDTO item : dataList) {
            DataSourceInfoDTO data = new DataSourceInfoDTO();
            data.dbName = item.name;
            data.tableNameList = getDbTable(item);
        }

        return list;
    }

    /**
     * 获取数据源所有表
     *
     * @param dto
     * @return
     */
    public List<TableNameDTO> getDbTable(DataSourceDTO dto) {
        Connection conn = null;
        try {
            AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
            conn = commonDbHelper.connection(dto.conStr, dto.conAccount, dto.conPassword, dto.conType);
            List<TableNameDTO> data = new ArrayList<>();
            switch (dto.conType) {
                case MYSQL:
                    data = MySqlConUtils.getTableName(conn);
                    break;
                case SQLSERVER:
                    data = SqlServerUtils.getTableName(conn);
                    break;
                case POSTGRESQL:
                    data = PgSqlUtils.getTableName(conn);
                    break;
                case ORACLE:
                    data = OracleUtils.getTableName(conn);
                    break;
                default:
                    throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
            }
            return data;
        } catch (Exception e) {
            log.error("【获取表信息失败】,{}", e);
            return null;
        } finally {
            AbstractCommonDbHelper.closeConnection(conn);
        }
    }

    /**
     * 获取fidata内部数据源
     *
     * @return
     */
    public List<DataSourceDTO> getAllFiDataDataSource() {
        ResultEntity<List<DataSourceDTO>> allFiDataDataSource = client.getAllFiDataDataSource();
        if (allFiDataDataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        return allFiDataDataSource.data;
    }

}
