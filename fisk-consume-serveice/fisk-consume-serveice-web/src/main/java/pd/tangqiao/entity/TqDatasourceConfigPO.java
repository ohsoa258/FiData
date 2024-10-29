package pd.tangqiao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @TableName tq_datasource_config
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tq_datasource_config")
@Data
public class TqDatasourceConfigPO extends BasePO implements Serializable {

    /**
     * 数据源名称
     */
    @ApiModelProperty(value = "数据源名称")
    private String name;

    /**
     * 连接字符串
     */
    @ApiModelProperty(value = "连接字符串")
    private String conStr;

    /**
     * ip
     */
    @ApiModelProperty(value = "ip")
    private String conIp;

    /**
     * 端口
     */
    @ApiModelProperty(value = "端口")
    private Integer conPort;

    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    private String conDbname;

    /**
     * 连接类型
     */
    @ApiModelProperty(value = "连接类型")
    private Integer conType;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号")
    private String conAccount;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    private String conPassword;

    /**
     * 请求协议
     */
    @ApiModelProperty(value = "请求协议")
    private String protocol;

    /**
     * 平台
     */
    @ApiModelProperty(value = "平台")
    private String platform;

    /**
     * oracle服务类型：1服务名 2SID
     */
    @ApiModelProperty(value = "oracle服务类型")
    private Integer serviceType;

    /**
     * oracle服务名
     */
    @ApiModelProperty(value = "oracle服务名")
    private String serviceName;

    /**
     * oracle域名
     */
    @ApiModelProperty(value = "oracle域名")
    private String domainName;

    /**
     * 数据源类型：1系统数据源 2外部数据源
     */
    @ApiModelProperty(value = "数据源类型：1系统数据源 2外部数据源")
    private Integer sourceType;

    /**
     * 数据源业务类型：1dw 2ods 3mdm 4olap 5lake
     */
    @ApiModelProperty(value = "数据源业务类型：1dw 2ods 3mdm 4olap 5lake")
    private Integer sourceBusinessType;

    /**
     * 数据源用途
     */
    @ApiModelProperty(value = "数据源用途")
    private String purpose;

    /**
     * 负责人
     */
    @ApiModelProperty(value = "负责人")
    private String principal;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}