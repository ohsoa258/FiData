package com.fisk.dataaccess.vo;

import com.fisk.common.dto.BaseDTO;
import com.fisk.common.entity.BaseEntity;
import com.fisk.common.vo.BaseVO;
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
public class AppDriveTypeVO extends BaseVO {

    private String name;

    private String type;

    public AppDriveTypeVO(BaseEntity entity) {
        super(entity);
    }

    /**
     * 将PO集合转为DTO对象
     *
     * @param list PO对象集合
     * @param <T>  PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<AppDriveTypeVO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(AppDriveTypeVO::new).collect(Collectors.toList());
    }

}
