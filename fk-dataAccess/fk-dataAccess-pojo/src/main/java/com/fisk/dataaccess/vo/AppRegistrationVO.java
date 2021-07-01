package com.fisk.dataaccess.vo;

import com.fisk.common.dto.BaseDTO;
import com.fisk.common.entity.BaseEntity;
import com.fisk.common.vo.BaseVO;
import com.fisk.dataaccess.dto.AppDataSourceDTO;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
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

//    private long id;

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
    private int appType;

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

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 更新时间
     */
    private DateTime updateTime;

    /**
     * 更新人
     */
    private String updateUser;

    /**
     * 逻辑删除(1: 未删除; 0: 删除)
     */
    private byte delFlag;
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
