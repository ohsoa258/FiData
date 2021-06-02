package com.fisk.dataaccess.dto;

import com.fisk.common.dto.BaseDTO;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Collections;
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
public class AppRegistrationDTO extends BaseDTO {

//    private String id;

    private String appName;

    private String appDes;

    private byte appType;

    private String appPrincipal;

    private String appPrincipalEmail;

    private DateTime createTime;

//    private String createUser;

//    private DateTime updateTime;

//    private String updateUser;

//    private byte delFlag;

    /**
     * 数据源
     */
    private AppDataSourceDTO appDatasourceDTO;


    public AppRegistrationDTO(BaseEntity entity) {
        super(entity);
    }

    /**
     * 将PO集合转为DTO对象
     *
     * @param list PO对象集合
     * @param <T>  PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<AppRegistrationDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(AppRegistrationDTO::new).collect(Collectors.toList());
    }

}
