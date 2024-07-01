package com.fisk.datamanagement;

import com.fisk.datamanagement.entity.MetadataEntityPO;
import com.fisk.datamanagement.service.impl.MetaAnalysisEmailConfigServiceImpl;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestManage {

    @Resource
    private MetaAnalysisEmailConfigServiceImpl metaAnalysisEmailConfigService;

    @org.junit.Test
    public void testSendEmail() {
        metaAnalysisEmailConfigService.sendEmailOfMetaAudit();
    }

    public static void main(String[] args) {
        List<MetadataEntityPO> poList = new ArrayList<>();
        MetadataEntityPO entityPO1 = new MetadataEntityPO();
        entityPO1.setId(27404);
        entityPO1.setName("accident_focus");
        entityPO1.setDescription(null);

        MetadataEntityPO entityPO2 = new MetadataEntityPO();
        entityPO2.setId(27429);
        entityPO2.setName("accident_focus_teachable");
        entityPO2.setDescription(null);

        MetadataEntityPO entityPO3 = new MetadataEntityPO();
        entityPO3.setId(73186);
        entityPO3.setName("dim_date");
        entityPO3.setDescription("日期维度表");

        MetadataEntityPO entityPO4 = new MetadataEntityPO();
        entityPO4.setId(73224);
        entityPO4.setName("dim_mrtorcade");
        entityPO4.setDescription("车队维度");

        MetadataEntityPO entityPO5 = new MetadataEntityPO();
        entityPO5.setId(118424);
        entityPO5.setName("accident_focus");
        entityPO5.setDescription("fidata - hudi入仓配置表");

        MetadataEntityPO entityPO6 = new MetadataEntityPO();
        entityPO6.setId(118450);
        entityPO6.setName("accident_focus_teachable");
        entityPO6.setDescription("fidata - hudi入仓配置表");

        poList.add(entityPO1);
        poList.add(entityPO2);
        poList.add(entityPO3);
        poList.add(entityPO4);
        poList.add(entityPO5);
        poList.add(entityPO6);

        // 使用流和收集器创建一个Map，key为name，value为描述为"fidata - hudi入仓配置表"的MetadataEntityPO列表
        Map<String, List<MetadataEntityPO>> nameToDesiredDescMap = poList.stream()
                .filter(po -> "fidata - hudi入仓配置表".equals(po.getDescription()))
                .collect(Collectors.groupingBy(MetadataEntityPO::getName));

        // 移除那些名字与已知需要保留的对象相同，但描述不是"fidata - hudi入仓配置表"的对象
        poList.removeIf(po -> nameToDesiredDescMap.containsKey(po.getName())
                && !"fidata - hudi入仓配置表".equals(po.getDescription()));

        System.out.println(poList);

        System.out.println(poList);

    }

}
