package com.fisk.chartvisual.service.impl;

import com.fisk.chartvisual.map.DataSourceConMap;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.service.IUseDataBase;

import javax.annotation.Resource;

/**
 * @author gy
 */
public class UseDataBaseImpl implements IUseDataBase {

    @Resource
    private DataSourceConMapper mapper;

    @Override
    public boolean testConnection(int id) {

        return false;
    }
}
