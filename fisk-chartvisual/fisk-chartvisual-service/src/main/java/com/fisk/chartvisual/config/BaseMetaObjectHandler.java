package com.fisk.chartvisual.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.fisk.common.constants.SqlConstants;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

/**
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
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        this.setFieldValByName("createTime", timestamp , metaObject);
        this.setFieldValByName("delFlag", Integer.parseInt(SqlConstants.NOT_DEL), metaObject);
        if (userHelper != null) {
            UserInfo user = userHelper.getLoginUserInfo();
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
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        this.setFieldValByName("updateTime", timestamp, metaObject);
        if (userHelper != null) {
            UserInfo user = userHelper.getLoginUserInfo();
            if(user != null && user.id != null){
                this.setFieldValByName("updateUser", user.id.toString(), metaObject);
            }
        }
    }
}
