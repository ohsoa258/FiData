package com.fisk.dataaccess.test;

import com.fisk.dataaccess.enums.ComponentIdTypeEnum;
import com.fisk.dataaccess.mapper.NifiConfigMapper;
import com.fisk.task.dto.daconfig.DataSourceConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class NifiTest {

    @Value("${spring.datasource.url}")
    private String jdbcStr;

    @Value("${spring.datasource.username}")
    private String user;

    @Value("${spring.datasource.password}")
    private String password;
    @Resource
    private NifiConfigMapper nifiConfigMapper;

    @Test
    public void testJdbcConfig() {
        System.out.println(jdbcStr + "\n" + user + "\n" + password);
    }

    @Test
    public void testName() {
        System.out.println(ComponentIdTypeEnum.CFG_DB_POOL_COMPONENT_ID.getName());
    }
    @Test
    public void testNifiConfig() {
        DataSourceConfig dataSourceConfig = null;
        try {
            String nifiKey = nifiConfigMapper.getNifiKey();
            dataSourceConfig = new DataSourceConfig();
            dataSourceConfig.componentId = nifiConfigMapper.getNifiValue();
            System.out.println(dataSourceConfig);
        } catch (Exception e) {
            System.out.println(dataSourceConfig);
        }
        //        if (StringUtils.isNotEmpty(nifiKey)) {
//            System.out.println(nifiConfigMapper.getNifiValue());
//        }
    }

}
