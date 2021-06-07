package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataaccess.entity.AppDriveTypePO;
import com.fisk.dataaccess.mapper.AppDriveTypeMapper;
import com.fisk.dataaccess.service.IAppDriveType;
import org.springframework.stereotype.Service;

/**
 * @author: Lock
 */
@Service
public class AppDriveTypeImpl extends ServiceImpl<AppDriveTypeMapper, AppDriveTypePO> implements IAppDriveType {
}
