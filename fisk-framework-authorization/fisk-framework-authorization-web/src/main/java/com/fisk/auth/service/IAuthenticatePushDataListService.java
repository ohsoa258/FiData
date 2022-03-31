package com.fisk.auth.service;

/**
 * @author gy
 */
public interface IAuthenticatePushDataListService {
    /**
     * 加载数据到redis
     *
     * @param path 请求路径
     * @return 是否在推送数据名单
     */
    boolean loadPushDataListToRedis(String path);

    /**
     * 判断redis中是否有数据
     *
     * @param path path
     * @return 是否在推送名单表
     */
    boolean pushDataPathIsExists(String path);
}
