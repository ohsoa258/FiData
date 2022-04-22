package com.fisk.datagovernance.service.impl.dataops;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.utils.PostgresConUtils;
import com.fisk.datagovernance.dto.dataops.ExecuteDataOpsSqlDTO;
import com.fisk.datagovernance.dto.dataops.PostgreDTO;
import com.fisk.datagovernance.enums.dataquality.DataSourceTypeEnum;
import com.fisk.datagovernance.service.dataops.IDataOpsDataSourceManageService;
import com.fisk.datagovernance.vo.dataops.ExecuteResultVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataBaseSourceVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataExampleSourceVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 数据运维数据源实现类
 * @date 2022/4/22 13:38
 */
@Service
@Slf4j
public class DataOpsDataSourceManageImpl implements IDataOpsDataSourceManageService {

    @Value("${pgsql-dw.id}")
    private int pgsqlDwId;
    @Value("${pgsql-dw.ip}")
    private String pgsqlDwIp;
    @Value("${pgsql-dw.port}")
    private int pgsqlDwPort;
    @Value("${pgsql-dw.dbName}")
    private String pgsqlDwDbName;
    @Value("${pgsql-dw.driverClassName}")
    private String pgsqlDwDriverClassName;
    @Value("${pgsql-dw.url}")
    private String pgsqlDwUrl;
    @Value("${pgsql-dw.username}")
    private String pgsqlDwUsername;
    @Value("${pgsql-dw.password}")
    private String pgsqlDwPassword;

    @Value("${pgsql-ods.id}")
    private int pgsqlOdsId;
    @Value("${pgsql-ods.ip}")
    private String pgsqlOdsIp;
    @Value("${pgsql-ods.port}")
    private int pgsqlOdsPort;
    @Value("${pgsql-ods.dbName}")
    private String pgsqlOdsDbName;
    @Value("${pgsql-ods.driverClassName}")
    private String pgsqlOdsDriverClassName;
    @Value("${pgsql-ods.url}")
    private String pgsqlOdsUrl;
    @Value("${pgsql-ods.username}")
    private String pgsqlOdsUsername;
    @Value("${pgsql-ods.password}")
    private String pgsqlOdsPassword;

    @Override
    public ResultEntity<List<DataExampleSourceVO>> getDataOpsSourceAll() {
        List<DataExampleSourceVO> dataExampleSourceVOList = new ArrayList<>();
        // 第一步：读取配置的数据源信息
        List<PostgreDTO> postgreDTOList = getPostgreDTOList();
        if (CollectionUtils.isEmpty(postgreDTOList)) {
            return ResultEntityBuild.buildData(ResultEnum.DATA_OPS_CONFIG_EXISTS, dataExampleSourceVOList);
        }
        // 第二步：读取数据源下的库、表、字段信息
        PostgresConUtils postgresConUtils = new PostgresConUtils();
        // 实例信息
        List<String> conIps = postgreDTOList.stream().map(PostgreDTO::getIp).distinct().collect(Collectors.toList());
        for (String conIp : conIps) {
            DataExampleSourceVO dataExampleSourceVO = null;
            List<DataBaseSourceVO> dataBaseSourceVOS = new ArrayList<>();
            for (PostgreDTO postgreDTO : postgreDTOList) {
                if (postgreDTO.getIp().equals(conIp)) {
                    if (dataExampleSourceVO == null) {
                        dataExampleSourceVO = new DataExampleSourceVO();
                        dataExampleSourceVO.setConIp(postgreDTO.getIp());
                        dataExampleSourceVO.setConType(postgreDTO.getDataSourceTypeEnum());
                        dataExampleSourceVO.setConPort(postgreDTO.getPort());
                    }
                    List<TablePyhNameDTO> tableNameAndColumns = postgresConUtils.getTableNameAndColumns(postgreDTO.getPgsqlUrl(), postgreDTO.getPgsqlUsername(),
                            postgreDTO.getPgsqlPassword(), postgreDTO.getDataSourceTypeEnum().getDriverName());
                    DataBaseSourceVO dataBaseSourceVO = new DataBaseSourceVO();
                    dataBaseSourceVO.setId(postgreDTO.getId());
                    dataBaseSourceVO.setConDbname(postgreDTO.getDbName());
                    dataBaseSourceVO.setChildren(tableNameAndColumns);
                    dataBaseSourceVOS.add(dataBaseSourceVO);
                }
            }
            dataExampleSourceVO.setChildren(dataBaseSourceVOS);
            dataExampleSourceVOList.add(dataExampleSourceVO);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, dataExampleSourceVOList);
    }

    @Override
    public ResultEntity<ExecuteResultVO> executeDataOpsSql(ExecuteDataOpsSqlDTO dto) {
        ExecuteResultVO executeResultVO = new ExecuteResultVO();

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, executeResultVO);
    }

    /**
     * @return java.util.List<com.fisk.datagovernance.dto.dataops.PostgreDTO>
     * @description 读取pg配置文件转实体
     * @author dick
     * @date 2022/4/22 14:26
     * @version v1.0
     * @params
     */
    public List<PostgreDTO> getPostgreDTOList() {
        List<PostgreDTO> postgreDTOList = new ArrayList<>();
        PostgreDTO postgreDTO_dw = new PostgreDTO();
        postgreDTO_dw.setId(pgsqlDwId);
        postgreDTO_dw.setPort(pgsqlDwPort);
        postgreDTO_dw.setIp(pgsqlDwIp);
        postgreDTO_dw.setDbName(pgsqlDwDbName);
        postgreDTO_dw.setDataSourceTypeEnum(DataSourceTypeEnum.getEnumByDriverName(pgsqlDwDriverClassName));
        postgreDTO_dw.setPgsqlUrl(pgsqlDwUrl);
        postgreDTO_dw.setPgsqlUsername(pgsqlDwUsername);
        postgreDTO_dw.setPgsqlPassword(pgsqlDwPassword);
        postgreDTOList.add(postgreDTO_dw);
        PostgreDTO postgreDTO_ods = new PostgreDTO();
        postgreDTO_ods.setId(pgsqlOdsId);
        postgreDTO_ods.setPort(pgsqlOdsPort);
        postgreDTO_ods.setIp(pgsqlOdsIp);
        postgreDTO_ods.setDbName(pgsqlOdsDbName);
        postgreDTO_ods.setDataSourceTypeEnum(DataSourceTypeEnum.getEnumByDriverName(pgsqlOdsDriverClassName));
        postgreDTO_ods.setPgsqlUrl(pgsqlOdsUrl);
        postgreDTO_ods.setPgsqlUsername(pgsqlOdsUsername);
        postgreDTO_ods.setPgsqlPassword(pgsqlOdsPassword);
        postgreDTOList.add(postgreDTO_ods);
        return postgreDTOList;
    }

    public static Connection getConnection(String driver, String url, String username, String password) {
        Connection conn = null;
        try {
            // 加载驱动类
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            System.out.println("找不到pg驱动程序类 ，加载驱动失败！");
            throw new FkException(ResultEnum.CREATE_PG_CONNECTION);
        } catch (SQLException e) {
            System.out.println("pg数据库连接失败！");
            throw new FkException(ResultEnum.PG_CONNECT_ERROR);
        }
        return conn;
    }
}
