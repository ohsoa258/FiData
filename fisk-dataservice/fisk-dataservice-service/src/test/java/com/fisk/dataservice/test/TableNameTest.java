package com.fisk.dataservice.test;

import com.fisk.datamodel.client.DimensionClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Lock
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TableNameTest {
    @Resource
    DimensionClient client;

    @Test
    public void test() {
//        System.out.println(client.getTableName(36, DataDoFieldTypeEnum.VALUE));
    }
}
