package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.chartvisual.dto.chartVisual.ObtainTableDataDTO;
import com.fisk.chartvisual.dto.chartVisual.TableInfoDTO;
import com.fisk.chartvisual.dto.dsTable.*;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.entity.DsTableFieldPO;
import com.fisk.chartvisual.entity.DsTablePO;
import com.fisk.chartvisual.enums.MysqlFieldTypeMappingEnum;
import com.fisk.chartvisual.enums.SqlServerFieldTypeMappingEnum;
import com.fisk.chartvisual.enums.isExistTypeEnum;
import com.fisk.chartvisual.map.DsTableMap;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.mapper.DsTableFieldMapper;
import com.fisk.chartvisual.mapper.DsTableMapper;
import com.fisk.chartvisual.service.DsTableService;
import com.fisk.chartvisual.util.dbhelper.AbstractDbHelper;
import com.fisk.chartvisual.util.dbhelper.DbHelperFactory;
import com.fisk.chartvisual.util.dbhelper.buildsql.IBuildSqlCommand;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.fisk.chartvisual.enums.DataSourceInfoTypeEnum.*;
import static com.fisk.chartvisual.enums.isCheckedTypeEnum.*;
import static com.fisk.chartvisual.enums.isExistTypeEnum.*;

/**
 * @author WangYan
 * @date 2022/3/4 11:22
 */
@Service
public class DsTableServiceImpl extends ServiceImpl<DsTableFieldMapper, DsTableFieldPO> implements DsTableService {

    @Resource
    DataSourceConMapper sourceConMapper;
    @Resource
    DsTableFieldMapper dsTableFieldMapper;
    @Resource
    DsTableMapper dsTableMapper;
    @Resource
    DsTableServiceImpl dsTableService;

    @Override
    public List<DsTableDTO> getTableInfo(Integer id) {
        List<DsTableDTO> dtoList = new ArrayList<>();
        DataSourceConPO model = sourceConMapper.selectById(id);
        if (model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 查询数据源连接配置
        AbstractDbHelper db = DbHelperFactory.getDbHelper(model.conType);
        Connection connection = db.connection(model.conStr, model.conAccount, model.conPassword);
        DsTableDTO dto = this.dataSourceInfo(model, connection, db);
        dtoList.add(dto);
        return dtoList;
    }

    @Override
    public List<Map<String, Object>> getData(ObtainTableDataDTO dto) {
        DataSourceConPO model = sourceConMapper.selectById(dto.getId());
        if (model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 查询数据源连接配置
        AbstractDbHelper db = DbHelperFactory.getDbHelper(model.conType);
        Connection connection = db.connection(model.conStr, model.conAccount, model.conPassword);
        return this.getTableData(model, connection, db, dto);
    }

    @Override
    public List<TableInfoDTO> getTableStructure(TableStructureDTO dto) {
        DataSourceConPO model = sourceConMapper.selectById(dto.getId());
        if (model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 查询数据源连接配置
        AbstractDbHelper db = DbHelperFactory.getDbHelper(model.conType);
        Connection connection = db.connection(model.conStr, model.conAccount, model.conPassword);
        return this.getFieldInfo(model, connection, db, dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEntity<ResultEnum> saveTableInfo(List<UpdateDsTableDTO> dsTableDto) {
        dsTableDto.stream().filter(Objects::nonNull).map(item -> {
            boolean table = this.isExistTable(item.getDataSourceId(), item.getTableName());
            if (table == false) {
                // 修改
                this.updateTableInfo(item);
            }else {
                // 保存表名
                DsTablePO dsTable = new DsTablePO();
                dsTable.setDataSourceId(item.getDataSourceId());
                dsTable.setTableName(item.getTableName());
                int i = dsTableMapper.insert(dsTable);
                if (i <= 0) {
                    throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                }


                // 保存字段信息
                List<DsTableFieldPO> fieldList = item.getFieldList().stream().filter(Objects::nonNull).map(e -> {
                    return DsTableMap.INSTANCES.dtoToPo(e, dsTable.getId());
                }).collect(Collectors.toList());

                boolean saveBatch = dsTableService.saveBatch(fieldList);
                if (saveBatch == false) {
                    throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                }
            }

            return null;
        }).collect(Collectors.toList());

        return ResultEntityBuild.build(ResultEnum.SUCCESS);
    }

    /**
     * 判断数据源和表名是否存在
     *
     * @param dataSourceId
     * @param tableName
     * @return
     */
    public boolean isExistTable(Integer dataSourceId, String tableName) {
        QueryWrapper<DsTablePO> query = new QueryWrapper<>();
        query.lambda()
                .eq(DsTablePO::getDataSourceId, dataSourceId)
                .eq(DsTablePO::getTableName, tableName)
                .last("limit 1");
        DsTablePO dsTablePo = dsTableMapper.selectOne(query);
        if (dsTablePo == null) {
            return true;
        }

        return false;
    }

    @Override
    public List<DsFiledDTO> selectByDataSourceId(Integer dataSourceId) {
        QueryWrapper<DsTablePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DsTablePO::getDataSourceId, dataSourceId);
        List<DsTablePO> dsTableList = dsTableMapper.selectList(queryWrapper);

        if (CollectionUtils.isNotEmpty(dsTableList)) {
            // 查询表名字段信息
            List<DsFiledDTO> dtoList = dsTableList.stream().filter(Objects::nonNull).map(e -> {
                DsFiledDTO dto = new DsFiledDTO();
                dto.setId((int)e.getId());
                dto.setName(e.getTableName());

                QueryWrapper<DsTableFieldPO> query = new QueryWrapper<>();
                query.lambda().
                        eq(DsTableFieldPO::getTableInfoId, e.getId());
                List<DsTableFieldPO> tableFieldList = dsTableFieldMapper.selectList(query);
                if (CollectionUtils.isNotEmpty(tableFieldList)){
                    List<DsFiledDTO> collect = tableFieldList.stream().filter(Objects::nonNull)
                            .map(item -> new DsFiledDTO((int) item.getId(), item.getTargetField(), item.getTargetFieldType()))
                            .collect(Collectors.toList());
                    dto.setChildren(collect);
                }
                return dto;
            }).collect(Collectors.toList());

            return dtoList;
        }

        return null;
    }

    @Override
    public List<ShowDsTableDTO> getTableInfoStatus(Integer id) {
        DataSourceConPO model = sourceConMapper.selectById(id);
        if (model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 查询数据源连接配置
        AbstractDbHelper db = DbHelperFactory.getDbHelper(model.conType);
        Connection connection = db.connection(model.conStr, model.conAccount, model.conPassword);

        return this.dataSourceInfo(model, connection, db, id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEntity<ResultEnum> updateTableInfo(UpdateDsTableDTO dto) {
        QueryWrapper<DsTablePO> query = new QueryWrapper<>();
        query.lambda()
                .eq(DsTablePO::getDataSourceId, dto.getDataSourceId())
                .eq(DsTablePO::getTableName, dto.getTableName());
        DsTablePO tablePo = dsTableMapper.selectOne(query);
        if (tablePo == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS, ResultEnum.DATA_NOTEXISTS.getMsg());
        }

        // 先删除表
        ResultEnum resultEnum = this.deleteTable(tablePo);
        if (resultEnum == ResultEnum.SAVE_DATA_ERROR) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR, "删除表信息失败!");
        }

        // 保存新的表信息
        this.saveTable(dto);

        return ResultEntityBuild.build(ResultEnum.SUCCESS);
    }

    /**
     * 保存表和字段信息
     *
     * @param dto
     * @return
     */
    public ResultEnum saveTable(UpdateDsTableDTO dto) {
        // 保存表名
        DsTablePO tablePo = DsTableMap.INSTANCES.tableDtoToPo(dto.getDataSourceId(), dto.getTableName());
        int insert = dsTableMapper.insert(tablePo);
        if (insert <= 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        // 保存字段信息
        List<DsTableFieldPO> dsTableFieldList = dto.getFieldList().stream().filter(Objects::nonNull).map(item -> DsTableMap.INSTANCES.dtoToPo(item, tablePo.getId())).collect(Collectors.toList());

        boolean saveBatch = dsTableService.saveBatch(dsTableFieldList);
        if (saveBatch == false) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        return ResultEnum.SUCCESS;
    }

    /**
     * 删除库里的表和字段信息
     *
     * @param tablePo
     * @return
     */
    public ResultEnum deleteTable(DsTablePO tablePo) {
        // 先删除表信息
        QueryWrapper<DsTablePO> query = new QueryWrapper<>();
        query.lambda()
                .eq(DsTablePO::getDataSourceId, tablePo.getDataSourceId())
                .eq(DsTablePO::getTableName, tablePo.getTableName());
        int delete = dsTableMapper.delete(query);
        if (delete <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 再删除字段信息
        QueryWrapper<DsTableFieldPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DsTableFieldPO::getTableInfoId, tablePo.getId());
        int res = dsTableFieldMapper.delete(queryWrapper);
        if (res <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        return ResultEnum.SUCCESS;
    }

    /**
     * 获取数据源连接信息和库中是否存在
     *
     * @param model
     * @param connection
     * @param db
     * @param id
     * @return
     */
    public List<ShowDsTableDTO> dataSourceInfo(DataSourceConPO model, Connection connection, AbstractDbHelper db, Integer id) {
        IBuildSqlCommand buildSqlCommand = DbHelperFactory.getSqlBuilder(model.conType);
        // 该库下的表
        List<TableInfoDTO> resultList = db.execQueryResultList(buildSqlCommand.buildQueryAllTables(model.conDbname), connection, TableInfoDTO.class);

        AtomicInteger count = new AtomicInteger(1);
        // 查询表已经是否选中的状态
        List<ShowDsTableDTO> collect = resultList.stream().map(e -> {
            // 查询该表下的字段
            List<String> fields = db.execQueryResultList(buildSqlCommand.buildQueryFiled(model.conDbname, e.getTableName()),
                    connection, String.class);

            // 查询表名是否存在
            QueryWrapper<DsTablePO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(DsTablePO::getDataSourceId, id)
                    .eq(DsTablePO::getTableName, e.getTableName());
            DsTablePO tablePo = dsTableMapper.selectOne(queryWrapper);
            ShowDsTableDTO dto1;
            if (tablePo == null) {
                dto1 = new ShowDsTableDTO(count.getAndIncrement(),e.getTableName(), fields.size(), TABLE_NAME, NOT_CHECKED);
            } else {
                dto1 = new ShowDsTableDTO(count.getAndIncrement(),e.getTableName(), fields.size(), TABLE_NAME, CHECKED);
            }

            return dto1;
        }).collect(Collectors.toList());

        List<ShowDsTableDTO> list = new ArrayList<>();

        // 唯一标识
        int mark = 1;
        ShowDsTableDTO dto = new ShowDsTableDTO();
        dto.setId(mark++);
        dto.setName(model.getConDbname());
        dto.setCount(resultList.size());
        dto.setType(DATABASE_NAME);
        dto.setChildren(collect);
        list.add(dto);
        return list;
    }

    /**
     * 获取数据源连接信息
     *
     * @param model
     */
    public DsTableDTO dataSourceInfo(DataSourceConPO model, Connection connection, AbstractDbHelper db) {
        IBuildSqlCommand buildSqlCommand = DbHelperFactory.getSqlBuilder(model.conType);
        List<TableInfoDTO> resultList = db.execQueryResultList(buildSqlCommand.buildQueryAllTables(model.conDbname), connection, TableInfoDTO.class);

        DsTableDTO dto = new DsTableDTO();
        dto.setName(model.getConDbname());
        dto.setCount(resultList.size());
        dto.setType(DATABASE_NAME);

        dto.setChildren(
                resultList.stream().map(e -> {
                    List<String> fields = db.execQueryResultList(buildSqlCommand.buildQueryFiled(model.conDbname, e.getTableName()),
                            connection, String.class);

                    return new DsTableDTO(e.getTableName(), fields.size(), TABLE_NAME);
                }).collect(Collectors.toList())
        );

        return dto;
    }

    /**
     * 获取表预览数据
     *
     * @param model
     * @param connection
     * @param db
     */
    public List<Map<String, Object>> getTableData(DataSourceConPO model, Connection connection, AbstractDbHelper db, ObtainTableDataDTO dto) {
        IBuildSqlCommand buildSqlCommand = DbHelperFactory.getSqlBuilder(model.conType);

        // 查询表字段信息
        List<Map<String, Object>> fieldList = db.execQueryResultMaps(buildSqlCommand.buildQueryFiledInfo(dto.getTableName()), connection);

        String field = fieldList.get(0).get("field").toString();
        Integer total = dto.getTotal();
        if (total == null) {
            total = 20;
        }
        return db.execQueryResultMaps(buildSqlCommand.getData(dto.getTableName(), total, field), connection);
    }

    /**
     * 获取表字段信息
     *
     * @param model
     * @param connection
     * @param db
     * @param dto
     * @return
     */
    public List<TableInfoDTO> getFieldInfo(DataSourceConPO model, Connection connection, AbstractDbHelper db, TableStructureDTO dto) {
        IBuildSqlCommand buildSqlCommand = DbHelperFactory.getSqlBuilder(model.conType);

        List<TableInfoDTO> dtoList = new ArrayList<>();
        AtomicInteger count = new AtomicInteger(1);
        dto.getTableName().stream().filter(Objects::nonNull).forEach(e -> {
            List<FieldInfoDTO> fieldList = db.execQueryResultList(buildSqlCommand.buildQueryFiledInfo(e), connection, FieldInfoDTO.class);

            // 字段类型匹配
            List<FieldInfoDTO> collect = fieldList.stream().filter(Objects::nonNull).map(item -> {
                FieldInfoDTO dto1 = new FieldInfoDTO();
                dto1.setId(count.getAndIncrement());
                dto1.setField(item.getField());
                dto1.setTargetField(item.getField());
                dto1.setType(item.getType());
                dto1.setFieldInfo(item.getFieldInfo());

                try {
                    // 类型匹配
                    switch (model.conType) {
                        case MYSQL:
                            dto1.setTargetType(MysqlFieldTypeMappingEnum.getTargetTypeBySourceType(item.getType()));
                            break;
                        case SQLSERVER:
                            dto1.setTargetType(SqlServerFieldTypeMappingEnum.getTargetTypeBySourceType(item.getType()));
                            break;
                        default:
                            throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                // 匹配库里的目标字段、目标类型、描述
                this.matchField(dto1, dto.getId(), e, item.getField());

                return dto1;
            }).collect(Collectors.toList());

            // 一个表名对应多个字段信息
            TableInfoDTO dto1 = new TableInfoDTO();
            dto1.setTableName(e);
            dto1.setDtoList(collect);
            dto1.setIsExist(this.tableIsExist(dto.getId(), e));

            dtoList.add(dto1);
        });

        return dtoList;
    }

    /**
     * 查询该数据源的下的表名是否在库里存在
     */
    public isExistTypeEnum tableIsExist(Integer dataSourceId, String tableName) {
        QueryWrapper<DsTablePO> query = new QueryWrapper<>();
        query.lambda()
                .eq(DsTablePO::getDataSourceId, dataSourceId)
                .eq(DsTablePO::getTableName, tableName);
        DsTablePO dsTablePo = dsTableMapper.selectOne(query);
        if (dsTablePo == null) {
            return NOT_Exist;
        } else {
            return EXIST;
        }
    }

    /**
     * 查询该数据源的下的字段名是否在库里存在
     *
     * @param dataSourceId
     * @param tableName
     * @param fieldName
     */
    public DsTableFieldPO fieldIsExist(Integer dataSourceId, String tableName, String fieldName) {
        QueryWrapper<DsTablePO> query = new QueryWrapper<>();
        query.lambda()
                .eq(DsTablePO::getDataSourceId, dataSourceId)
                .eq(DsTablePO::getTableName, tableName);
        DsTablePO dsTablePo = dsTableMapper.selectOne(query);
        if (dsTablePo != null) {
            QueryWrapper<DsTableFieldPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(DsTableFieldPO::getTableInfoId, dsTablePo.getId())
                    .eq(DsTableFieldPO::getSourceField, fieldName);

            DsTableFieldPO fieldPo = dsTableFieldMapper.selectOne(queryWrapper);
            if (fieldPo != null) {
                return fieldPo;
            }
        }

        return null;
    }

    /**
     * 匹配库里的目标字段、目标类型、描述
     *
     * @param dto1
     * @param dataSourceId
     * @param tableName
     * @param fieldName
     */
    public void matchField(FieldInfoDTO dto1, Integer dataSourceId, String tableName, String fieldName) {
        // 判断字段是否在数据库存在
        DsTableFieldPO dsTableFieldPo = this.fieldIsExist(dataSourceId, tableName, fieldName);
        isExistTypeEnum type = null;
        if (dsTableFieldPo == null) {
            type = NOT_Exist;
        } else {
            type = EXIST;
            // 目标字段和目标类型
            dto1.setTargetField(dsTableFieldPo.getTargetField());
            dto1.setTargetType(dsTableFieldPo.getTargetFieldType());
            dto1.setFieldInfo(dsTableFieldPo.getDescribe());
        }
        dto1.setFieldIsExist(type);
    }
}
