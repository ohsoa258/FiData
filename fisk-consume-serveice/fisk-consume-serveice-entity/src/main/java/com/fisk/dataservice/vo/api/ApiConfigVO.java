package com.fisk.dataservice.vo.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version v1.0
 * @description api VO
 * @date 2022/1/10 17:51
 */
@Data
public class ApiConfigVO {
    /**
     * Id
     */
    @ApiModelProperty(value = "主键")
    public int id;

    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称")
    public String apiName;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

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
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int datasourceId;

    /**
     * 数据源类型
     */
    @ApiModelProperty(value = "数据源类型")
    public int datasourceType;

    /**
     * api标识code
     */
    @ApiModelProperty(value = "api标识code")
    public String apiCode;

    /**
     * api描述
     */
    @ApiModelProperty(value = "api描述")
    public String apiDesc;

    /**
     * api类型 1 sql、2 自定义sql
     */
    @ApiModelProperty(value = "api类型 1 sql、2 自定义sql")
    public int apiType;

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
     * 创建api类型
     */
    @ApiModelProperty(value = "创建api类型：1 创建新api 2 使用现有api 3 代理API")
    public Integer createApiType;

    /**
     * api地址
     */
    @ApiModelProperty(value = "api地址")
    public String apiAddress;

    /**
     * api代理转发地址
     */
    @ApiModelProperty(value = "api代理转发地址")
    public String apiProxyUrl;

    /**
     * api代理调用地址
     */
    @ApiModelProperty(value = "api代理调用地址")
    public String apiProxyCallUrl;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    public String createUser;

    /**
     * 是否是重点接口 0否，1是
     */
    @ApiModelProperty(value = "是否是重点接口 0否，1是")
    private int importantInterface;

    /**
     * 失效时间
     */
    @ApiModelProperty(value = "失效时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expirationTime;

    /**
     * 有效期类型 1:永久有效 2:有效期至（yyyy-MM-dd HH:mm:ss）
     */
    @ApiModelProperty(value = "有效期类型")
    private Integer expirationType;
}
