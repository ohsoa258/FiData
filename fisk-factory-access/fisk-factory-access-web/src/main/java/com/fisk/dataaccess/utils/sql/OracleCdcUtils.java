package com.fisk.dataaccess.utils.sql;

import com.fisk.common.core.enums.dbdatatype.FlinkTypeEnum;
import com.fisk.common.core.enums.dbdatatype.OracleTypeEnum;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.dto.oraclecdc.CdcJobParameterDTO;
import com.fisk.dataaccess.dto.oraclecdc.CdcJobScriptDTO;
import com.fisk.dataaccess.dto.table.FieldNameDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessDTO;
import com.fisk.system.dto.datasource.DataSourceDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 * @date 2022-09-20 11:47
 */
@Component
public class OracleCdcUtils {

    /**
     * oracle字段类型映射flink类型
     *
     * @param dataTypeName 数据类型名称
     * @param dataLength   数据长度
     * @param precision    精度
     * @return
     */
    public static FlinkTypeEnum oracleTypeMappingFlinkType(String dataTypeName, String dataLength, String precision) {
        OracleTypeEnum typeEnum = OracleTypeEnum.getValue(dataTypeName);
        FlinkTypeEnum flinkTypeEnum = null;
        switch (typeEnum) {
            case CHAR:
            case NCHAR:
            case NVARCHAR2:
            case VARCHAR:
            case VARCHAR2:
            case CLOB:
            case NCLOB:
            case XMLType:
            case NUMBER:
                flinkTypeEnum = FlinkTypeEnum.STRING;
                break;
            case FLOAT:
            case BINARY_FLOAT:
                flinkTypeEnum = FlinkTypeEnum.FLOAT;
                break;
            case DOUBLEPRECISION:
            case BINARY_DOUBLE:
                flinkTypeEnum = FlinkTypeEnum.DOUBLE;
                break;
            case BLOB:
            case ROWID:
                flinkTypeEnum = FlinkTypeEnum.BYTES;
                break;
            case INTERVALDAYTOSECOND:
            case INTERVALYEARTOMONTH:
                flinkTypeEnum = FlinkTypeEnum.BIGINT;
                break;
            default:
                flinkTypeEnum = FlinkTypeEnum.STRING;
                break;
        }
        return flinkTypeEnum;
    }

    public CdcJobScriptDTO createCdcJobScript(CdcJobParameterDTO dto,
                                              AppDataSourceDTO dataSourceDto,
                                              DataSourceDTO dataSource,
                                              TbTableAccessDTO tableAccessData) {
        CdcJobScriptDTO data = new CdcJobScriptDTO();
        StringBuilder str = new StringBuilder();
        str.append("SET pipeline.name ='" + tableAccessData.pipelineName);
        str.append("\n");
        str.append("SET execution.checkpointing.interval =" + tableAccessData.checkPointInterval + tableAccessData.checkPointUnit);
        str.append("\n");
        //来源表脚本
        str.append(buildSourceTableScript(dto, dataSourceDto, tableAccessData));
        //目标表脚本
        str.append(buildTargetTableScript(dto, dataSource));
        str.append(buildSqlScript(dto));
        return data;
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
        List<String> tablePrimaryKeyList = OracleUtils.getTablePrimaryKey(dataSourceDto.connectStr,
                dataSourceDto.connectAccount,
                dataSourceDto.connectPwd,
                dataSourceDto.dbName, sourceTable);

        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE ");
        str.append(dto.fieldNameDTOList.get(0).sourceTableName + " (");
        List<String> columnList = new ArrayList<>();
        for (FieldNameDTO item : dto.fieldNameDTOList) {
            FlinkTypeEnum flinkTypeEnum = oracleTypeMappingFlinkType(item.sourceFieldType, null, null);
            columnList.add(item.sourceFieldName + " " + flinkTypeEnum.getName());
        }
        //拼接主键
        List<String> collect = dto.fieldNameDTOList.stream().map(e -> e.sourceFieldName).collect(Collectors.toList());
        //取交集
        collect.retainAll(tablePrimaryKeyList);
        if (!CollectionUtils.isEmpty(collect)) {
            columnList.add("PRIMARY KEY (" + StringUtils.join(collect, ",") + ") NOT ENFORCED");
        }

        str.append(StringUtils.join(columnList, ","));
        str.append(")");
        str.append("WITH");
        str.append("(");
        str.append("'connector'=" + "'" + dataSourceDto.driveType + "',");
        str.append("'hostname'=" + "'" + dataSourceDto.host + "',");
        str.append("'port'=" + "'" + dataSourceDto.port + "',");
        str.append("'username'=" + "'" + dataSourceDto.connectAccount + "',");
        str.append("'password'=" + "'" + dataSourceDto.connectPwd + "',");
        //服务名
        str.append("'database-name'=" + "'" + dataSourceDto.serviceName + "',");
        str.append("'schema-name'=" + "'" + dataSourceDto.dbName + "',");
        str.append("'table-name'=" + "'" + sourceTable + "',");
        str.append("'scan.startup.mode'=" + "'" + tableAccessData.scanStartupMode.getName() + "'");
        str.append(");");

        return str.toString();
    }

    /**
     * 目标表脚本配置
     *
     * @param dto
     * @return
     */
    public String buildTargetTableScript(CdcJobParameterDTO dto, DataSourceDTO dataSource) {
        StringBuilder str = new StringBuilder();
        str.append("CREATE TABLE ");
        str.append(dto.targetTable);
        str.append("(");
        List<String> columnList = new ArrayList<>();
        for (FieldNameDTO item : dto.fieldNameDTOList) {
            if (item.sourceFieldType.equalsIgnoreCase("VARCHAR")
                    || item.sourceFieldType.equalsIgnoreCase("TEXT")) {
                columnList.add(item.sourceFieldName + " " + "STRING");
                continue;
            } else if (item.sourceFieldType.equalsIgnoreCase("NUMERIC")) {

            } else if (item.sourceFieldType.equalsIgnoreCase("timestamp")) {

            }
            columnList.add(item.sourceFieldName + " " + item.sourceFieldType);
        }
        //拼接主键
        List<FieldNameDTO> collect = dto.fieldNameDTOList.stream().filter(e -> e.isPrimarykey == 1).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(collect)) {
            columnList.add("PRIMARY KEY (" + StringUtils.join(collect.stream().map(e -> e.fieldName).collect(Collectors.toList()), ",") + ") NOT ENFORCED");
        }

        str.append(StringUtils.join(columnList, ","));
        str.append(")");
        str.append("WITH");
        str.append("(");
        str.append("'connector'=" + "'jdbc',");
        str.append("'url'=" + "'" + dataSource.conStr + "',");
        str.append("'username'=" + "'" + dataSource.conAccount + "',");
        str.append("'password'=" + "'" + dataSource.conPassword + "',");
        str.append("'table-name'=" + "'" + dto.fieldNameDTOList.get(0).sourceTableName + "'");
        str.append(");");

        return str.toString();
    }

    /**
     * 拼接insert select语句
     *
     * @param dto
     * @return
     */
    public String buildSqlScript(CdcJobParameterDTO dto) {
        StringBuilder str = new StringBuilder();
        str.append("INSERT INTO ");
        str.append(dto.targetTable);
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
