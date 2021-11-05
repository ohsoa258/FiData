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
 * 非实时对象
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TableAccessNonDTO extends BaseDTO {

    public long id;

    /**
     *  tb_app_registration表id
     */
    public long appId;

    /**
     * 应用名称
     */
    public String appName;

    /**
     *  物理表名
     */
    public String tableName;

    /**
     *  物理表描述
     */
    public String tableDes;

    /**
     *  如果是实时物理表，需要提供数据同步地址
     */
    public String syncSrc;

    /**
     *  0是实时物理表，1是非实时物理表
     */
    public int isRealtime;

    /**
     * 0: 未发布  1: 发布成功  2: 发布失败
     */
    public Integer publish;
    /**
     * SQL脚本
     */
    public String sqlScript;

    /**
     * 表字段对象
     */
    public List<TableFieldsDTO> list;

    /**
     * 业务时间对象
     */
    public TableBusinessDTO businessDTO;

    /**
     * 同步方式对象
     */
    public TableSyncmodeDTO tableSyncmodeDTO;

    public TableAccessNonDTO(BaseEntity entity) {
        super(entity);
    }

    /**
     * 将PO集合转为DTO对象
     *
     * @param list PO对象集合
     * @param <T>  PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<TableAccessNonDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(TableAccessNonDTO::new).collect(Collectors.toList());
    }
}
