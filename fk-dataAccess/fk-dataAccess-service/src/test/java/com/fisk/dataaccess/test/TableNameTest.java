package com.fisk.dataaccess.test;

import com.fisk.common.response.ResultEntity;
import com.fisk.dataaccess.entity.TableSyncmodePO;
import com.fisk.dataaccess.mapper.TableSyncmodeMapper;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.dataservice.dto.TableDataDTO;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TableNameTest {
    @Resource
    DataModelClient client;
    @Resource
    TableSyncmodeMapper mapper;

    @Test
    public void test() {

//        System.out.println(client.getTableName(36, DataDoFieldTypeEnum.VALUE));
        ResultEntity<TableDataDTO> name = client.getTableName(155, DataDoFieldTypeEnum.COLUMN, "year");
        System.out.println(name.data);
    }

    @Test
    public void test02() {
        TableSyncmodePO data = mapper.getData(1651);
        System.out.println(data);
        System.out.println(data.syncField.length());
    }


}