package com.fisk.datagovernance.vo.dataquality.datasource;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.datagovernance.enums.DataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 数据源 VO
 * @date 2022/3/22 14:51
 */
@Data
public class DataSourceConVO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 连接名称
     */
    @ApiModelProperty(value = "连接名称")
    public String name;

    /**
     * 连接字符串
     */
    @ApiModelProperty(value = "连接字符串")
    public String conStr;

    /**
     * ip
     */
    @ApiModelProperty(value = "ip")
    public String conIp;

    /**
     * 端口
     */
    @ApiModelProperty(value = "端口")
    public int conPort;

    /**
     * 数据源类型 1、FiData 2、自定义
     */
    @ApiModelProperty(value = "数据源类型 1、FiData 2、自定义")
    public SourceTypeEnum datasourceType;

    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    public String conDbname;

    /**
     * 连接类型
     */
    @ApiModelProperty(value = "连接类型")
    public DataSourceTypeEnum conType;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号")
    public String conAccount;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    public String conPassword;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public String createTime;
}
