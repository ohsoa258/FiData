package com.fisk.datafactory.test;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lock
 * @version 2.6
 * @description
 * @date 2022/7/19 16:22
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class FeignCallTest {

    @Resource
    DataAccessClient dataAccessClient;

    @Test
    public void test01()throws Exception {

        ResultEntity<List<DataAccessSourceTableDTO>> dataAccessMetaData = dataAccessClient.getDataAccessMetaData();

        if (dataAccessMetaData.code == ResultEnum.SUCCESS.getCode()) {

            System.out.println(dataAccessMetaData);
        }


    }

}
