package com.fisk.dataaccess.vo;

import com.fisk.common.entity.BaseEntity;
import com.fisk.common.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 *
 * 应用注册添加应用
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AppRegistrationVO extends BaseVO {

    /**
     * 应用名称
     */
    public String appName;

    /**
     * 应用描述
     */
    public String appDes;

    /**
     * 应用类型
     */
    public int appType;

    /**
     * 应用负责人
     */
    public String appPrincipal;

    /**
     * 应用负责人邮箱
     */
    public String appPrincipalEmail;

    /**
     * 创建时间
     */
    public DateTime createTime;

    /**
     * 创建人
     */
    public String createUser;

    /**
     * 更新时间
     */
    public DateTime updateTime;

    /**
     * 更新人
     */
    private String updateUser;

    /**
     * 逻辑删除(1: 未删除; 0: 删除)
     */
    public byte delFlag;

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

        if (list == null) {
            return Collections.emptyList();
        }

        return list.stream().map(AppRegistrationVO::new).collect(Collectors.toList());
    }

}
