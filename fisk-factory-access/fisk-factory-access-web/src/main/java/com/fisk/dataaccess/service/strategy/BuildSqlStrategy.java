package com.fisk.dataaccess.service.strategy;

import com.fisk.dataaccess.service.IBuildOverlaySqlPreview;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
public class BuildSqlStrategy {

    private static final Map<String, IBuildOverlaySqlPreview> SERVER_MAP = new ConcurrentHashMap<>();

    public static IBuildOverlaySqlPreview getService(String driverType){
        return SERVER_MAP.get(driverType);
    }

    public static void register(String driverType, IBuildOverlaySqlPreview service){
        Assert.notNull(driverType, "type can`t be null");
        SERVER_MAP.put(driverType, service);
    }
}
