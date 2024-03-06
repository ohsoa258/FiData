package com.fisk.dataservice.dto.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author dick
 * @version v1.0
 * @description api DTO
 * @date 2022/1/6 14:51
 */
@Data
public class ApiConfigDTO
{
    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称")
    @NotNull()
    public String apiName;

    /**
     * api描述
     */
    @ApiModelProperty(value = "api描述")
    @NotNull()
    public String apiDesc;

    /**
     * 表名，带架构名
     */
    @ApiModelProperty(value = "表名，带架构名")
    public String tableName;

    /**
     * 表架构名
     */
    @ApiModelProperty(value = "表架构名")
    public String tableFramework;

    /**
     * 表名，不带架构名
     */
    @ApiModelProperty(value = "表名，不带架构名")
    public String tableRelName;

    /**
     * 表别名
     */
    @ApiModelProperty(value = "表别名")
    public String tableNameAlias;

    /**
     * 表类型 1：表  2：视图
     */
    @ApiModelProperty(value = "表类型 1：表  2：视图")
    public int tableType;

    /**
     * 表业务类型 1：事实表、2：维度表、3、指标表  4、宽表
     */
    @ApiModelProperty(value = "表业务类型 1：事实表、2：维度表、3、指标表  4、宽表")
    public int tableBusinessType;

    /**
     * 表路径
     */
    @ApiModelProperty(value = "表路径")
    public String tablePath;

    /**
     * sql语句
     */
    @ApiModelProperty(value = "sql语句")
    public String createSql;

    /**
     * sql语句，查询总条数
     */
    @ApiModelProperty(value = "sql语句，查询总条数")
    public String createCountSql;
    
    /**
     * api类型 1 sql、2 自定义sql
     */
    @ApiModelProperty(value = "api类型 1 sql、2 自定义sql")
    public int apiType;

    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int datasourceId;

    /**
     * 创建api类型
     */
    @ApiModelProperty(value = "创建api类型：1 创建新api 2 使用现有api 3 代理API")
    public Integer createApiType = 0;

    /**
     * 现有api地址
     */
    @ApiModelProperty(value = "现有api地址")
    public String apiAddress;

    /**
     * api代理地址
     */
    @ApiModelProperty(value = "api代理地址")
    public String apiProxyUrl;


    /**
     * 是否是重点接口 0否，1是
     */
    @ApiModelProperty(value = "是否是重点接口 0否，1是")
    private int importantInterface;


    /**
     * 失效时间
     */
    @ApiModelProperty(value = "失效时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expirationTime;

    /**
     * 有效期类型 1:永久有效 2:有效期至（yyyy-MM-dd HH:mm:ss）
     */
    @ApiModelProperty(value = "有效期类型 1:永久有效 2:有效期至（yyyy-MM-dd HH:mm:ss）")
    private Integer expirationType;

    /**
     * api父级菜单id
     */
    @ApiModelProperty(value = "api父级菜单id")
    private Integer menuId;

    /**
     * api菜单id
     */
    @ApiModelProperty(value = "api菜单id")
    private Integer apiMenuId;

    /**
     * 有无最大限制 0无 1有
     */
    @ApiModelProperty(value = "有无最大限制 0无 1有")
    private Integer maxSizeType;

    /**
     * api最大单次查询条数
     */
    @ApiModelProperty(value = "api最大单次查询条数")
    private Integer maxSize;

    /**
     * 是否开启缓存 0否，1是
     */
    @ApiModelProperty(value = "是否开启缓存 0否，1是")
    private int enableCache;

    /**
     * 缓存时间(秒)
     */
    @ApiModelProperty(value = "缓存时间(秒)")
    private Integer cacheTime;
}
