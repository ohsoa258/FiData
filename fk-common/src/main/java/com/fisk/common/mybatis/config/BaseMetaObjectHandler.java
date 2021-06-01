package com.fisk.common.mybatis.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.fisk.common.constants.SqlConstants;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author gy
 */
public class BaseMetaObjectHandler implements MetaObjectHandler {
    /**
     * 插入时的填充策略
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("createTime", new Date(), metaObject);
        this.setFieldValByName("delFlag", Integer.parseInt(SqlConstants.NOT_DEL), metaObject);
    }

    /**
     * 更新时的填充策略
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }
}
