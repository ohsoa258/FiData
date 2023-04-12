package com.fisk.datamodel.service.strategy;

import com.fisk.datamodel.service.IBuildOverlaySqlPreview;
import org.apache.xmlbeans.impl.common.ConcurrentReaderHashMap;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
public class BuildSqlStrategy {

    private static final Map<String, IBuildOverlaySqlPreview> SERVICE_MAP = new ConcurrentReaderHashMap();

    public static IBuildOverlaySqlPreview getService(String driverType){
        return SERVICE_MAP.get(driverType);
    }

    public static void register(String driverType, IBuildOverlaySqlPreview service){
        Assert.notNull(driverType, "type can`t be null");
        SERVICE_MAP.put(driverType, service);
    }
}
