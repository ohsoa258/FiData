package com.fisk.dataaccess.dto;

import com.fisk.common.dto.BaseDTO;
import com.fisk.common.entity.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "驱动类型", required = true)
    public String driveType;

    /**
     * 主机名
     */
    @ApiModelProperty(value = "服务器地址", required = true)
    public String host;

    /**
     * 端口号
     */
    @ApiModelProperty(value = "端口", required = true)
    public String port;

    /**
     * 数据库名
     */
    @ApiModelProperty(value = "数据库", required = true)
    public String dbName;

    /**
     * 连接字符串
     */
    @ApiModelProperty(value = "连接字符串", required = true)
    public String connectStr;

    /**
     * 连接账号
     */
    @ApiModelProperty(value = "连接账号", required = true)
    public String connectAccount;

    /**
     * 连接密码
     */
    @ApiModelProperty(value = "连接密码", required = true)
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
