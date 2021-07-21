package com.fisk.dataaccess.dto;

import com.fisk.common.dto.BaseDTO;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
public class AppRegistrationDTO extends BaseDTO {

    public long id;

    public String appName;
    
    /**
     * 应用简称
     */
    public String appAbbreviation;

    public String appDes;

    public int appType;

    public String appPrincipal;

    public String appPrincipalEmail;

    public Date createTime;

    public AppDataSourceDTO appDatasourceDTO;


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
