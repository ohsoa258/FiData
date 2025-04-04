package com.fisk.common.framework.mybatis.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.fisk.common.core.constants.SqlConstants;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * mybatis 填充策略
 *
 * @author gy
 */
@Component
public class BaseMetaObjectHandler implements MetaObjectHandler {

    @Resource
    UserHelper userHelper;

    /**
     * 插入时的填充策略
     *
     * @param metaObject meta
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("delFlag", Integer.parseInt(SqlConstants.NOT_DEL), metaObject);
        if (userHelper != null) {
            UserInfo user = userHelper.getLoginUserInfoNotThrowError();
            if(user != null && user.id != null){
                this.setFieldValByName("createUser", user.id.toString(), metaObject);
            }
        }
    }

    /**
     * 更新时的填充策略
     *
     * @param metaObject meta
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        if (userHelper != null) {
            UserInfo user = userHelper.getLoginUserInfoNotThrowError();
            if(user != null && user.id != null){
                this.setFieldValByName("updateUser", user.id.toString(), metaObject);
            }
        }
    }
}
