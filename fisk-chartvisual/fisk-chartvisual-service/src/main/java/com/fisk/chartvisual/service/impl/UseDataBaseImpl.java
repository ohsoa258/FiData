package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.chartvisual.entity.DataSourceConPO;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.service.IUseDataBase;
import com.fisk.chartvisual.util.dscon.AbstractUseDataBase;
import com.fisk.chartvisual.util.dscon.DataSourceConFactory;
import com.fisk.chartvisual.vo.DataSourceConVO;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import org.springframework.stereotype.Service;

import java.sql.*;
import javax.annotation.Resource;

/**
 * @author gy
 */
@Service
public class UseDataBaseImpl extends ServiceImpl<DataSourceConMapper, DataSourceConPO> implements IUseDataBase {

    @Resource
    private DataSourceConMapper mapper;

    @Override
    public boolean testConnection(DataSourceTypeEnum type, String con, String acc, String pwd) {
        AbstractUseDataBase db = DataSourceConFactory.getConnection(type);
        Connection connection = db.connection(con, acc, pwd);
        boolean res = connection != null;
        try {
            if(res) {
                connection.close();
            }
        } catch (SQLException e) {
            log.error("【testConnection】数据库连接关闭报错, ex", e);
        }
        return res;
    }


}
