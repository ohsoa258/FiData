package com.fisk.dataaccess.utils.sql;

import com.fisk.common.core.enums.dbdatatype.FlinkTypeEnum;
import com.fisk.common.core.enums.dbdatatype.OracleTypeEnum;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.dto.oraclecdc.CdcJobParameterDTO;
import com.fisk.dataaccess.dto.oraclecdc.CdcJobScriptDTO;
import com.fisk.dataaccess.dto.savepointhistory.SavepointHistoryDTO;
import com.fisk.dataaccess.dto.table.FieldNameDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessDTO;
import com.fisk.dataaccess.service.impl.SavepointHistoryImpl;
import com.fisk.system.dto.datasource.DataSourceDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 * @date 2022-09-20 11:47
 */
@Component
public class OracleCdcUtils {

    private static String passwordPlaceholder = "******";
    private static String ln = "\n";
    public static String redisPrefix = "Cdc";
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    SavepointHistoryImpl savepointHistory;

    /**
     * oracle字段类型映射flink类型
     *
     * @param dataTypeName 数据类型名称
     * @param dataLength   数据长度
     * @param precision    精度
     * @return
     */
    public static String oracleTypeMappingFlinkType(String dataTypeName, Integer dataLength, Integer precision) {
        OracleTypeEnum typeEnum = OracleTypeEnum.getValue(dataTypeName);
        String dataType = null;
        int length = dataLength - precision;
        switch (typeEnum) {
            case CHAR:
            case NCHAR:
            case NVARCHAR2:
            case VARCHAR:
            case VARCHAR2:
            case CLOB:
            case NCLOB:
            case XMLType:
                dataType = FlinkTypeEnum.STRING.getName();
                break;
            case NUMBER:
                if (precision <= 0 && length < 3) {
                    dataType = FlinkTypeEnum.TINYINT.getName();
                } else if (precision <= 0 && length < 5) {
                    dataType = FlinkTypeEnum.SMALLINT.getName();
                } else if (precision <= 0 && length < 10) {
                    dataType = FlinkTypeEnum.INT.getName();
                } else if (precision <= 0 && length < 19) {
                    dataType = FlinkTypeEnum.BIGINT.getName();
                } else if (precision <= 0 && length <= 38 && length > 19) {
                    dataType = FlinkTypeEnum.DECIMAL.getName() + "(" + length + "," + 0 + ")";
                } else if (precision > 0) {
                    dataType = FlinkTypeEnum.DECIMAL.getName() + "(" + dataLength + "," + precision + ")";
                } else {
                    dataType = FlinkTypeEnum.STRING.getName();
                }
                break;
            case FLOAT:
            case BINARY_FLOAT:
                dataType = FlinkTypeEnum.FLOAT.getName();
                break;
            case DATE:
            case TIMESTAMP:
                //dataType = "TIMESTAMP(" + dataLength + ") WITHOUT TIMEZONE";
                //break;
            case TIMESTAMPWITHTIMEZONE:
                dataType = "TIMESTAMP(" + dataLength + ")";
                //dataType = "TIMESTAMP(" + dataLength + ") WITH TIME ZONE";
                break;
            case TIMESTAMPWITHLOCALTIMEZONE:
                dataType = "TIMESTAMP_LTZ(" + dataLength + ")";
                break;
            case DOUBLEPRECISION:
            case BINARY_DOUBLE:
                dataType = FlinkTypeEnum.DOUBLE.getName();
                break;
            case BLOB:
            case ROWID:
                dataType = FlinkTypeEnum.BYTES.getName();
                break;
            case INTERVALDAYTOSECOND:
            case INTERVALYEARTOMONTH:
                dataType = FlinkTypeEnum.BIGINT.getName();
                break;
            default:
                dataType = FlinkTypeEnum.STRING.getName();
                break;
        }
        return dataType;
    }

    /**
     * 拼接cdc任务脚本
     *
     * @param dto
     * @param dataSourceDto
     * @param dataSource
     * @param tableAccessData
     * @return
     */
    public CdcJobScriptDTO createCdcJobScript(CdcJobParameterDTO dto,
                                              AppDataSourceDTO dataSourceDto,
                                              DataSourceDTO dataSource,
                                              TbTableAccessDTO tableAccessData) {
        CdcJobScriptDTO data = new CdcJobScriptDTO();
        StringBuilder str = new StringBuilder();
        StringBuilder redisStr = new StringBuilder();
        str.append("SET pipeline.name =" + tableAccessData.pipelineName + ";");
        str.append(ln);
        str.append("SET execution.checkpointing.interval =" + tableAccessData.checkPointInterval + tableAccessData.checkPointUnit + ";");
        str.append(ln);
        SavepointHistoryDTO savepointHistoryDetails = savepointHistory.getSavepointHistoryDetails(dto.tableAccessId, dto.savepointHistoryId);
        if (savepointHistoryDetails != null) {
            str.append("SET execution.savepoint.path = '" + savepointHistoryDetails.savepointPath + "';");
            str.append(ln);
        }
        str.append(ln);
        redisStr.append(str);
        //来源表脚本
        String sourceTable = buildSourceTableScript(dto, dataSourceDto, tableAccessData);
        str.append(sourceTable);

        sourceTable = sourceTable.replace(passwordPlaceholder, dataSourceDto.connectPwd);
        redisStr.append(sourceTable);

        //目标表脚本
        String targetTable = buildTargetTableScript(dto, dataSource, tableAccessData.tableName);
        str.append(targetTable);

        targetTable = targetTable.replace(passwordPlaceholder, dataSource.conPassword);
        redisStr.append(targetTable);

        //insert select语句
        String sql = buildSqlScript(dto, tableAccessData.tableName);
        str.append(sql);

        redisStr.append(sql);
        setCdcRedis(redisStr, tableAccessData.id);

        data.jobScript = str.toString();
        return data;
    }

    public void setCdcRedis(StringBuilder redisStr, long id) {
        redisTemplate.opsForValue().set(redisPrefix + ":" + id, redisStr.toString());
    }

    /**
     * 来源表脚本配置
     *
     * @param dto
     * @param dataSourceDto
     * @return
     */
    public String buildSourceTableScript(CdcJobParameterDTO dto,
                                         AppDataSourceDTO dataSourceDto,
                                         TbTableAccessDTO tableAccessData) {
        String sourceTable = dto.fieldNameDTOList.get(0).sourceTableName;
        //获取oracle主键字段
        Connection conn = DbConnectionHelper.connection(dataSourceDto.connectStr,
                dataSourceDto.connectAccount,
                dataSourceDto.connectPwd,
                com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL);
        List<String> tablePrimaryKeyList = OracleUtils.getTablePrimaryKey(conn,
                dataSourceDto.dbName, sourceTable);

        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE ");
        str.append(dto.fieldNameDTOList.get(0).sourceTableName + " (");
        str.append(ln);
        List<String> columnList = new ArrayList<>();
        for (FieldNameDTO item : dto.fieldNameDTOList) {
            String dataType = oracleTypeMappingFlinkType(item.sourceFieldType, item.sourceFieldLength, item.sourceFieldPrecision);
            columnList.add(item.sourceFieldName + " " + dataType);
        }
        //拼接主键
        List<String> collect = dto.fieldNameDTOList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        //取交集
        collect.retainAll(tablePrimaryKeyList);
        if (!CollectionUtils.isEmpty(collect)) {
            columnList.add("PRIMARY KEY (" + StringUtils.join(collect, ",") + ") NOT ENFORCED" + ln);
        }

        str.append(StringUtils.join(columnList, "," + ln + ""));
        str.append(")");
        str.append(ln);
        str.append("WITH");
        str.append("(");
        str.append(ln);
        str.append("'connector'=" + "'" + dataSourceDto.driveType + "'," + ln);
        if (!StringUtils.isEmpty(dataSourceDto.domainName)) {
            str.append("'hostname'=" + "'" + dataSourceDto.domainName + "'," + ln);
        } else {
            str.append("'hostname'=" + "'" + dataSourceDto.host + "'," + ln);
        }
        str.append("'port'=" + "'" + dataSourceDto.port + "'," + ln);
        str.append("'username'=" + "'" + dataSourceDto.connectAccount + "'," + ln);
        str.append("'password'=" + "'" + passwordPlaceholder + "'," + ln);
        //服务名
        str.append("'database-name'=" + "'" + dataSourceDto.serviceName + "'," + ln);
        str.append("'schema-name'=" + "'" + dataSourceDto.dbName + "'," + ln);
        str.append("'table-name'=" + "'" + sourceTable + "'," + ln);
        str.append("'scan.startup.mode'=" + "'" + tableAccessData.scanStartupMode.getName() + "'," + ln);
        if (dataSourceDto.pattern == 1) {
            str.append("'debezium.database.pdb.name'=" + "'" + dataSourceDto.pdbName + "'," + ln);
            str.append("'debezium.database.url'=" + "'" + dataSourceDto.connectStr + "'," + ln);
        }
        str.append("'debezium.log.mining.strategy'=" + "'online_catalog'," + ln);
        str.append("'debezium.log.mining.sleep.time.min.ms'=" + "'0'," + ln);
        str.append("'debezium.log.mining.sleep.time.max.ms'=" + "'1000'" + ln);
        str.append(");");
        str.append(ln);

        return str.toString();
    }

    /**
     * 目标表脚本配置
     *
     * @param dto
     * @return
     */
    public String buildTargetTableScript(CdcJobParameterDTO dto, DataSourceDTO dataSource, String targetTable) {
        StringBuilder str = new StringBuilder();
        str.append(ln);
        str.append("CREATE TABLE ");
        str.append(targetTable);
        str.append("(");
        str.append(ln);
        List<String> columnList = new ArrayList<>();

        for (FieldNameDTO item : dto.fieldNameDTOList) {
            String dataType = oracleTypeMappingFlinkType(item.sourceFieldType, item.sourceFieldLength, item.sourceFieldPrecision);
            columnList.add(item.fieldName + " " + dataType);
        }

        //拼接主键
        List<FieldNameDTO> collect = dto.fieldNameDTOList.stream().filter(e -> e.isPrimarykey == 1).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(collect)) {
            columnList.add("PRIMARY KEY (" + StringUtils.join(collect.stream().map(e -> e.fieldName).collect(Collectors.toList()), ",") + ") NOT ENFORCED");
        }
        str.append(StringUtils.join(columnList, "," + ln + ""));
        str.append(")");
        str.append(ln);
        str.append("WITH");
        str.append("(");
        str.append(ln);
        str.append("'connector'=" + "'jdbc'," + ln);
        str.append("'driver'=" + "'" + dataSource.conType.getDriverName() + "'," + ln);
        str.append("'url'=" + "'" + dataSource.conStr + "'," + ln);
        str.append("'username'=" + "'" + dataSource.conAccount + "'," + ln);
        str.append("'password'=" + "'" + passwordPlaceholder + "'," + ln);
        str.append("'table-name'=" + "'" + targetTable + "'" + ln);
        str.append(");");
        str.append(ln);

        return str.toString();
    }

    /**
     * 拼接insert select语句
     *
     * @param dto
     * @return
     */
    public String buildSqlScript(CdcJobParameterDTO dto, String targetTable) {
        StringBuilder str = new StringBuilder();
        str.append(ln);
        str.append("INSERT INTO ");
        str.append(targetTable);
        str.append("(");
        List<String> collect = dto.fieldNameDTOList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        str.append(StringUtils.join(collect, ","));
        str.append(")");
        str.append(" SELECT ");
        List<String> collect1 = dto.fieldNameDTOList.stream().map(e -> e.fieldName).collect(Collectors.toList());
        str.append(StringUtils.join(collect1, ","));
        str.append(" FROM ");
        str.append(dto.fieldNameDTOList.get(0).sourceTableName);

        return str.toString();
    }


}
