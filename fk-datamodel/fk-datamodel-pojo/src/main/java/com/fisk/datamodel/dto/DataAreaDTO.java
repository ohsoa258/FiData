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
 * @author: Lock
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DataAreaDTO extends BaseDTO {

    /**
     * 表主键
     */
    public long id;

/*    */
    /**
     * 业务域表id
     *//*
    public long businessid;*/

//    public BusinessNameDTO businessNameDTO;

    /**
     * 业务表名称
     */
    public String businessName;

    /**
     *  数据域名称
     */
    public String dataName;

    /**
     *  1true  0false
     */
    public boolean isShare;

    /**
     *  数据域描述
     */
    public String dataDes;


    public DataAreaDTO(BaseEntity entity) {
        super(entity);
    }

    /**
     * 将PO集合转为DTO对象
     *
     * @param list PO对象集合
     * @param <T>  PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<DataAreaDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(DataAreaDTO::new).collect(Collectors.toList());
    }
}
