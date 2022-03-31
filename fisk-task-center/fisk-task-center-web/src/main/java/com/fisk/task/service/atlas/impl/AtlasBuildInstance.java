package com.fisk.task.service.atlas.impl;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.fisk.task.service.atlas.IAtlasBuildInstance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/8 12:09
 * Description:
 */
@Service
@Slf4j
public class AtlasBuildInstance implements IAtlasBuildInstance {

    @ApolloConfig
    private Config config;
    //private String jvm=config.getProperty("spring.rabbitmq.virtual-host",null);
    private String spring_redis_host;


    @Resource
    IAtlasBuildInstance atlas;

}
