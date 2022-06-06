package com.fisk.datagovernance.service.impl.datasecurity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.datagovernance.constants.datasecurity.DataMaskingConstant;
import com.fisk.datagovernance.dto.datasecurity.DatamaskingConfigDTO;
import com.fisk.datagovernance.dto.datasecurity.datamasking.DataSourceIdDTO;
import com.fisk.datagovernance.entity.datasecurity.DatamaskingConfigPO;
import com.fisk.datagovernance.enums.datasecurity.DataMaskingTypeEnum;
import com.fisk.datagovernance.map.datasecurity.DatamaskingConfigMap;
import com.fisk.datagovernance.mapper.datasecurity.DatamaskingConfigMapper;
import com.fisk.datagovernance.service.datasecurity.DatamaskingConfigService;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamanagement.dto.datamasking.DataMaskingSourceDTO;
import com.fisk.datamanagement.dto.datamasking.DataMaskingTargetDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:48
 */
@Service
@Slf4j
public class DatamaskingConfigServiceImpl extends ServiceImpl<DatamaskingConfigMapper, DatamaskingConfigPO> implements DatamaskingConfigService {

    @Resource
    private DataManageClient dataManageClient;

    @Override
    public DatamaskingConfigDTO getData(long id) {

        DatamaskingConfigPO po = this.getById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // po -> dto
        return DatamaskingConfigMap.INSTANCES.poToDto(po);
    }

    @Override
    public ResultEnum addData(DatamaskingConfigDTO dto) {

        // TODO NAME为需要判断的字段名
        // 当前字段名不可重复
        List<String> list = this.list().stream().map(e -> e.fieldName).collect(Collectors.toList());
        if (list.contains(dto.fieldName)) {
            return ResultEnum.FIELD_NAME_IS_SELECTED;
        }

        // dto -> po
        DatamaskingConfigPO model = DatamaskingConfigMap.INSTANCES.dtoToPo(dto);
        // 参数校验
        if (model == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }

        if (dto.publishFlag) {

        }

        //保存
        return this.save(model) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    private DatamaskingConfigDTO buildDataSyncSql(DatamaskingConfigDTO dto) {
        DataMaskingTypeEnum typeEnum = DataMaskingTypeEnum.getEnum(dto.maskingType);
        switch (typeEnum) {
            // 保留字段配置
            case KEEP_FIELD:
                dto.dataMaskingSql = "update " + DataMaskingConstant.TABLE_NAME + " set " + DataMaskingConstant.FIELD_PREFIX + dto.fieldName +
                        " = concat(subStr(" + dto.fieldName + ",1," + dto.numberDigits + "),'" + dto.contentReplace + "');";
                break;
            // 值加密配置
            case VALUE_ENCRYPT:
                // 值加密还未想好
                dto.dataMaskingSql = null;
                break;
            default:
                break;
        }
        return dto;
    }

    public String alterTableSql(DatamaskingConfigDTO dto) {
        return "ALTER TABLE " + DataMaskingConstant.TABLE_NAME + " ADD " +
                DataMaskingConstant.FIELD_PREFIX + dto.fieldName + " VARCHAR(255);";
    }

    private void executeDataMaskingConfig(DatamaskingConfigDTO dto) {
        try {
            DataMaskingSourceDTO dataMaskingSourceDto = new DataMaskingSourceDTO();
            dataMaskingSourceDto.datasourceId = dto.datasourceId;
            dataMaskingSourceDto.tableId = dto.tableId;

            ResultEntity<DataMaskingTargetDTO> result = dataManageClient.getSourceDataConfig(dataMaskingSourceDto);

            if (result.code == ResultEnum.SUCCESS.getCode()) {
                DataMaskingTargetDTO dataMaskingTargetDto = result.data;
                AbstractDbHelper abstractDbHelper = new AbstractDbHelper();

                // 创建连接
                Connection connection = abstractDbHelper.connection(dataMaskingTargetDto.url, dataMaskingTargetDto.username, dataMaskingTargetDto.password, DataSourceTypeEnum.PG);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select * from " + dataMaskingTargetDto.tableName + " LIMIT 10 OFFSET 0;");
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                // 获取当前表所有字段
                List<String> fieldNameList = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    fieldNameList.add(metaData.getColumnName(i));
                }

                DatamaskingConfigDTO buildDataSyncSqlDto = buildDataSyncSql(dto);
                // 预留sql,防止报错
                String syncSql = "SELECT * FROM test LIMIT 10 OFFSET 0;";
                if (StringUtils.isNotBlank(buildDataSyncSqlDto.dataMaskingSql)) {
                    syncSql = buildDataSyncSqlDto.dataMaskingSql.replace(DataMaskingConstant.TABLE_NAME, dataMaskingTargetDto.tableName);
                }

                // 存在原始字段
                if (fieldNameList.contains(dto.fieldName)) {
                    // 存在脱敏字段
                    if (fieldNameList.contains(DataMaskingConstant.FIELD_PREFIX + dto.fieldName)) {
                        // 执行数据同步sql
                        statement.execute(syncSql);
                    } else { // 不存在脱敏字段
                        // 1.修改表结构,创建脱敏字段
                        String alterTableSql = alterTableSql(dto);
                        // 字符串替换
                        String replaceSql = alterTableSql.replace(DataMaskingConstant.TABLE_NAME, dataMaskingTargetDto.tableName);
                        statement.execute(replaceSql);
                        // 2.执行数据同步
                        statement.execute(syncSql);
                    }
                } else {
                    log.info("脱敏原始字段已删除,当前脱敏配置过期,建议删除");
                }
            }
        } catch (Exception e) {
            log.error("远程调用失败，方法名：【datamanagement-service:getSourceDataConfig】,报错异常: " + e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum editData(DatamaskingConfigDTO dto) {
        // TODO NAME为需要判断的字段名
        // 判断名称是否重复
        QueryWrapper<DatamaskingConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DatamaskingConfigPO::getFieldName, dto.fieldName);
        DatamaskingConfigPO po = baseMapper.selectOne(queryWrapper);
        if (po != null && po.id != dto.id) {
            return ResultEnum.FIELD_NAME_IS_SELECTED;
        }

        // 参数校验
        DatamaskingConfigPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // dto -> po
        // 执行修改
        return this.updateById(DatamaskingConfigMap.INSTANCES.dtoToPo(dto)) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteData(long id) {

        // 参数校验
        DatamaskingConfigPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 执行删除
        return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<DatamaskingConfigDTO> getList(DataSourceIdDTO dto) {

        List<DatamaskingConfigPO> listPo = this.query()
                .eq("datasource_id", dto.datasourceId)
                .eq("table_id", dto.tableId)
                .orderByDesc("create_time").list();

        return DatamaskingConfigMap.INSTANCES.listPoToDto(listPo);
    }

}