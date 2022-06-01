package com.fisk.mdm.service.impl;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.common.service.mdmBEBuild.BuildFactoryHelper;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.mdm.dto.complextype.GeographyDTO;
import com.fisk.mdm.map.ComplexTypeMap;
import com.fisk.mdm.service.IComplexType;
import com.fisk.mdm.vo.complextype.GeographyVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author JianWenYang
 */
@Service
public class ComplexTypeServiceImpl implements IComplexType {

    @Resource
    UserHelper userHelper;
    @Value("${pgsql-mdm.type}")
    private DataSourceTypeEnum type;
    @Value("${pgsql-mdm.url}")
    private String url;
    @Value("${pgsql-mdm.username}")
    private String username;
    @Value("${pgsql-mdm.password}")
    private String password;

    public static Map beanToMap(Object object) {
        try {
            Map<String, Object> map = new HashMap<>();
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(object));
            }
            return map;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Override
    public Integer addGeography(GeographyDTO dto) {
        GeographyVO geographyVO = ComplexTypeMap.INSTANCES.dtoToVo(dto);
        geographyVO.setCreate_user(userHelper.getLoginUserInfo().id);
        geographyVO.setCreate_time(LocalDateTime.now());
        IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
        String sql = buildSqlCommand.buildInsertSingleData(beanToMap(geographyVO), "tb_geography");
        return AbstractDbHelper.executeSqlReturnKey(sql, getConnection());
    }

    /**
     * 连接Connection
     *
     * @return {@link Connection}
     */
    public Connection getConnection() {
        AbstractDbHelper dbHelper = new AbstractDbHelper();
        Connection connection = dbHelper.connection(url, username,
                password, type);
        return connection;
    }

}
