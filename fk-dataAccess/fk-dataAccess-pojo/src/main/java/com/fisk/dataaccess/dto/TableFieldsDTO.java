package com.fisk.dataaccess.dto;

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
 *
 * 实时对象
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TableFieldsDTO extends BaseDTO {

    /**
     * 主键id
     */
    public long id;

    /**
     * 功能类型
     * 0:旧数据不操作  1:修改表字段  2:新增表字段
     */
    public int funcType;

    /**
     * table_access（id）
     */
    public long tableAccessId;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 字段描述
     */
    public String fieldDes;

    /**
     * 字段类型
     */
    public String fieldType;

    /**
     * 字段长度
     */
    public long fieldLength;

    /**
     * 1是主键，0非主键
     */
    public int isPrimarykey;

    /**
     * 1是业务时间，0非业务时间
     */
    public int isBusinesstime;

    /**
     * 1：实时物理表的字段，0：非实时物理表的字段
     */
    public long isRealtime;

    /**
     * 1是时间戳，0非时间戳
     */
    public int isTimestamp;
    /**
     * 应用简写
     */
    public String appbAbreviation;
    /**
     * 原表名
     */
    public String originalTableName;

    /**
     * 1：实时物理表的字段，0：非实时物理表的字段
     */
//    public int isTimestamp;

    public TableFieldsDTO(BaseEntity entity) {
        super(entity);
    }


    /**
     * 将PO集合转为DTO对象
     *
     * @param list PO对象集合
     * @param <T>  PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<TableFieldsDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(TableFieldsDTO::new).collect(Collectors.toList());
    }

}
