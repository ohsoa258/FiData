package com.fisk.dataservice.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.CreateSchemaSqlUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.datafactory.enums.DelFlagEnum;
import com.fisk.dataservice.dto.dataanalysisview.DataViewAccountDTO;
import com.fisk.dataservice.dto.dataanalysisview.DataViewThemeDTO;
import com.fisk.dataservice.entity.DataViewAccountPO;
import com.fisk.dataservice.entity.DataViewPO;
import com.fisk.dataservice.entity.DataViewRolePO;
import com.fisk.dataservice.entity.DataViewThemePO;
import com.fisk.dataservice.enums.AccountJurisdictionEnum;
import com.fisk.dataservice.map.DataViewMap;
import com.fisk.dataservice.mapper.DataViewAccountMapper;
import com.fisk.dataservice.mapper.DataViewMapper;
import com.fisk.dataservice.mapper.DataViewRoleMapper;
import com.fisk.dataservice.mapper.DataViewThemeMapper;
import com.fisk.dataservice.service.IDataViewThemeService;
import com.fisk.dataservice.vo.dataanalysisview.DataSourceVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Service
@Slf4j
public class DataViewThemeServiceImpl
        extends ServiceImpl<DataViewThemeMapper, DataViewThemePO>
        implements IDataViewThemeService {

    @Resource
    private DataViewThemeMapper baseMapper;

    @Resource
    private UserClient userClient;

    @Resource
    private DataViewAccountMapper dataViewAccountMapper;

    @Resource
    private DataViewMapper dataViewMapper;

    @Resource
    private DataViewRoleMapper dataViewRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addViewTheme(DataViewThemeDTO dto) {
        log.info("保存数据视图主题参数，[{}]", JSON.toJSONString(dto));

        // 查询视图主题是否已经存在
        QueryWrapper<DataViewThemePO> qw = new QueryWrapper<>();
        qw.lambda().eq(DataViewThemePO::getThemeName, dto.getThemeName())
                .eq(DataViewThemePO::getDelFlag, DelFlagEnum.NORMAL_FLAG.getValue());
        DataViewThemePO prePo = baseMapper.selectOne(qw);
        if (!Objects.isNull(prePo)){
            throw new FkException(ResultEnum.DS_VIEW_THEME_EXISTS);
        }

        // 校验数据源
        DataSourceDTO dataSourceDTO = verifyDataSource(dto.getTargetDbId());

        // 校验未选择架构的情况下是否存在重复简称
        Integer count = baseMapper.selectAbbr(dto.getTargetDbId(), dto.getThemeAbbr());
        if (count != null && count > 0){
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "当前简称已被其他主题使用");
        }

        // 校验架构是否已经存在，不包含则创建架构
        boolean schemaFlag = false;
        List<String> abbrList = baseMapper.getAbbreviation(dataSourceDTO.getId(), DelFlagEnum.NORMAL_FLAG.getValue());
        if (dto.getWhetherSchema() && abbrList.contains(dto.getThemeAbbr())){
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "当前架构已被其他主题使用");
        }
        if (dto.getWhetherSchema() && !abbrList.contains(dto.getThemeAbbr())){
            schemaFlag = true;
        }

        // dto -> po
        DataViewThemePO model = DataViewMap.INSTANCES.dtoToPo(dto);
        boolean save = this.save(model);
        if (!save){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 创建架构
        if (schemaFlag){
            createSchema(model.getThemeAbbr(), model.getTargetDbId());
        }

        // 处理关联账号
        if (!CollectionUtils.isEmpty(dto.getRelAccountList())){
            Integer viewThemeId = baseMapper.selectViewThemeId(dto.getThemeName(), DelFlagEnum.NORMAL_FLAG.getValue());
            saveRelationAccount(dto.getRelAccountList(), viewThemeId, dataSourceDTO, null);
        }

        return ResultEnum.SUCCESS;
    }

    private void saveRelationAccount(List<DataViewAccountDTO> dtoList, Integer viewThemeId, DataSourceDTO dataSourceDTO, String type){
        log.info("视图主题id，[{}, 账号集合，{}]", viewThemeId, JSON.toJSONString(dtoList));

        // 创建登录用户、数据库用户、并关联角色信息
        if (StringUtils.isEmpty(type)){
            List<String> nameList = dataViewAccountMapper.selectNameList(dataSourceDTO.getId(), DelFlagEnum.NORMAL_FLAG.getValue());
            List<String> dataList = dtoList.stream().map(DataViewAccountDTO::getAccountName).collect(Collectors.toList());
            for (String item : dataList){
                if (nameList.contains(item)){
                    throw new FkException(ResultEnum.SAVE_DATA_ERROR, item + "：该账号已存在");
                }
            }
            // 创建数据库角色
            createRole(dataSourceDTO, viewThemeId);

            for (DataViewAccountDTO dto : dtoList){
                if (StringUtils.isEmpty(dto.getAccountName()) || StringUtils.isEmpty(dto.getAccountPsd())){
                    throw new FkException(ResultEnum.DS_VIEW_THEME_ACCOUNT_ERROR);
                }
                DataViewAccountPO po = new DataViewAccountPO();
                po.setViewThemeId(viewThemeId);
                po.setAccountName(dto.getAccountName());
                po.setAccountDesc(dto.getAccountDesc());
                po.setAccountPsd(dto.getAccountPsd());
                po.setJurisdiction(AccountJurisdictionEnum.READ_ONLY.getName());
                int insert = dataViewAccountMapper.insert(po);
                if (insert <= 0){
                    throw new FkException(ResultEnum.DS_VIEW_THEME_ACCOUNT_SAVE);

                }

                // 不存在用户时，则将新加入的用户设置到目标数据库中
                String sql = null;
                if (DataSourceTypeEnum.SQLSERVER.getName().equalsIgnoreCase(dataSourceDTO.conType.getName())){
                    sql = "CREATE LOGIN " + po.getAccountName() + " with " + " PASSWORD=" + "'" + po.getAccountPsd() + "'";
                    execSql(sql, dataSourceDTO);
                    sql = "create user " + po.getAccountName() + " for login " + po.getAccountName();
                    execSql(sql, dataSourceDTO);
                }else if (DataSourceTypeEnum.POSTGRESQL.getName().equalsIgnoreCase(dataSourceDTO.conType.getName())){
                    sql = "CREATE USER "+ po.getAccountName() + " WITH PASSWORD " + "'" + po.getAccountPsd() + "'";
                    execSql(sql, dataSourceDTO);
                }

                // 创建数据库用户并关联角色
                relationRole(dataSourceDTO, viewThemeId, po);
            }
        }else{
            for (DataViewAccountDTO dto : dtoList){
                if (StringUtils.isEmpty(dto.getAccountName()) || StringUtils.isEmpty(dto.getAccountPsd())){
                    throw new FkException(ResultEnum.DS_VIEW_THEME_ACCOUNT_ERROR);
                }
                DataViewAccountPO po = new DataViewAccountPO();
                po.setViewThemeId(viewThemeId);
                po.setAccountName(dto.getAccountName());
                po.setAccountDesc(dto.getAccountDesc());
                po.setAccountPsd(dto.getAccountPsd());
                po.setJurisdiction(AccountJurisdictionEnum.READ_ONLY.getName());

                // 不存在用户时，则将新加入的用户设置到目标数据库中
                String sql = null;
                if (DataSourceTypeEnum.SQLSERVER.getName().equalsIgnoreCase(dataSourceDTO.conType.getName())){
                    sql = "CREATE LOGIN " + po.getAccountName() + " with " + " PASSWORD=" + "'" + po.getAccountPsd() + "'";
                    execSql(sql, dataSourceDTO);
                    sql = "create user " + po.getAccountName() + " for login " + po.getAccountName();
                    execSql(sql, dataSourceDTO);
                }else if (DataSourceTypeEnum.POSTGRESQL.getName().equalsIgnoreCase(dataSourceDTO.conType.getName())){
                    sql = "CREATE USER "+ po.getAccountName() + " WITH PASSWORD " + "'" + po.getAccountPsd() + "'";
                    execSql(sql, dataSourceDTO);
                }

                // 创建数据库用户并关联角色
                relationRole(dataSourceDTO, viewThemeId, po);
            }
        }

    }

    private void relationRole(DataSourceDTO dataSourceDTO, Integer viewThemeId, DataViewAccountPO po){
        log.info("开始关联数据库角色，【{}{}】", viewThemeId, JSON.toJSONString(po));
        DataViewThemePO dataViewThemePO = baseMapper.selectById(viewThemeId);
        String roleName = dataSourceDTO.conDbname + "_viewThemeRole_" + dataViewThemePO.getThemeAbbr();
        String relationSql = "exec sp_addrolemember " + roleName + "," + po.getAccountName();
        if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.POSTGRESQL.getName())){
            relationSql = "grant " + roleName + " to " + po.getAccountName();
        }
        execSql(relationSql, dataSourceDTO);
        log.info("关联数据库角色结束");

        // 更新角色视图权限信息
        updateRoleViewInfo(roleName, viewThemeId, dataSourceDTO);
    }

    private void updateRoleViewInfo(String roleName, Integer viewThemeId, DataSourceDTO dataSourceDTO) {
        // 查询架构
        DataViewThemePO dataViewThemePO = baseMapper.selectById(viewThemeId);
        QueryWrapper<DataViewPO> qw = new QueryWrapper<>();
        qw.lambda().eq(DataViewPO::getViewThemeId, viewThemeId).eq(DataViewPO::getDelFlag, DelFlagEnum.NORMAL_FLAG.getValue());
        List<DataViewPO> dataViewPOList = dataViewMapper.selectList(qw);
        try {
            AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
            Connection connection = null;
            if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.SQLSERVER.getName())){
                connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                        dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.SQLSERVER);
            }else if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.POSTGRESQL.getName())){
                connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                        dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.PG);
            }
            if (!CollectionUtils.isEmpty(dataViewPOList)){
                for (DataViewPO item : dataViewPOList){
                    if (!dataViewThemePO.getWhetherSchema()){
                        dataViewThemePO.setThemeAbbr("dbo");
                        if (dataSourceDTO.conType.getName().contains(DataSourceTypeEnum.POSTGRESQL.getName())){
                            dataViewThemePO.setThemeAbbr("public");
                        }
                    }
                    String sql = "GRANT SELECT ON " + dataViewThemePO.getThemeAbbr() + "." + item.getName() + " TO " + roleName;
                    abstractDbHelper.executeSql(sql, connection);
                }
            }
        } catch (SQLException e) {
            log.error("数据分析视图目标数据库执行sql失败,", e);
        }
    }

    /**
     * 创建数据库角色信息
     * @param dataSourceDTO 数据源
     * @param viewThemeId 数据视图主题id
     */
    private void createRole(DataSourceDTO dataSourceDTO, Integer viewThemeId){
        DataViewThemePO dataViewThemePO = baseMapper.selectById(viewThemeId);
        String roleName = dataSourceDTO.conDbname + "_viewThemeRole_" + dataViewThemePO.getThemeAbbr();
        String roleSql = "exec sp_addrole " + roleName;
        if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.POSTGRESQL.getName())){
            roleSql = "create role " + roleName;
        }

        // 存储入库
        DataViewRolePO roleModel = new DataViewRolePO();
        roleModel.setDbName(dataSourceDTO.conDbname);
        roleModel.setThemeId(viewThemeId);
        roleModel.setRoleName(roleName);

        QueryWrapper<DataViewRolePO> qw = new QueryWrapper<>();
        qw.lambda().eq(DataViewRolePO::getDbName, dataSourceDTO.getConDbname())
                .eq(DataViewRolePO::getRoleName, roleName)
                .eq(DataViewRolePO::getDelFlag, DelFlagEnum.NORMAL_FLAG.getValue());
        Integer roleCount = dataViewRoleMapper.selectCount(qw);
        if (roleCount <= 0){
            int insertFlag = dataViewRoleMapper.insert(roleModel);
            if (insertFlag <= 0){
                throw new FkException(ResultEnum.SAVE_DATA_ERROR, "添加数据库角色失败");
            }
        }
        try{
            // 执行sql
            AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
            Connection connection = null;
            if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.SQLSERVER.getName())){
                connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                        dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.SQLSERVER);
            }else if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.POSTGRESQL.getName())){
                connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                        dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.PG);
            }
            abstractDbHelper.executeSql(roleSql, connection);
            log.info("数据分析视图服务sql执行结束,[{}]", roleSql);
        }catch (Exception e){
            log.error("执行sql失败",e);
        }
    }

    private void execSql(String sql, DataSourceDTO dataSourceDTO){
        try {
            AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
            Connection connection = null;
            if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.SQLSERVER.getName())){
                connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                        dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.SQLSERVER);
            }else if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.POSTGRESQL.getName())){
                connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                        dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.PG);
            }
            assert connection != null;
            abstractDbHelper.executeSql(sql, connection);
            log.info("数据分析视图服务sql执行结束,[{}]", sql);
        } catch (SQLException e) {
            log.error("数据分析视图目标数据库执行sql失败,", e);
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, e.getMessage());
        }
    }

    @Override
    public List<DataSourceVO> getTargetDbList() {
        ResultEntity<List<DataSourceDTO>> result;
        try{
            result = userClient.getAllFiDataDataSource();
            if (result.getCode() != ResultEnum.SUCCESS.getCode()){
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
        }catch (Exception e){
            log.error("数据分析视图调用userClient失败", e);
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED,e.getMessage());
        }

        List<DataSourceDTO> dsList = result.getData();
        List<DataSourceDTO> targetDbList = dsList.stream().filter(item -> item.sourceBusinessTypeValue == SourceBusinessTypeEnum.DW.getValue()
                || item.sourceBusinessTypeValue == SourceBusinessTypeEnum.ODS.getValue()).collect(Collectors.toList());

        List<DataSourceVO> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(targetDbList)){
            targetDbList.stream().filter(item -> {
                DataSourceVO model = new DataSourceVO();
                model.setId(item.getId());
                model.setName(item.getName());
                list.add(model);
                return false;
            }).collect(Collectors.toList());
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum removeViewTheme(Integer viewThemeId) {
        // 查询数据
        DataViewThemePO model = baseMapper.selectById(viewThemeId);
        if (Objects.isNull(model)){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 删除视图主题
        boolean removeFlag = this.removeById(viewThemeId);
        if (!removeFlag){
            throw new FkException(ResultEnum.DELETE_ERROR);
        }

        // 删除数据视图
        QueryWrapper<DataViewPO> dqw = new QueryWrapper<>();
        dqw.lambda().eq(DataViewPO::getViewThemeId, viewThemeId);
        dataViewMapper.delete(dqw);

        // 删除视图主题关联账号
        QueryWrapper<DataViewAccountPO> accountQw = new QueryWrapper<>();
        accountQw.lambda().eq(DataViewAccountPO::getViewThemeId, viewThemeId).eq(DataViewAccountPO::getDelFlag, DelFlagEnum.NORMAL_FLAG.getValue());
        List<DataViewAccountPO> accountList = dataViewAccountMapper.selectList(accountQw);
        if (!CollectionUtils.isEmpty(accountList)){
            int flag = dataViewAccountMapper.deleteBatchIds(accountList.stream().map(DataViewAccountPO::getId).collect(Collectors.toList()));
            if (flag <= 0){
                throw new FkException(ResultEnum.DELETE_ERROR);
            }
            // 删除数据库登录账号信息
            removeLogin(accountList, verifyDataSource(model.getTargetDbId()));
            // 删除角色
            removeRole(viewThemeId, verifyDataSource(model.getTargetDbId()));
        }

        // 删除数据库角色
        QueryWrapper<DataViewRolePO> qw = new QueryWrapper<>();
        qw.lambda().eq(DataViewRolePO::getThemeId, viewThemeId);
        dataViewRoleMapper.delete(qw);

        // 删除不使用的架构
        if (model.getWhetherSchema()){
            removeSchema(model.getThemeAbbr(), model.getTargetDbId());
        }

        return ResultEnum.SUCCESS;
    }

    private void removeRole(Integer viewThemeId, DataSourceDTO dataSourceDTO){
        try{
            QueryWrapper<DataViewRolePO> qw = new QueryWrapper<>();
            qw.lambda().eq(DataViewRolePO::getThemeId, viewThemeId);
            DataViewRolePO dataViewRolePO = dataViewRoleMapper.selectOne(qw);

            String sql1 = "ALTER AUTHORIZATION ON SCHEMA::" + dataViewRolePO.getRoleName() + " TO " + "dbo";
            String sql2 = "DROP ROLE IF EXISTS " + dataViewRolePO.getRoleName();
            if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.POSTGRESQL.getName())){
                sql1 = "REASSIGN OWNED BY " + dataViewRolePO.getRoleName() + " TO postgres";
                sql2 = "DROP USER " + dataViewRolePO.getRoleName();
            }
            AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
            Connection connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                    dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.SQLSERVER);
            abstractDbHelper.executeSql(sql1, connection);
            abstractDbHelper.executeSql(sql2, connection);
        }catch (Exception e){
            log.error("删除数据库角色失败,", e);
        }
    }

    private void removeLogin(List<DataViewAccountPO> accountList, DataSourceDTO dataSourceDTO){
        try {
            for (DataViewAccountPO item : accountList){
                String sql2 = "drop login " + item.getAccountName();
                String sql1 = "drop user if exists " + item.getAccountName();

                AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
                Connection connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                        dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.SQLSERVER);
                if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.POSTGRESQL.getName())){
                    connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                            dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.PG);
                    String sql = "DROP user if exists " + item.getAccountName();
                    abstractDbHelper.executeSql(sql, connection);
                    log.info("删除数据结束，{}", sql);
                }else{
                    // 先查询是否处于登录状态
                    String sql = "SELECT session_id FROM sys.dm_exec_sessions WHERE login_name='" + item.getAccountName() + "'" + " AND status = 'sleeping'";
                    Statement st = null;
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start();
                    st = connection.createStatement();
                    ResultSet res = st.executeQuery(sql);
                    while (res.next()){
                        int sessioonId = res.getInt("session_id");
                        log.info("session_id is {}", sessioonId);
                        if (sessioonId != 0){
                            sql = "kill " + sessioonId;
                            abstractDbHelper.executeSql(sql, connection);
                        }
                    }
                    abstractDbHelper.executeSql(sql1, connection);
                    abstractDbHelper.executeSql(sql2, connection);
                }
            }
        } catch (SQLException e) {
            log.error("删除数据库登录用户失败,", e);
        }
    }

    private void removeSchema(String themeAbbr, Integer targetDbId){
        try {
            // 删除角色和用户TODO
            String sql = "DROP SCHEMA IF EXISTS " + themeAbbr;
            log.info("删除架构语句,[{}]", sql);

            DataSourceDTO dataSourceDTO = verifyDataSource(targetDbId);

            AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
            Connection connection = null;
            if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.SQLSERVER.getName())){
                connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                        dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.SQLSERVER);
            }else{
                connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                        dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.PG);
            }
            abstractDbHelper.executeSql(sql, connection);
        } catch (SQLException e) {
            log.error("删除数据库架构失败,", e);
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "目标数据库删除结果失败，或架构不存在");
        }
        log.info("删除架构执行成功");
    }

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public ResultEnum updateViewTheme(DataViewThemeDTO dto) {
        // 查询数据视图主题
        Integer themeId = dto.getId();
        if (themeId == null || themeId <= 0){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        DataViewThemePO model = baseMapper.selectById(themeId);
        if (Objects.isNull(model)){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 判断视图主题名称是否重复
        QueryWrapper<DataViewThemePO> qw = new QueryWrapper<>();
        qw.lambda().ne(DataViewThemePO::getId, dto.getId()).eq(DataViewThemePO::getThemeName, dto.getThemeName())
                .eq(DataViewThemePO::getDelFlag, DelFlagEnum.NORMAL_FLAG.getValue());
        Integer count = baseMapper.selectCount(qw);
        if (count > 0){
            throw new FkException(ResultEnum.DS_VIEW_THEME_NAME_EXIST);
        }

        // 校验数据源
        DataSourceDTO dataSourceDTO = verifyDataSource(dto.getTargetDbId());

        // 数据转换
        DataViewThemePO po = DataViewMap.INSTANCES.dtoToPo(dto);
        int flag = baseMapper.updateById(po);
        if (flag <= 0){
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR);
        }

        // 更新账号信息
        List<DataViewAccountDTO> relAccountList = dto.getRelAccountList();
        updateRelAccountList(relAccountList, dataSourceDTO, (int)po.getId());
        return ResultEnum.SUCCESS;
    }

    @Override
    public PageDTO<DataViewThemeDTO> getViewThemeList(Integer pageNum, Integer pageSize) {
        Page<DataViewThemePO> poPage = new Page<>(pageNum, pageSize);

        // 查询数据
        QueryWrapper<DataViewThemePO> qw = new QueryWrapper<>();
        qw.lambda().eq(DataViewThemePO::getDelFlag, DelFlagEnum.NORMAL_FLAG.getValue())
                .orderByDesc(DataViewThemePO::getCreateTime);
        baseMapper.selectPage(poPage, qw);

        List<DataViewThemePO> records = poPage.getRecords();
        PageDTO<DataViewThemeDTO> pageDTO = new PageDTO<>();
        if (!CollectionUtils.isEmpty(records)){
            pageDTO.setTotal(poPage.getTotal());
            pageDTO.setTotalPage(poPage.getPages());
            List<DataViewThemeDTO> dtoRecords = DataViewMap.INSTANCES.poToDto(records);

            // 查询关联账号
            List<Integer> themeIds = dtoRecords.stream().map(DataViewThemeDTO::getId).collect(Collectors.toList());
            QueryWrapper<DataViewAccountPO> aqw = new QueryWrapper<>();
            aqw.lambda().in(DataViewAccountPO::getViewThemeId, themeIds);
            List<DataViewAccountPO> poList = dataViewAccountMapper.selectList(aqw);
            for (DataViewThemeDTO parent : dtoRecords){
                List<DataViewAccountPO> accList = poList.stream().filter(item -> item.getViewThemeId().equals(parent.getId())).collect(Collectors.toList());
                parent.setRelAccountList(DataViewMap.INSTANCES.accountListPoToDto(accList));
            }
            pageDTO.setItems(dtoRecords);
        }
        return pageDTO;
    }

    @Override
    public DataSourceVO getDataSourceByViewThemeId(Integer viewThemeId) {
        // 查询targetDbId
        DataSourceDTO dataSourceDTO = verifyDataSource(baseMapper.selectDbId(viewThemeId));

        DataSourceVO vo = new DataSourceVO();
        if (!Objects.isNull(dataSourceDTO)){
            vo.setId(dataSourceDTO.id);
            vo.setName(dataSourceDTO.name);
        }
        return vo;
    }

    private DataSourceDTO verifyDataSource(Integer targetDbId){
        try{
            log.info("开始校验数据源信息");
            // 查询数据源是否存在
            ResultEntity<List<DataSourceDTO>> dsResult = userClient.getAllFiDataDataSource();
            if (dsResult.getCode() != ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(dsResult.data)){
                throw new FkException(ResultEnum.DATASOURCE_INFORMATION_ISNULL);
            }
            // 过滤ods和dw数据源
            List<DataSourceDTO> dsList = dsResult.getData();
            List<DataSourceDTO> targetDbList = dsList.stream().filter(item ->
                    item.sourceBusinessTypeValue == SourceBusinessTypeEnum.DW.getValue()
                            || item.sourceBusinessTypeValue == SourceBusinessTypeEnum.ODS.getValue()
            ).collect(Collectors.toList());
            log.info("目标数据源集合,[{}]", JSON.toJSONString(targetDbList));
            DataSourceDTO dataSourceDTO = targetDbList.stream().filter(item -> item.sourceBusinessTypeValue == targetDbId).findFirst().orElse(null);
            if (Objects.isNull(dataSourceDTO)){
                throw new FkException(ResultEnum.DATASOURCE_INFORMATION_ISNULL);
            }
            log.info("结束校验数据源信息");
            return dataSourceDTO;
        }catch (Exception e){
            log.error("数据分析视图调用userClient失败", e);
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED,e.getMessage());
        }
    }

    private void updateRelAccountList(List<DataViewAccountDTO> list, DataSourceDTO dataSourceDTO, Integer viewThemeId){
        // 获取数据
        QueryWrapper<DataViewAccountPO> qw = new QueryWrapper<>();
        qw.lambda().eq(DataViewAccountPO::getViewThemeId, viewThemeId);
        List<DataViewAccountPO> allList = dataViewAccountMapper.selectList(qw);
        log.info("原始数据{}", JSON.toJSONString(allList));

        if (CollectionUtils.isEmpty(list) && CollectionUtils.isEmpty(allList)){
            return;
        }

        if (CollectionUtils.isEmpty(list) && !CollectionUtils.isEmpty(allList)){
            dataViewAccountMapper.deleteBatchIds(allList.stream().map(DataViewAccountPO::getId).collect(Collectors.toList()));
            removeLogin(allList, dataSourceDTO);
            return;
        }

        if (!CollectionUtils.isEmpty(allList)){
            dataViewAccountMapper.deleteBatchIds(allList.stream().map(DataViewAccountPO::getId).collect(Collectors.toList()));
            removeLogin(allList, dataSourceDTO);
        }

        List<DataViewAccountPO> currList = new ArrayList<>();
        for (DataViewAccountDTO dto : list){
            if (StringUtils.isEmpty(dto.getAccountName()) || StringUtils.isEmpty(dto.getAccountPsd())){
                throw new FkException(ResultEnum.DS_VIEW_THEME_ACCOUNT_ERROR);
            }

            qw = new QueryWrapper<>();
            DataViewAccountPO po = new DataViewAccountPO();
            po.setViewThemeId(viewThemeId);
            po.setAccountName(dto.getAccountName());
            po.setAccountDesc(dto.getAccountDesc());
            po.setAccountPsd(dto.getAccountPsd());
            po.setJurisdiction(AccountJurisdictionEnum.READ_ONLY.getName());
            qw.lambda().eq(DataViewAccountPO::getViewThemeId, po.getViewThemeId()).eq(DataViewAccountPO::getAccountName, dto.getAccountName());
            DataViewAccountPO preModel = dataViewAccountMapper.selectOne(qw);
            if (!Objects.isNull(preModel)){
                throw new FkException(ResultEnum.SAVE_DATA_ERROR, dto.getAccountName() + "：该账号名称已存在");
            }
            int save = dataViewAccountMapper.insert(po);
            if (save <= 0){
                throw new FkException(ResultEnum.DA_VIEWTHEME_UPDATE_ACCOUNT_ERROR);
            }
            currList.add(po);
        }

        if (CollectionUtils.isEmpty(currList)){
            return;
        }
        saveRelationAccount(DataViewMap.INSTANCES.accountListPoToDto(currList), viewThemeId, dataSourceDTO, "update");
    }

    /**
     * 校验schema
     *
     * @param schemaName
     * @param targetDbId
     */
    public void createSchema(String schemaName, Integer targetDbId) {
        ResultEntity<DataSourceDTO> dataSourceConfig = null;
        try{
            dataSourceConfig = userClient.getFiDataDataSourceById(targetDbId);
            if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
            }
        }catch (Exception e){
            log.error("数据分析视图服务创建视图调用userClient失败,", e);
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED,e.getMessage());
        }

        AbstractCommonDbHelper helper = new AbstractCommonDbHelper();
        Connection connection = helper.connection(dataSourceConfig.data.conStr, dataSourceConfig.data.conAccount, dataSourceConfig.data.conPassword, dataSourceConfig.data.conType);
        log.info("已获取数据库连接");
        CreateSchemaSqlUtils.buildSchemaSql(connection, schemaName, dataSourceConfig.data.conType);
        log.info("架构创建结束");
    }
}
