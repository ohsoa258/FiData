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
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AppDataSourceDTO extends BaseDTO {

    /**
     * 驱动类型
     */
    public String driveType;

    /**
     * 主机名
     */
    public String host;

    /**
     * 端口号
     */
    public String port;

    /**
     * 数据库名
     */
    public String dbName;

    /**
     * 连接字符串
     */
    public String connectStr;

    /**
     * 连接账号
     */
    public String connectAccount;

    /**
     * 连接密码
     */
    public String connectPwd;

    public AppDataSourceDTO(BaseEntity entity) {
        super(entity);
    }

    /**
     * 将PO集合转为DTO对象
     *
     * @param list PO对象集合
     * @param <T>  PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<AppDataSourceDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(AppDataSourceDTO::new).collect(Collectors.toList());
    }

}
