package com.fisk.mdm;

import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.utlis.DataSynchronizationUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
class FiskMdmModelApplicationTests {

    @Resource
    EntityService entityService;
    @Resource
    DataSynchronizationUtils dataSynchronizationUtils;

    @Test
    void contextLoads() {
        dataSynchronizationUtils.stgDataSynchronize(124, "b23a805d-9740-43b9-8ef7-78ddbdf896c9");
    }

    @Test
   public void Test1(){
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("id",1);
        map1.put("code","code001");
        list.add(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("id",2);
        map2.put("code","code002");
        list.add(map2);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("id",3);
        map3.put("code","code003");
        list.add(map3);

        Map<String, Object> map4 = new HashMap<>();
        map4.put("id",4);
        map4.put("code","code004");
        list.add(map4);

        List<Map<String, Object>> codelist = new ArrayList<>();
        Map<String, Object> map5 = new HashMap<>();
        map5.put("id",5);
        map5.put("code","code003");
        codelist.add(map5);

        Map<String, Object> map6 = new HashMap<>();
        map6.put("id",6);
        map6.put("code","code004");
        codelist.add(map6);

        List<Map<String, Object>> collect = new ArrayList<>();
        list.stream().forEach(e -> {
            codelist.stream().filter(item -> !e.get("code").equals(item.get("code")))
                    .forEach(item -> {
                        // 求出差集
                        collect.add(e);;
                    });
        });

        Map<Map<String, Object>, Long> countMap = collect.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<Map<String, Object>> collect1 = countMap.keySet().stream().filter(e -> countMap.get(e) > 1).distinct().collect(Collectors.toList());
        System.out.println(collect1);
    }
}
