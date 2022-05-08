package com.fisk.mdm.utlis;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.service.mdmBEBuild.dto.DataSourceConDTO;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.fisk.common.service.mdmBEBuild.AbstractDbHelper.execQueryResultList;

/**
 * @Author WangYan
 * @Date 2022/5/6 14:19
 * @Version 1.0
 */
public class DataSynchronizationUtils {

    @Value("${pgsql-mdm.type}")
    DataSourceTypeEnum type;
    @Value("${pgsql-mdm.url}")
    String connectionStr;
    @Value("${pgsql-mdm.username}")
    String acc;
    @Value("${pgsql-mdm.password}")
    String pwd;

    @Resource
    EntityService entityService;


    /**
     * stg数据同步
     * @param entityId
     * @param batchCode
     */
    public void stgDataSynchronize(Integer entityId,String batchCode){

        // 1.查询属性配置信息
        EntityInfoVO entityInfoVo = entityService.getAttributeById(entityId);
        String mdmTableName = entityInfoVo.getTableName();
        String stgTableName = "stg_" + entityInfoVo.getModelId() + "_" + entityInfoVo.getId();

        // 2.查询需要同步的数据
        String sql = "SELECT * FROM " + stgTableName + " WHERE fidata_batch_code = '" + batchCode +"'";

        DataSourceConDTO dto = new DataSourceConDTO();
        dto.setConStr(connectionStr);
        dto.setConAccount(acc);
        dto.setConPassword(pwd);
        dto.setConType(type);

        // 3.结果集转换
        List<Map<String, Object>> resultList = execQueryResultList(sql, dto);
        Map<String, List<Object>> listMap = new HashMap<>();

        resultList.stream().filter(Objects::nonNull)
                .forEach(e -> {

                    // key值
                    AtomicReference<String> key = null;
                    List<Object> valueList = new ArrayList<>();
                    e.forEach((k,v) -> {
                        key.set(k);
                        valueList.add(v);
                    });

                    listMap.put(key.toString(),valueList);
                });

        // 名称转换
        listMap.forEach((k,v) -> {
            entityInfoVo.getAttributeList().stream().filter(e -> k.equals(e.getName()))
                    .forEach(e -> {
                        listMap.put(e.getColumnName(),v);
                    });
        });

        // 4.数据类型转换

        // 5.数据导入
    }
}
