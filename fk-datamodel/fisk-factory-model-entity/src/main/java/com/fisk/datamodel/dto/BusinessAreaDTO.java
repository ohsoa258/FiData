package com.fisk.datamodel.dto;

import com.fisk.common.dto.BaseDTO;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BusinessAreaDTO extends BaseDTO {

    /**
     * 主键
     */
    public long id;

    /**
     * 业务域名称
     */
    public String businessName;

    /**
     * 业务域描述
     */
    public String businessDes;

    /**
     * 业务需求管理员
     */
    public String businessAdmin;

    /**
     * 应用负责人邮箱
     */
    public String businessEmail;


    public BusinessAreaDTO(BaseEntity entity) {
        super(entity);
    }

    /**
     * 将PO集合转为DTO对象
     *
     * @param list PO对象集合
     * @param <T>  PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<BusinessAreaDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(BusinessAreaDTO::new).collect(Collectors.toList());
    }

}
