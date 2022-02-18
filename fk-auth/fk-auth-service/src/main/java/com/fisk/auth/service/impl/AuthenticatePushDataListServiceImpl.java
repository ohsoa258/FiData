package com.fisk.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.auth.entity.AuthenticatePushDataListPO;
import com.fisk.auth.mapper.AuthenticatePushDataListMapper;
import com.fisk.auth.service.IAuthenticatePushDataListService;
import com.fisk.common.redis.RedisKeyEnum;
import com.fisk.common.redis.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author gy
 */
@Service
public class AuthenticatePushDataListServiceImpl extends ServiceImpl<AuthenticatePushDataListMapper, AuthenticatePushDataListPO> implements IAuthenticatePushDataListService {

    @Resource
    private RedisUtil redis;

    @Override
    public boolean loadPushDataListToRedis(String path) {
        QueryWrapper<AuthenticatePushDataListPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "path", "details");
        List<AuthenticatePushDataListPO> data = this.list(queryWrapper);
        Map<String, Object> map = data.stream().collect(Collectors.toMap(AuthenticatePushDataListPO::getPath, JSON::toJSONString, (k1, k2) -> k1));
        redis.hmset(RedisKeyEnum.AUTH_PUSH_DATA_LIST.getName(), map, RedisKeyEnum.AUTH_PUSH_DATA_LIST.getValue());

        if (StringUtils.isNotEmpty(path)) {
            return map.containsKey(path);
        }
        return false;
    }

    @Override
    public boolean pushDataPathIsExists(String path) {
        if(!redis.hasKey(RedisKeyEnum.AUTH_PUSH_DATA_LIST.getName())){
            return loadPushDataListToRedis(path);
        }

        return redis.hHasKey(RedisKeyEnum.AUTH_PUSH_DATA_LIST.getName(), path);
    }
}
