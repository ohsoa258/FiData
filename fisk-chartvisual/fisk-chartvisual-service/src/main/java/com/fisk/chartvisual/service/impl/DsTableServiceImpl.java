package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.chartvisual.dto.*;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.entity.DsTableFieldPO;
import com.fisk.chartvisual.entity.DsTablePO;
import com.fisk.chartvisual.enums.MysqlFieldTypeMappingEnum;
import com.fisk.chartvisual.enums.SqlServerFieldTypeMappingEnum;
import com.fisk.chartvisual.map.DsTableMap;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.mapper.DsTableFieldMapper;
import com.fisk.chartvisual.mapper.DsTableMapper;
import com.fisk.chartvisual.service.DsTableService;
import com.fisk.chartvisual.util.dbhelper.AbstractDbHelper;
import com.fisk.chartvisual.util.dbhelper.DbHelperFactory;
import com.fisk.chartvisual.util.dbhelper.buildsql.IBuildSqlCommand;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fisk.chartvisual.enums.DataSourceInfoTypeEnum.*;

/**
 * @author WangYan
 * @date 2022/3/4 11:22
 */
@Service
public class DsTableServiceImpl extends ServiceImpl<DsTableFieldMapper,DsTableFieldPO> implements DsTableService {

    @Resource
    DataSourceConMapper sourceConMapper;
    @Resource
    DsTableFieldMapper dsTableFieldMapper;
    @Resource
    DsTableMapper dsTableMapper;
    @Resource
    DsTableServiceImpl dsTableService;

    @Override
    public ResultEntity<DsTableDTO> getTableInfo(Integer id) {
        DataSourceConPO model = sourceConMapper.selectById(id);
        if (model == null){
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS, ResultEnum.DATA_NOTEXISTS.getMsg());
        }

        // 查询数据源连接配置
        AbstractDbHelper db = DbHelperFactory.getDbHelper(model.conType);
        Connection connection = db.connection(model.conStr, model.conAccount, model.conPassword);
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,this.dataSourceInfo(model,connection,db));
    }

    @Override
    public ResultEntity<List<Map<String, Object>>> getData(ObtainTableDataDTO dto) {
        DataSourceConPO model = sourceConMapper.selectById(dto.getId());
        if (model == null){
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS, ResultEnum.DATA_NOTEXISTS.getMsg());
        }

        // 查询数据源连接配置
        AbstractDbHelper db = DbHelperFactory.getDbHelper(model.conType);
        Connection connection = db.connection(model.conStr, model.conAccount, model.conPassword);
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,this.getTableData(model,connection,db,dto));
    }

    @Override
    public ResultEntity<List<FieldInfoDTO>> getTableStructure(TableStructureDTO dto) {
        DataSourceConPO model = sourceConMapper.selectById(dto.getId());
        if (model == null){
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS, ResultEnum.DATA_NOTEXISTS.getMsg());
        }

        // 查询数据源连接配置
        AbstractDbHelper db = DbHelperFactory.getDbHelper(model.conType);
        Connection connection = db.connection(model.conStr, model.conAccount, model.conPassword);
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS,this.getFieldInfo(model,connection,db,dto));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEntity<ResultEnum> saveTableInfo(SaveDsTableDTO dsTableDto) {

        // 保存表名
        DsTablePO dsTable = new DsTablePO();
        dsTable.setDataSourceId(dsTableDto.getDataSourceId());
        dsTable.setTableName(dsTableDto.getTableName());
        int i = dsTableMapper.insert(dsTable);
        if (i <= 0){
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR, ResultEnum.SAVE_DATA_ERROR.getMsg());
        }


        // 保存字段信息
        List<DsTableFieldPO> fieldList = dsTableDto.getFieldList().stream().filter(Objects::nonNull).map(e -> {
            return DsTableMap.INSTANCES.dtoToPo(e, dsTable.getId());
        }).collect(Collectors.toList());

        boolean saveBatch = dsTableService.saveBatch(fieldList);
        if (saveBatch == false){
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR, ResultEnum.SAVE_DATA_ERROR.getMsg());
        }

        return ResultEntityBuild.build(ResultEnum.SUCCESS);
    }

    @Override
    public ResultEntity<List<SaveDsTableDTO>> selectByDataSourceId(Integer dataSourceId) {
        QueryWrapper<DsTablePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DsTablePO::getDataSourceId,dataSourceId);
        List<DsTablePO> dsTableList = dsTableMapper.selectList(queryWrapper);

        if (CollectionUtils.isNotEmpty(dsTableList)){
            // 查询表名字段信息
            List<SaveDsTableDTO> dtoList = dsTableList.stream().filter(Objects::nonNull).map(e -> {
                SaveDsTableDTO dto = new SaveDsTableDTO();
                dto.setDataSourceId(e.getDataSourceId());
                dto.setTableName(e.getTableName());

                QueryWrapper<DsTableFieldPO> query = new QueryWrapper<>();
                query.lambda().
                        eq(DsTableFieldPO::getTableInfoId, e.getId());
                dto.setFieldList(DsTableMap.INSTANCES.poToDtoDsList(dsTableFieldMapper.selectList(query)));
                return dto;
            }).collect(Collectors.toList());

            return ResultEntityBuild.buildData(ResultEnum.SUCCESS,dtoList);
        }

        return null;
    }

    /**
     * 获取数据源连接信息
     * @param model
     */
    public DsTableDTO dataSourceInfo(DataSourceConPO model,Connection connection,AbstractDbHelper db){
        IBuildSqlCommand buildSqlCommand = DbHelperFactory.getSqlBuilder(model.conType);
        List<TableInfoDTO> resultList = db.execQueryResultList(buildSqlCommand.buildQueryAllTables(model.conDbname), connection, TableInfoDTO.class);

        DsTableDTO dto = new DsTableDTO();
        dto.setName(model.getConDbname());
        dto.setCount(resultList.size());
        dto.setType(DATABASE_NAME);

        dto.setChildren(
                resultList.stream().map(e -> {
                    List<String> fields = db.execQueryResultList(buildSqlCommand.buildQueryFiled(model.conDbname, e.getTable_name()),
                            connection, String.class);

                    return new DsTableDTO(e.getTable_name(), fields.size(),TABLE_NAME);
                }).collect(Collectors.toList())
        );

        return dto;
    }

    /**
     * 获取表预览数据
     * @param model
     * @param connection
     * @param db
     */
    public List<Map<String, Object>> getTableData(DataSourceConPO model,Connection connection,AbstractDbHelper db,ObtainTableDataDTO dto){
        IBuildSqlCommand buildSqlCommand = DbHelperFactory.getSqlBuilder(model.conType);

        // 查询表字段信息
        List<Map<String, Object>> fieldList = db.execQueryResultMaps(buildSqlCommand.buildQueryFiledInfo(dto.getTableName()), connection);

        String field = fieldList.get(0).get("field").toString();
        Integer total = dto.getTotal();
        if (total == null){
            total = 20;
        }
        return db.execQueryResultMaps(buildSqlCommand.getData(dto.getTableName(), total, field), connection);
    }

    /**
     * 获取表字段信息
     * @param model
     * @param connection
     * @param db
     * @param dto
     * @return
     */
    public List<FieldInfoDTO> getFieldInfo(DataSourceConPO model,Connection connection,AbstractDbHelper db,TableStructureDTO dto){
        IBuildSqlCommand buildSqlCommand = DbHelperFactory.getSqlBuilder(model.conType);

        List<FieldInfoDTO> dtoList = new ArrayList<>();
        dto.getTableName().stream().filter(Objects::nonNull).forEach(e -> {
            List<FieldInfoDTO> fieldList = db.execQueryResultList(buildSqlCommand.buildQueryFiledInfo(e), connection, FieldInfoDTO.class);

            // 把表名放进去一起返回
            List<FieldInfoDTO> collect = fieldList.stream().filter(Objects::nonNull).map(item -> {
                FieldInfoDTO dto1 = new FieldInfoDTO();
                dto1.setTable_name(e);
                dto1.setField(item.getField());
                dto1.setType(item.getType());
                dto1.setFieldInfo(item.getFieldInfo());

                try {
                    // 类型匹配
                    switch (model.conType){
                        case MYSQL:
                            dto1.setTargetType(MysqlFieldTypeMappingEnum.getTargetTypeBySourceType(item.getType()));
                            break;
                        case SQLSERVER:
                            dto1.setTargetType(SqlServerFieldTypeMappingEnum.getTargetTypeBySourceType(item.getType()));
                            break;
                    }
                }catch (Exception exception){
                    exception.printStackTrace();
                }

                return dto1;
            }).collect(Collectors.toList());

            dtoList.addAll(collect);
        });

        return dtoList;
    }
}
