package com.fisk.dataaccess.dto.app;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 * <p>
 * 应用注册添加应用
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AppRegistrationDTO extends BaseDTO {

    @ApiModelProperty(value = "主键")
    public long id;

    @ApiModelProperty(value = "应用名称", required = true)
    public String appName;

    /**
     * 应用简称
     */
    @ApiModelProperty(value = "应用简称", required = true)
    public String appAbbreviation;
    /**
     * 应用描述
     */
    @ApiModelProperty(value = "应用描述")
    public String appDes;
    @ApiModelProperty(value = "应用类型(0:实时应用  1:非实时应用)", required = true)
    public int appType;
    @ApiModelProperty(value = "应用负责人", required = true)
    public String appPrincipal;
    @ApiModelProperty(value = "应用负责人邮箱", required = true)
    public String appPrincipalEmail;
    @ApiModelProperty(value = "创建时间")
    public Date createTime;
    @ApiModelProperty(value = "是否将应用简称作为schema使用")
    public Boolean whetherSchema;

    public AppDataSourceDTO appDatasourceDTO;


    public AppRegistrationDTO(BaseEntity entity) {
        super(entity);
    }

    /**
     * 将PO集合转为DTO对象
     *
     * @param list PO对象集合
     * @param <T>  PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<AppRegistrationDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(AppRegistrationDTO::new).collect(Collectors.toList());
    }

}
