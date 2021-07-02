package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.mapper.AppDataSourceMapper;
import com.fisk.dataaccess.service.IAppDataSource;
import org.springframework.stereotype.Service;

/**
 * @author Lock
 */
@Service
public class AppDataSourceImpl extends ServiceImpl<AppDataSourceMapper, AppDataSourcePO> implements IAppDataSource {
}
