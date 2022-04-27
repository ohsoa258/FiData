package com.fisk.datagovernance.test;

import com.fisk.common.core.utils.similarity.CosineSimilarity;
import com.fisk.datagovernance.service.impl.dataops.DataOpsDataSourceManageImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量测试类
 * @date 2022/4/25 13:37
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class DataQualityTest {

    /**
     * @return void
     * @description 相似度规则测试
     * @author dick
     * @date 2022/4/25 13:42
     * @version v1.0
     * @params
     */
    @Test
    public void similarityTest() {
        double similarity = CosineSimilarity.getSimilarity("产品销售渠道", "上海农产品销售渠道");
        System.out.println("相似度i计算比例：" + similarity);
        similarity = CosineSimilarity.getSimilarity("上海", "上海农产品销售渠道");
        System.out.println("相似度i计算比例：" + similarity);
        similarity = CosineSimilarity.getSimilarity("上海农产品销售渠道", "上海农产品销售渠道");
        System.out.println("相似度i计算比例：" + similarity);
    }

    /**
     * @return void
     * @description pg数据库信息写入redis
     * @author dick
     * @date 2022/4/25 13:42
     * @version v1.0
     * @params
     */
    @Test
    public void setDataOpsDataSource() {
        DataOpsDataSourceManageImpl dataOpsDataSourceManage=new DataOpsDataSourceManageImpl();
        dataOpsDataSourceManage.setDataOpsDataSource();
    }

}
