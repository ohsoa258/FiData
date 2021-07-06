package com.fisk.datamodel.dto;

import com.baomidou.mybatisplus.annotation.TableId;
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
    private long id;

    /**
     * 业务域名称
     */
    private String businessName;

    /**
     * 业务域描述
     */
    private String businessDes;

    /**
     * 业务需求管理员
     */
    private String businessAdmin;

    /**
     * 应用负责人邮箱
     */
    private String businessEmail;

    /**
     * 创建时间
     */
//    private DateTime createTime;

    /**
     * 创建人
     */
//    private String createUser;

    /**
     * 更新时间
     */
//    private DateTime updateTime;

    /**
     * 更新人
     */
//    private String updateUser;

    /**
     * 逻辑删除(1: 未删除; 0: 删除)
     */
//    private int delFlag;

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
