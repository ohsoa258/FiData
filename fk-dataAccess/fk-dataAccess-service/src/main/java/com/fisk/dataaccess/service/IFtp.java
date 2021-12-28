package com.fisk.dataaccess.service;

import com.fisk.common.response.ResultEntity;
import com.fisk.dataaccess.dto.DbConnectionDTO;

/**
 * @author Lock
 * @version 1.0
 * @description ftp数据源接口
 * @date 2021/12/28 10:50
 */
public interface IFtp {
    /**
     * 测试ftp数据源连接
     *
     * @param dto dto
     * @return 连接结果
     */
    ResultEntity<Object> connectFtp(DbConnectionDTO dto);
}
