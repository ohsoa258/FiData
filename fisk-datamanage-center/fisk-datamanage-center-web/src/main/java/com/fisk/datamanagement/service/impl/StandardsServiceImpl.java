package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.NifiConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.core.utils.dbutils.utils.MySqlConUtils;
import com.fisk.common.core.utils.dbutils.utils.OracleUtils;
import com.fisk.common.core.utils.dbutils.utils.PgSqlUtils;
import com.fisk.common.core.utils.dbutils.utils.SqlServerUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbMetaData.utils.DorisConUtils;
import com.fisk.datamanagement.dto.standards.*;
import com.fisk.datamanagement.entity.StandardsBeCitedPO;
import com.fisk.datamanagement.entity.StandardsMenuPO;
import com.fisk.datamanagement.entity.StandardsPO;
import com.fisk.datamanagement.map.StandardsBeCitedMap;
import com.fisk.datamanagement.map.StandardsMap;
import com.fisk.datamanagement.mapper.StandardsMapper;
import com.fisk.datamanagement.service.StandardsBeCitedService;
import com.fisk.datamanagement.service.StandardsMenuService;
import com.fisk.datamanagement.service.StandardsService;
import com.fisk.datamanagement.utils.freemarker.FreeMarkerUtils;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service("standardsService")
public class StandardsServiceImpl extends ServiceImpl<StandardsMapper, StandardsPO> implements StandardsService {

    @Resource
    StandardsMenuService standardsMenuService;
    @Resource
    StandardsBeCitedService standardsBeCitedService;

    @Resource
    UserClient userClient;

    @Override
    public StandardsDTO getStandards(int id) {
        LambdaQueryWrapper<StandardsPO> queryStandards = new LambdaQueryWrapper<>();
        queryStandards.eq(StandardsPO::getMenuId,id);
        StandardsPO standardsPO = this.getOne(queryStandards);
        if (standardsPO == null){
            return null;
        }
        LambdaQueryWrapper<StandardsBeCitedPO> queryBeCited = new LambdaQueryWrapper<>();
        queryBeCited.eq(StandardsBeCitedPO::getStandardsId,standardsPO.getId());
        List<StandardsBeCitedPO> standardsBeCitedPOList = standardsBeCitedService.list(queryBeCited);
        List<StandardsBeCitedDTO> standardsBeCitedDTOList = standardsBeCitedPOList.stream().map(StandardsBeCitedMap.INSTANCES::poToDTO).collect(Collectors.toList());
        StandardsDTO standardsDTO = StandardsMap.INSTANCES.poToDTO(standardsPO);
        standardsDTO.setStandardsBeCitedDTOList(standardsBeCitedDTOList);
        return standardsDTO;
    }

    @Override
    public ResultEnum addStandards(StandardsDTO standardsDTO) {

        //查询排序添加位置
        LambdaQueryWrapper<StandardsMenuPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StandardsMenuPO::getPid,standardsDTO.getMenuId());
        queryWrapper.orderByDesc(StandardsMenuPO::getSort);
        queryWrapper.last("LIMIT 1");
        StandardsMenuPO tragetMenu = standardsMenuService.getOne(queryWrapper);
        //添加标签并排序

        StandardsMenuPO standardsMenuPO = new StandardsMenuPO();
        standardsMenuPO.setPid(standardsDTO.getMenuId());
        standardsMenuPO.setType(2);
        standardsMenuPO.setName(standardsDTO.getChineseName());
        if (tragetMenu == null){
            standardsMenuPO.setSort(1);
        }else {
            standardsMenuPO.setSort(tragetMenu.getSort()+1);
        }
        standardsMenuService.save(standardsMenuPO);
        standardsDTO.setMenuId((int)standardsMenuPO.getId());

        LambdaQueryWrapper<StandardsPO> queryStandards = new LambdaQueryWrapper<>();
        queryStandards.eq(StandardsPO::getMenuId,standardsDTO.getMenuId());
        StandardsPO standard = this.getOne(queryStandards);
        if (standard != null){
            return ResultEnum.DATA_EXISTS;
        }
        //添加数据标准
        StandardsPO standardsPO = StandardsMap.INSTANCES.dtoToPo(standardsDTO);
        save(standardsPO);
        List<StandardsBeCitedDTO> standardsBeCitedDTOList = standardsDTO.getStandardsBeCitedDTOList();
        if (!CollectionUtils.isEmpty(standardsBeCitedDTOList)){
            List<StandardsBeCitedPO> standardsBeCitedPOS = standardsBeCitedDTOList.stream().map(StandardsBeCitedMap.INSTANCES::dtoToPo).collect(Collectors.toList());
            standardsBeCitedPOS = standardsBeCitedPOS.stream().map(i -> {
                i.setStandardsId((int) standardsPO.getId());
                return i;
            }).collect(Collectors.toList());
            standardsBeCitedService.saveBatch(standardsBeCitedPOS);
        }

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum updateStandards(StandardsDTO standardsDTO) {
        StandardsPO standardsPO = StandardsMap.INSTANCES.dtoToPo(standardsDTO);
        updateById(standardsPO);
        List<StandardsBeCitedDTO> standardsBeCitedDTOList = standardsDTO.getStandardsBeCitedDTOList();

        if (!CollectionUtils.isEmpty(standardsBeCitedDTOList)){
            //DTO数据类型转PO
            List<StandardsBeCitedPO> standardsBeCitedPOS = standardsBeCitedDTOList.stream().map(StandardsBeCitedMap.INSTANCES::dtoToPo).collect(Collectors.toList());
            //查询未修改时详情数据
            LambdaQueryWrapper<StandardsBeCitedPO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StandardsBeCitedPO::getStandardsId,standardsDTO.getId());
            List<StandardsBeCitedPO> all = standardsBeCitedService.list(queryWrapper);
            //找出待删除数据id
            List<Integer> Ids = standardsBeCitedDTOList.stream().map(StandardsBeCitedDTO::getId).filter(Objects::nonNull).collect(Collectors.toList());
            List<StandardsBeCitedPO> dels = new ArrayList<>();
            if (!CollectionUtils.isEmpty(all)){
                if (!CollectionUtils.isEmpty(Ids)){
                    dels = all.stream().filter(i -> !Ids.contains((int)i.getId())).collect(Collectors.toList());
                    List<Long> delIds = dels.stream().map(StandardsBeCitedPO::getId).collect(Collectors.toList());
                    standardsBeCitedService.removeByIds(delIds);
                }else {
                    List<Long> delIds = all.stream().map(StandardsBeCitedPO::getId).collect(Collectors.toList());
                    standardsBeCitedService.removeByIds(delIds);
                }
            }

            //找出待添加数据
            List<StandardsBeCitedPO> adds = standardsBeCitedPOS.stream().filter(i -> i.getId() == 0).collect(Collectors.toList());
            //找出待修改数据
            List<StandardsBeCitedPO> updates = standardsBeCitedPOS.stream().filter(i -> i.getId() != 0).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(adds)){
                standardsBeCitedService.saveBatch(adds);
            }
            if (!CollectionUtils.isEmpty(updates)){
                standardsBeCitedService.updateBatchById(updates);
            }
        }
        Integer menuId = standardsDTO.getMenuId();
        StandardsMenuPO standardsMenuPO = standardsMenuService.getById(menuId);
        standardsMenuPO.setName(standardsDTO.getChineseName());
        standardsMenuService.save(standardsMenuPO);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum delStandards(int id) {

        StandardsMenuPO standardsMenuServiceById = standardsMenuService.getById(id);
        standardsMenuService.removeById(id);
        LambdaQueryWrapper<StandardsPO> queryStandard = new LambdaQueryWrapper<>();
        queryStandard.eq(StandardsPO::getMenuId,standardsMenuServiceById.getId());
        StandardsPO standardsPO = getOne(queryStandard);
        if (standardsPO !=null){
            removeById(standardsPO.id);
            LambdaQueryWrapper<StandardsBeCitedPO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StandardsBeCitedPO::getStandardsId,standardsPO.id);
            standardsBeCitedService.remove(queryWrapper);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum delStandards(List<Integer> ids) {
        standardsMenuService.removeByIds(ids);
        LambdaQueryWrapper<StandardsPO> queryStandard = new LambdaQueryWrapper<>();
        queryStandard.in(StandardsPO::getMenuId,ids);
        List<StandardsPO> standardsPOS = list(queryStandard);
        List<Long> standardsIds = standardsPOS.stream().map(StandardsPO::getId).collect(Collectors.toList());
        removeByIds(standardsIds);
        LambdaQueryWrapper<StandardsBeCitedPO> queryBeCited = new LambdaQueryWrapper<>();
        queryBeCited.in(StandardsBeCitedPO::getStandardsId,standardsIds);
        standardsBeCitedService.remove(queryBeCited);
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<DataSourceInfoDTO> getDataSourceTree() {
        ResultEntity<List<DataSourceDTO>> all = userClient.getAll();
        if (all.getCode() != ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(all.data)) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        List<DataSourceDTO> allDatasources = all.data.stream().filter(i -> i.getSourceType() == 1).collect(Collectors.toList());

        List<DataSourceInfoDTO> list = new ArrayList<>();

        for (DataSourceDTO item : allDatasources) {
            DataSourceInfoDTO data = new DataSourceInfoDTO();
            data.dbId = item.id;
            data.dataSourceName = item.conDbname;
            DataBaseInfoDTO dataBaseInfoDTO = new DataBaseInfoDTO();
            dataBaseInfoDTO.setDbName(item.name);
            dataBaseInfoDTO.setTableNameList(getDbTable(item));
            List<DataBaseInfoDTO> dataBaseInfoDTOList = new ArrayList<>();
            dataBaseInfoDTOList.add(dataBaseInfoDTO);
            data.setDataBaseInfoDTOList(dataBaseInfoDTOList);
            list.add(data);
        }
        return list;
    }

    @Override
    public QueryResultDTO preview(QueryDTO query) {
        QueryResultDTO array = new QueryResultDTO();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        ResultEntity<DataSourceDTO> dataSourceConfig = null;
        try {
            dataSourceConfig = userClient.getFiDataDataSourceById(query.dataSourceId);
            if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
            }
            conn = getConnection(dataSourceConfig.data);
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            st.setMaxRows(10);
            query.setQuerySql("select * from "+ query.tableName);
            rs = st.executeQuery(query.getQuerySql());
            // 获取数据集
            array = resultSetToJsonArrayDataAccess(rs);
            array.sql = "select * from "+ query.tableName;
            array.total = array.dataArray.size();
        } catch (SQLException e) {
            log.error("preview ex:", e);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, ":" + e.getMessage());
        } finally {
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return array;
    }

    /**
     * 数据库连接
     *
     * @param dto
     * @return
     */
    public Connection getConnection(DataSourceDTO dto) {
        AbstractCommonDbHelper dbHelper = new AbstractCommonDbHelper();
        Connection connection = dbHelper.connection(dto.conStr, dto.conAccount,
                dto.conPassword, dto.conType);
        return connection;
    }

    public static QueryResultDTO resultSetToJsonArrayDataAccess(ResultSet rs) throws SQLException, JSONException {
        QueryResultDTO data = new QueryResultDTO();
        // json数组
        JSONArray array = new JSONArray();
        // 获取列数
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        // 遍历ResultSet中的每条数据
        int count = 1;
        // 预览展示10行
        int row = 10;
        while (rs.next() && count <= row) {
            JSONObject jsonObj = new JSONObject();
            // 遍历每一列
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                //过滤ods表中pk和code默认字段
                String tableName = metaData.getTableName(i) + "key";
                if (NifiConstants.AttrConstants.FIDATA_BATCH_CODE.equals(columnName) || tableName.equals("ods_" + columnName)) {
                    continue;
                }
                //获取sql查询数据集合
                String value = rs.getString(columnName);
                jsonObj.put(columnName, value);
            }
            count++;
            array.add(jsonObj);
        }
        data.dataArray = array;
        return data;
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
                case DORIS:
                    data = DorisConUtils.getTableName(conn);
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

    @Override
    public List<TableColumnDTO> getColumn(ColumnQueryDTO dto) {
        //获取数据源
        ResultEntity<DataSourceDTO> dataSource = userClient.getFiDataDataSourceById(dto.getDbId());
        if (dataSource.getCode() != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        if (dataSource.data == null) {
            return new ArrayList<>();
        }

        return getColumnByTableName(dataSource.data, dto.tableName);
    }

    @Override
    public void exportStandards(List<Integer> ids, HttpServletResponse response) {

        List<StandardsMenuPO> standardsMenuPOS = standardsMenuService.listByIds(ids);
        List<StandardsMenuDataDTO> standardsMenuDataDTOS = standardsMenuPOS.stream().map(i -> {
            StandardsMenuDataDTO standardsMenuDataDTO = new StandardsMenuDataDTO();
            standardsMenuDataDTO.setId((int) i.getId());
            standardsMenuDataDTO.setMenuName(i.getName());
            return standardsMenuDataDTO;
        }).collect(Collectors.toList());
        //根据标签id查询需要导出的数据
        LambdaQueryWrapper<StandardsPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(StandardsPO::getMenuId,ids);
        List<StandardsPO> standardsPOS = this.list(queryWrapper);
        List<StandardsDTO> standardsDTOS = StandardsMap.INSTANCES.poListToDTOList(standardsPOS);
        //查询关联数据元表数据
        List<Integer> standardsIds = standardsDTOS.stream().map(StandardsDTO::getId).collect(Collectors.toList());
        LambdaQueryWrapper<StandardsBeCitedPO> queryBeCited = new LambdaQueryWrapper<>();
        queryBeCited.in(StandardsBeCitedPO::getStandardsId,standardsIds);
        List<StandardsBeCitedPO> standardsBeCitedPOS = standardsBeCitedService.list(queryBeCited);
        List<StandardsBeCitedDTO> standardsBeCitedDTOS = StandardsBeCitedMap.INSTANCES.poListToDTOList(standardsBeCitedPOS);
        Map<Integer, List<StandardsBeCitedDTO>> standardsBeCitedMap = standardsBeCitedDTOS.stream().collect(groupingBy(StandardsBeCitedDTO::getStandardsId));
        //组装数据标准
        standardsDTOS = standardsDTOS.stream().map(i->{
            List<StandardsBeCitedDTO> list = standardsBeCitedMap.get(i.getId());
            if (!CollectionUtils.isEmpty(list)){
                i.setStandardsBeCitedDTOList(list);
                i.setNum(list.size());
            }
            return i;
        }).collect(Collectors.toList());
        Map<Integer, List<StandardsDTO>> standardsMap = standardsDTOS.stream().collect(groupingBy(StandardsDTO::getMenuId));
        standardsMenuDataDTOS = standardsMenuDataDTOS.stream().map(i -> {
            List<StandardsDTO> standardsDTOS1 = standardsMap.get(i.getId());
            if (!CollectionUtils.isEmpty(standardsDTOS1)) {
                i.setStandard(standardsDTOS1.get(0));
            }
            return i;
        }).collect(Collectors.toList());
        String templateName = "dataStandard.xml";
        String fileName = "数据标准.doc";
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("standards",standardsMenuDataDTOS);
        // 执行导出
        FreeMarkerUtils.exportWord(templateName, fileName, dataModel,response);
    }

    @Override
    public ResultEnum standardsSort(StandardsSortDTO dto) {
        Integer tragetId = dto.getTragetId();
        StandardsMenuPO standardsMenuPO = standardsMenuService.getById(dto.getMenuId());
        if (tragetId == null || tragetId == 0){
            LambdaQueryWrapper<StandardsMenuPO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StandardsMenuPO::getPid,standardsMenuPO.getPid());
            queryWrapper.lt(StandardsMenuPO::getSort,standardsMenuPO.getSort());
            List<StandardsMenuPO> list = standardsMenuService.list(queryWrapper);
            for (StandardsMenuPO menuPO : list) {
                menuPO.setSort(menuPO.getSort()+1);
                standardsMenuService.updateById(menuPO);
            }
            standardsMenuPO.setSort(1);
            standardsMenuService.updateById(standardsMenuPO);
        }else {
            StandardsMenuPO tragetMenuPO = standardsMenuService.getById(tragetId);
            if (tragetMenuPO.getSort()>standardsMenuPO.getSort()){
                LambdaQueryWrapper<StandardsMenuPO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(StandardsMenuPO::getPid,standardsMenuPO.getPid());
                queryWrapper.gt(StandardsMenuPO::getSort,standardsMenuPO.getSort());
                queryWrapper.le(StandardsMenuPO::getSort,tragetMenuPO.getSort());
                List<StandardsMenuPO> list = standardsMenuService.list(queryWrapper);
                for (StandardsMenuPO menuPO : list) {
                    menuPO.setSort(menuPO.getSort()-1);
                    standardsMenuService.updateById(menuPO);
                }
                standardsMenuPO.setSort(tragetMenuPO.getSort());
                standardsMenuService.updateById(standardsMenuPO);
            }else if (tragetMenuPO.getSort()<standardsMenuPO.getSort()){
                LambdaQueryWrapper<StandardsMenuPO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(StandardsMenuPO::getPid,standardsMenuPO.getPid());
                queryWrapper.gt(StandardsMenuPO::getSort,tragetMenuPO.getSort());
                queryWrapper.lt(StandardsMenuPO::getSort,standardsMenuPO.getSort());
                List<StandardsMenuPO> list = standardsMenuService.list(queryWrapper);
                for (StandardsMenuPO menuPO : list) {
                    menuPO.setSort(menuPO.getSort()+1);
                    standardsMenuService.updateById(menuPO);
                }
                standardsMenuPO.setSort(tragetMenuPO.getSort()+1);
                standardsMenuService.updateById(standardsMenuPO);
            }
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 根据表名获取字段
     *
     * @param dto
     * @param tableName
     * @return
     */
    public List<TableColumnDTO> getColumnByTableName(DataSourceDTO dto, String tableName) {
        Connection conn = null;
        try {
            AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
            conn = commonDbHelper.connection(dto.conStr, dto.conAccount, dto.conPassword, dto.conType);
            List<TableColumnDTO> data = new ArrayList<>();
            switch (dto.conType) {
                case MYSQL:
                    data = MySqlConUtils.getColNames(conn, tableName);
                    break;
                case SQLSERVER:
                    data = SqlServerUtils.getColumnsName(conn, tableName);
                    break;
                case POSTGRESQL:
                    data = PgSqlUtils.getTableColumnName(conn, tableName);
                    break;
                case ORACLE:
                    data = OracleUtils.getTableColumnInfoList(conn, dto.conDbname, tableName);
                    break;
                case DORIS:
                    data = DorisConUtils.getColNames(conn, tableName);
                    break;
                default:
                    throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
            }
            return data;
        } catch (Exception e) {
            log.error("【获取表信息失败】,{}", e);
            return null;
        }finally {
            AbstractCommonDbHelper.closeConnection(conn);
        }
    }
}
