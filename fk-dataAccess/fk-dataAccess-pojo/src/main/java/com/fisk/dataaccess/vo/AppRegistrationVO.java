package com.fisk.dataaccess.vo;

import com.fisk.common.dto.BaseDTO;
import com.fisk.common.entity.BaseEntity;
import com.fisk.dataaccess.dto.AppDataSourceDTO;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Lock
 * @data: 2021/5/26 14:59
 *
 * 应用注册添加应用
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AppRegistrationVO extends  BaseDTO{

    private String id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用描述
     */
    private String appDes;

    /**
     * 应用类型
     */
    private byte appType;

    /**
     * 应用负责人
     */
    private String appPrincipal;

    /**
     * 应用负责人邮箱
     */
    private String appPrincipalEmail;

    /**
     * 创建时间
     */
    private DateTime createTime;

    private String connectPwd;
    public AppRegistrationVO(BaseEntity entity) {
        super(entity);
    }

    /**
     * 将PO集合转为DTO对象
     * @param list PO对象集合
     * @param <T> PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<AppRegistrationVO> convertEntityList(Collection<T> list){
        return list.stream().map(AppRegistrationVO::new).collect(Collectors.toList());
    }

}
