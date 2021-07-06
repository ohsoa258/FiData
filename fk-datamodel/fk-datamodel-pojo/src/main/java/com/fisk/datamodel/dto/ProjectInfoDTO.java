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
public class ProjectInfoDTO extends BaseDTO {

    /**
     * 主键
     */
    public long id;

    /**
     * 业务表名称
     */
    public String businessName;

    /**
     * 项目空间模式
     */
    public String projectPattern;

    /**
     * 项目名称
     */
    public String projectName;

    /**
     * 项目描述
     */
    public String projectDes;

    /**
     * 项目负责人
     */
    public String projectPrincipal;

    /**
     * 负责人邮箱
     */
    public String principalEmail;

    public ProjectInfoDTO(BaseEntity entity) {
        super(entity);
    }

    /**
     * 将PO集合转为DTO对象
     *
     * @param list PO对象集合
     * @param <T>  PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<ProjectInfoDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(ProjectInfoDTO::new).collect(Collectors.toList());
    }
}
