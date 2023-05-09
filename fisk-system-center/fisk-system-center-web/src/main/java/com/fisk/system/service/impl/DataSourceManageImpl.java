package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.FileBinaryUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.dto.app.DbConnectionDTO;
import com.fisk.system.dto.GetConfigDTO;
import com.fisk.system.dto.datasource.*;
import com.fisk.system.entity.DataSourcePO;
import com.fisk.system.map.DataSourceMap;
import com.fisk.system.mapper.DataSourceMapper;
import com.fisk.system.service.IDataSourceManageService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据源接口实现类
 *
 * @author dick
 */
@Service
@Slf4j
public class DataSourceManageImpl extends ServiceImpl<DataSourceMapper, DataSourcePO> implements IDataSourceManageService {

    @Resource
    private GetConfigDTO getConfig;

    @Resource
    private GetMetadata getMetadata;

    @Resource
    private GenerateCondition generateCondition;

    @Resource
    private DataAccessClient dataAccessClient;

    @Resource
    UserHelper userHelper;

    @Override
    public List<DataSourceDTO> getSystemDataSource() {
        List<DataSourceDTO> all = getAll(true);
        if (CollectionUtils.isNotEmpty(all)) {
            all = all.stream().filter(t -> t.getSourceType() == 1).collect(Collectors.toList());
        }
        return all;
    }

    @Override
    public List<DataSourceDTO> getExternalDataSource() {
        List<DataSourceDTO> all = getAll(true);
        if (CollectionUtils.isNotEmpty(all)) {
            all = all.stream().filter(t -> t.getSourceType() == 1 || t.getSourceType() == 2).collect(Collectors.toList());
        }
        return all;
    }

    @Override
    public List<DataSourceDTO> getAll() {
        List<DataSourceDTO> all = getAll(true);
        return all;
    }

    @Override
    public List<FilterFieldDTO> getSearchColumn() {
        MetaDataConfigDTO dto = new MetaDataConfigDTO();
        dto.url = getConfig.url;
        dto.userName = getConfig.username;
        dto.password = getConfig.password;
        dto.driver = getConfig.driver;
        dto.tableName = "tb_datasource_config";
        dto.filterSql = FilterSqlConstants.PLATFORM_DATASOURCE_SQL;
        return getMetadata.getMetadataList(dto);
    }

    @Override
    public Page<DataSourceDTO> getAllDataSource(DataSourceQueryDTO queryDTO) {
        StringBuilder querySql = new StringBuilder();
        if (CollectionUtils.isNotEmpty(queryDTO.getDto())) {
            List<FilterQueryDTO> filterQueryDTOS = queryDTO.getDto().stream().filter(t -> t.columnName.contains("con_type")).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(filterQueryDTOS)) {
                filterQueryDTOS.forEach(filterQueryDTO -> {
                    if (StringUtils.isNotEmpty(filterQueryDTO.getColumnValue())) {
                        if (filterQueryDTO.getColumnValue().equalsIgnoreCase("MYSQL")) {
                            filterQueryDTO.setColumnValue("0");
                        } else if (filterQueryDTO.getColumnValue().equalsIgnoreCase("SQLSERVER")
                                || filterQueryDTO.getColumnValue().equalsIgnoreCase("SSMS")) {
                            filterQueryDTO.setColumnValue("1");
                        } else if (filterQueryDTO.getColumnValue().equalsIgnoreCase("POSTGRESQL")
                                || filterQueryDTO.getColumnValue().equalsIgnoreCase("PG")
                                || filterQueryDTO.getColumnValue().equalsIgnoreCase("PGSQL")) {
                            filterQueryDTO.setColumnValue("4");
                        } else if (filterQueryDTO.getColumnValue().equalsIgnoreCase("DORIS")) {
                            filterQueryDTO.setColumnValue("5");
                        } else if (filterQueryDTO.getColumnValue().equalsIgnoreCase("ORACLE")) {
                            filterQueryDTO.setColumnValue("6");
                        }
                    }
                });
            }
        }

        // 拼接原生筛选条件
        querySql.append(generateCondition.getCondition(queryDTO.getDto()));
        DataSourcePageDTO data = new DataSourcePageDTO();
        data.page = queryDTO.getPage();
        // 筛选器左边的模糊搜索查询SQL拼接
        data.where = querySql.toString();
        if (queryDTO.getSourceType() > 0) {
            data.where += " AND ds.source_type=" + queryDTO.getSourceType();
        }

        Page<DataSourceDTO> filter = baseMapper.filter(queryDTO.getPage(), data);
        if (filter != null && CollectionUtils.isNotEmpty(filter.getRecords())) {
            filter.getRecords().stream().forEach(t -> {
                t.setConType(DataSourceTypeEnum.getEnum(t.getConTypeValue()));
                t.setConTypeName(t.getConType().getName());
                t.setSourceBusinessType(SourceBusinessTypeEnum.getEnum(t.getSourceBusinessTypeValue()));
            });
        }
        return filter;
    }

    @Override
    public ResultEnum updateDataSource(DataSourceSaveDTO dto) {
        DataSourcePO model = baseMapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<DataSourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DataSourcePO::getName, dto.name)
                .eq(DataSourcePO::getSourceType, dto.sourceType)
                .ne(DataSourcePO::getId, dto.id);
        DataSourcePO data = baseMapper.selectOne(queryWrapper);
        if (data != null) {
            return ResultEnum.DATA_SOURCE_NAME_ALREADY_EXISTS;
        }
        //mapStruct:sftp密钥已忽略
        DataSourceMap.INSTANCES.dtoToPo(dto, model);
        //sftp秘钥方式,存储二进制数据 (如果涉及到sftp公钥更换文件)
        if (dto.fileBinary != null && DataSourceTypeEnum.SFTP.getName().equals(dto.conType.getName())) {
            model.fileBinary = fileToBinaryStr(dto.fileBinary);
        }
        return baseMapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteDataSource(int id) {
        DataSourcePO model = baseMapper.selectById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
//        if (model.getSourceType() == 1) {
//            return ResultEnum.SYSTEM_DATA_SOURCE_NOT_OPERATION;
//        }
        return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.DELETE_ERROR;
    }

    @Override
    public ResultEntity<Object> insertDataSource(DataSourceSaveDTO dto) {
        //获取当前登陆人  取缔
//        UserInfo userInfo = userHelper.getLoginUserInfo();
//        String username = userInfo.getUsername();

        QueryWrapper<DataSourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataSourcePO::getName, dto.name)
                .eq(DataSourcePO::getSourceType, dto.sourceType)
                .eq(DataSourcePO::getDelFlag, 1);
        DataSourcePO model = baseMapper.selectOne(queryWrapper);
        if (model != null) {
            return ResultEntityBuild.build(ResultEnum.DATA_SOURCE_NAME_ALREADY_EXISTS);
        }
        model = new DataSourcePO();
//        model.createUser = username;
        //mapStruct:sftp RSA密钥属性 fileBinary 已忽略,不参与转换
        DataSourceMap.INSTANCES.dtoToPo(dto, model);
        //sftp秘钥方式,存储二进制数据(如果sftp选择RSA公钥方式添加的话，才会将文件转为二进制字符串)
        if (dto.fileBinary != null && DataSourceTypeEnum.SFTP.getName().equals(dto.conType.getName())) {
            //密钥路径手动转换
            model.fileBinary = fileToBinaryStr(dto.fileBinary);
        }
        int flag = baseMapper.insert(model);
        DataSourcePO newPo = null;
        if (flag > 0) {
            // 查询数据源
            queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(DataSourcePO::getName, dto.name)
                    .eq(DataSourcePO::getSourceType, dto.sourceType)
                    .eq(DataSourcePO::getDelFlag, 1);
            newPo = baseMapper.selectOne(queryWrapper);
        }
        return flag > 0 ? ResultEntityBuild.buildData(ResultEnum.SUCCESS, newPo.getId())
                : ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
    }

    @SneakyThrows
    @Override
    public ResultEnum testConnection(DataSourceSaveDTO dto) {
        Connection conn = null;
        try {
            switch (dto.conType) {
                case MYSQL:
                    Class.forName(DataSourceTypeEnum.MYSQL.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    return ResultEnum.SUCCESS;
                case SQLSERVER:
                    Class.forName(DataSourceTypeEnum.SQLSERVER.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    return ResultEnum.SUCCESS;
                case POSTGRESQL:
                    Class.forName(DataSourceTypeEnum.POSTGRESQL.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    return ResultEnum.SUCCESS;
                case DORIS:
                    Class.forName(DataSourceTypeEnum.DORIS.getDriverName());
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    return ResultEnum.SUCCESS;
                case ORACLE:
                    log.info("ORACLE驱动开始加载");
                    log.info("ORACLE驱动基本信息：" + DataSourceTypeEnum.ORACLE.getDriverName());
                    Class.forName(DataSourceTypeEnum.ORACLE.getDriverName());
                    log.info("ORACLE驱动加载完毕");
                    conn = DriverManager.getConnection(dto.conStr, dto.conAccount, dto.conPassword);
                    return ResultEnum.SUCCESS;
                case FTP:
                    //为了测试ftp连接，先将DataSourceSaveDTO对象转为DbConnectionDTO对象
                    DbConnectionDTO ftpDTO = saveDtoToConDto(dto);
                    ResultEntity<Object> connectFtp = dataAccessClient.connectFtp(ftpDTO);
                    if (connectFtp.getCode() == ResultEnum.SUCCESS.getCode()){
                        return ResultEnum.SUCCESS;
                    }else {
                        return ResultEnum.FTP_CONNECTION_ERROR;
                    }
                case SFTP:
                    //为了测试sftp连接，先将DataSourceSaveDTO对象转为DbConnectionDTO对象
                    DbConnectionDTO sftpDTO = saveDtoToConDto(dto);
                    ResultEntity<Object> connectSftp = dataAccessClient.connectSftp(sftpDTO);
                    if (connectSftp.getCode() == ResultEnum.SUCCESS.getCode()){
                        return ResultEnum.SUCCESS;
                    }else {
                        return ResultEnum.SFTP_CONNECTION_ERROR;
                    }
                case API:
                    if (dto.authenticationMethod == 3) {
                        AppDataSourceDTO appDataSourceDTO = saveDtoToSourceDto(dto);
                        ResultEntity<Object> apiToken = dataAccessClient.getApiToken(appDataSourceDTO);
                        if (apiToken != null) {
                            return ResultEnum.SUCCESS;
                        } else {
                            return ResultEnum.API_NOT_EXIST;
                        }
                    }
                default:
                    return ResultEnum.DS_DATASOURCE_CON_WARN;
            }
        } catch (Exception e) {
            if (conn != null) {
                conn.close();
            }
            log.error("测试连接异常：" + e);
            return ResultEnum.DATASOURCE_CONNECTERROR;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                throw new FkException(ResultEnum.DATASOURCE_CONNECTCLOSEERROR);
            }
        }
    }

    /**
     * 将文件转为二进制字符串
     *
     * @param filePath
     * @return
     */
    public String fileToBinaryStr(String filePath) {
        return FileBinaryUtils.fileToBinStr(filePath);
    }

    /**
     * 将DataSourceSaveDTO对象转为DbConnectionDTO对象
     *
     * @param saveDTO
     * @return
     */
    public DbConnectionDTO saveDtoToConDto(DataSourceSaveDTO saveDTO) {
        DbConnectionDTO conDto = new DbConnectionDTO();
        conDto.connectAccount = saveDTO.conAccount;
        conDto.connectPwd = saveDTO.conPassword;
        conDto.connectStr = saveDTO.conStr;
        conDto.driveType = saveDTO.conType.getName();
        conDto.port = String.valueOf(saveDTO.conPort);
        conDto.host = saveDTO.conIp;
        return conDto;
    }

    /**
     * 将DataSourceSaveDTO对象转为AppDataSourceDTO对象
     *
     * @param saveDTO
     * @return
     */
    public AppDataSourceDTO saveDtoToSourceDto(DataSourceSaveDTO saveDTO) {
        AppDataSourceDTO sourceDTO = new AppDataSourceDTO();
        sourceDTO.apiResultConfigDtoList = saveDTO.apiResultConfigDtoList;
        sourceDTO.connectStr = saveDTO.conStr;
        sourceDTO.accountKey = saveDTO.accountKey;
        sourceDTO.pwdKey = saveDTO.pwdKey;
        sourceDTO.connectAccount = saveDTO.conAccount;
        sourceDTO.connectPwd = saveDTO.conPassword;
        sourceDTO.id = Long.valueOf(saveDTO.id);
        sourceDTO.expirationTime = saveDTO.expirationTime;
        return sourceDTO;
    }

    @Override
    public ResultEntity<DataSourceDTO> getById(int datasourceId) {
        DataSourcePO t = baseMapper.selectById(datasourceId);
        if (t == null) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_NOTEXISTS, null);
        }
        DataSourceDTO dataSourceDTO = poToDto(true, t);
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataSourceDTO);
    }

    @Override
    public DataSourceResultDTO insertDataSourceByAccess(DataSourceSaveDTO dto) {
        DataSourcePO model = this.query().eq("name", dto.name).one();
        if (model == null) {
            model = new DataSourcePO();
            DataSourceMap.INSTANCES.accessDtoToPo(dto, model);
            boolean flat = this.save(model);
            if (!flat) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        } else {
            model = updateDataSourceByAccess(model, dto);
            boolean flat = this.updateById(model);
            if (!flat) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        DataSourceResultDTO result = new DataSourceResultDTO();
        result.id = (int) model.id;
        return result;
    }

    public DataSourcePO updateDataSourceByAccess(DataSourcePO po, DataSourceSaveDTO dto) {
        po.conAccount = dto.conAccount;
        po.conDbname = dto.conDbname;
        po.conIp = dto.conIp;
        po.conPassword = dto.conPassword;
        po.conPort = dto.conPort;
        po.conStr = dto.conStr;
        po.serviceName = dto.serviceName;
        po.serviceType = dto.serviceType;
        po.conType = dto.conType.getValue();
        po.name = dto.name;
        return po;
    }

    public List<DataSourceDTO> getAll(boolean isShowPwd) {
        List<DataSourceDTO> dataSourceList = new ArrayList<>();
        QueryWrapper<DataSourcePO> dataSourcePOQueryWrapper = new QueryWrapper<>();
        dataSourcePOQueryWrapper.lambda().eq(DataSourcePO::getDelFlag, 1);
        List<DataSourcePO> dataSourcePOS = baseMapper.selectList(dataSourcePOQueryWrapper);
        if (CollectionUtils.isNotEmpty(dataSourcePOS)) {
            dataSourcePOS.forEach(t -> {
                DataSourceDTO dataSourceDTO = poToDto(isShowPwd, t);
                dataSourceList.add(dataSourceDTO);
            });
        }
        return dataSourceList;
    }

    /**
     * @param isShowPwd
     * @param t
     * @return
     */
    private DataSourceDTO poToDto(boolean isShowPwd, DataSourcePO t) {
        DataSourceDTO dataSourceDTO = new DataSourceDTO();
        dataSourceDTO.setId(Math.toIntExact(t.getId()));
        dataSourceDTO.setName(t.getName());
        dataSourceDTO.setConStr(t.getConStr());
        dataSourceDTO.setConIp(t.getConIp());
        dataSourceDTO.setConPort(t.getConPort());
        dataSourceDTO.setConDbname(t.getConDbname());
        dataSourceDTO.setConType(DataSourceTypeEnum.getEnum(t.getConType()));
        dataSourceDTO.setConTypeValue(t.getConType());
        dataSourceDTO.setConTypeName(DataSourceTypeEnum.getEnum(t.getConType()).getName());
        dataSourceDTO.setConAccount(t.getConAccount());
        dataSourceDTO.setPlatform(t.getPlatform());
        dataSourceDTO.setProtocol(t.getProtocol());
        dataSourceDTO.setServiceType(t.getServiceType());
        dataSourceDTO.setServiceName(t.getServiceName());
        dataSourceDTO.setDomainName(t.getDomainName());
        dataSourceDTO.setSourceType(t.getSourceType());
        dataSourceDTO.setSourceBusinessType(SourceBusinessTypeEnum.getEnum(t.getSourceBusinessType()));
        dataSourceDTO.setSourceBusinessTypeValue(t.getSourceBusinessType());
        dataSourceDTO.setPurpose(t.getPurpose());
        dataSourceDTO.setPrincipal(t.getPrincipal());
        dataSourceDTO.setFileSuffix(t.getFileSuffix());
        dataSourceDTO.setFileBinary(t.getFileBinary());
        dataSourceDTO.setPdbName(t.getPdbName());
        dataSourceDTO.setSignatureMethod(t.getSignatureMethod());
        dataSourceDTO.setConsumerKey(t.getConsumerKey());
        dataSourceDTO.setConsumerSecret(t.getConsumerSecret());
        dataSourceDTO.setAccessToken(t.getAccessToken());
        dataSourceDTO.setTokenSecret(t.getTokenSecret());
        dataSourceDTO.setAccountKey(t.getAccountKey());
        dataSourceDTO.setPwdKey(t.getPwdKey());
        dataSourceDTO.setExpirationTime(t.getExpirationTime());
        dataSourceDTO.setToken(t.getToken());
        dataSourceDTO.setAuthenticationMethod(t.getAuthenticationMethod());
        if (isShowPwd) {
            dataSourceDTO.setConPassword(t.getConPassword());
        }
        return dataSourceDTO;
    }

}
