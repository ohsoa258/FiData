package com.fisk.dataservice.utils.mysql;

import com.fisk.dataservice.dto.DataDoFieldDTO;
import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CreateMysqlUtilTest extends TestCase {

    @Test
    public void Test(){
        List<DataDoFieldDTO> apiConfigureFieldList = new ArrayList<>();
        DataDoFieldDTO dataDoFieldDTO1 = new DataDoFieldDTO();
        dataDoFieldDTO1.setFieldId(10);
        dataDoFieldDTO1.setFieldType(DataDoFieldTypeEnum.WHERE);
        dataDoFieldDTO1.setWhere("=");
        dataDoFieldDTO1.setWhereValue("2021");

        DataDoFieldDTO dataDoFieldDTO2 = new DataDoFieldDTO();
        dataDoFieldDTO2.setFieldType(DataDoFieldTypeEnum.VALUE);
        dataDoFieldDTO2.setFieldName("money");

        DataDoFieldDTO dataDoFieldDTO3 = new DataDoFieldDTO();
        dataDoFieldDTO3.setFieldType(DataDoFieldTypeEnum.COLUMN);
        dataDoFieldDTO3.setFieldName("year");
        dataDoFieldDTO3.setFieldId(116);

        CreateMysqlUtil createMysqlUtil = new CreateMysqlUtil();
        createMysqlUtil.filterData(apiConfigureFieldList,1,10);
    }
}