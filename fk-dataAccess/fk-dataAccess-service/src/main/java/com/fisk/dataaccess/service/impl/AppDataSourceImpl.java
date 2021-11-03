package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataaccess.dto.TablePyhNameDTO;
import com.fisk.dataaccess.dto.v3.DataSourceDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.mapper.AppDataSourceMapper;
import com.fisk.dataaccess.service.IAppDataSource;
import com.fisk.dataaccess.utils.MysqlConUtils;
import com.fisk.dataaccess.utils.SqlServerConUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 */
@Service
public class AppDataSourceImpl extends ServiceImpl<AppDataSourceMapper, AppDataSourcePO> implements IAppDataSource {

    @Resource
    AppDataSourceMapper mapper;

    @Override
    public DataSourceDTO getDataSourceMeta(long appId) {

        DataSourceDTO dataSource = mapper.getDataSource(appId);
        AppDataSourcePO po = this.query().eq("app_id", appId).one();
        MysqlConUtils mysqlConUtils = new MysqlConUtils();
        SqlServerConUtils sqlServerConUtils = new SqlServerConUtils();
        List<TablePyhNameDTO> list = mysqlConUtils.getTableNameAndColumns(po.connectStr, po.connectAccount, po.connectPwd);

        return null;
    }
}
