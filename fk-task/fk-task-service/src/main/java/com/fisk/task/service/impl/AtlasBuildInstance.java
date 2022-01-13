package com.fisk.task.service.impl;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.fisk.common.entity.BusinessResult;
import com.fisk.task.dto.atlas.AtlasEntityDeleteDTO;
import com.fisk.task.dto.atlas.AtlasEntityProcessDTO;
import com.fisk.task.service.IAtlasBuildInstance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    @Value("${atlasconstr.url}")
    private String atlas_url;
    @Value("${atlasconstr.username}")
    private String atlas_username;
    @Value("${atlasconstr.password}")
    private String atlas_pwd;

    @Resource
    IAtlasBuildInstance atlas;

}
