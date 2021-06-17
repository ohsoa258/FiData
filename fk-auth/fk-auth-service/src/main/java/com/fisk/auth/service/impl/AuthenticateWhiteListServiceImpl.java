package com.fisk.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.auth.entity.AuthenticateWhiteListPO;
import com.fisk.auth.mapper.AuthenticateWhiteListMapper;
import com.fisk.auth.service.IAuthenticateWhiteListService;
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
public class AuthenticateWhiteListServiceImpl extends ServiceImpl<AuthenticateWhiteListMapper, AuthenticateWhiteListPO> implements IAuthenticateWhiteListService {

    @Resource
    private RedisUtil redis;

    @Override
    public boolean loadDataToRedis(String path) {
        QueryWrapper<AuthenticateWhiteListPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "path", "details");
        List<AuthenticateWhiteListPO> data = this.list(queryWrapper);
        Map<String, Object> map = data.stream().collect(Collectors.toMap(AuthenticateWhiteListPO::getPath, JSON::toJSONString, (k1, k2) -> k1));
        redis.hmset(RedisKeyEnum.AUTH_WHITELIST.getName(), map, RedisKeyEnum.AUTH_WHITELIST.getValue());

        if (StringUtils.isNotEmpty(path)) {
            return map.containsKey(path);
        }
        return false;
    }

    @Override
    public boolean pathIsExists(String path) {
        if(!redis.hasKey(RedisKeyEnum.AUTH_WHITELIST.getName())){
            return loadDataToRedis(path);
        }

        return redis.hHasKey(RedisKeyEnum.AUTH_WHITELIST.getName(), path);
    }

}
