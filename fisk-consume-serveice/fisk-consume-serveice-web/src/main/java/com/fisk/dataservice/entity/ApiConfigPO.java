package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version v1.0
 * @description api实体类
 * @date 2022/1/6 14:51
 */
@Data
@TableName("tb_api_config")
public class ApiConfigPO extends BasePO
{
    /**
     * api名称
     */
    public String apiName;

    /**
     * api标识code
     */
    public String apiCode;

    /**
     * api描述
     */
    public String apiDesc;

    /**
     * api类型 1 sql、2 自定义sql
     */
    public int apiType;

    /**
     * 数据源id
     */
    public int datasourceId;

    /**
     * 表名称
     */
    public String tableName;

    /**
     * 表别名
     */
    public String tableNameAlias;

    /**
     * 表类型 1：表  2：视图
     */
    public int tableType;

    /**
     * 表业务类型 1：事实表、2：维度表、3、指标表  4、宽表
     */
    public int tableBusinessType;

    /**
     * 表路径
     */
    public String tablePath;

    /**
     * sql语句
     */
    public String createSql;

    /**
     * sql语句，查询总条数
     */
    public String createCountSql;

    /**
     * 创建api类型：1 创建新api 2 使用现有api 3 代理API
     */
    public Integer createApiType;

    /**
     * api地址
     */
    public String apiAddress;

    /**
     * api代理地址
     */
    public String apiProxyUrl;

    /**
     * 是否是重点接口 0否，1是
     */
    private Integer importantInterface;

    /**
     * 失效时间
     */
    private LocalDateTime expirationTime;

    /**
     * 有效期类型 1:永久有效 2:有效期至（yyyy-MM-dd HH:mm:ss）
     */
    private Integer expirationType;

    /**
     * 父级菜单id
     */
    private Integer menuId;

    /**
     * api菜单id
     */
    private Integer apiMenuId;


    /**
     * 是否开启缓存 0否，1是
     */
    private int enableCache;

    /**
     * 缓存时间(秒)
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer cacheTime;


    /**
     * 有无最大限制 0无 1有
     */
    private int maxSizeType;

    /**
     * api最大单次查询条数
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer maxSize;

    /**
     * 标签
     */
    private String tag;
}
