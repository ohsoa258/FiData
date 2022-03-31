package com.fisk.dataaccess.vo;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AppDataSourceVO extends BaseDTO {

    /**
     * 驱动类型
     */
    private String driveType;

    /**
     * 连接字符串
     */
    private String connectStr;

    /**
     * 连接账号
     */
    private String connectAccount;

    /**
     * 连接密码
     */
    private String connectPwd;

    public AppDataSourceVO(BaseEntity entity) {
        super(entity);
    }

    public static <T extends BaseEntity> List<AppDataSourceVO> convertEntityList(Collection<T> list){
        return list.stream().map(AppDataSourceVO::new).collect(Collectors.toList());
    }

}
