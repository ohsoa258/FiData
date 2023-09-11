package com.fisk.dataaccess.dto.v3;

import com.fisk.dataaccess.enums.ScanStartupModeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TbTableAccessDTO {

    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "父id", required = true)
    public int pid;

    /**
     * tb_app_registration表id
     */
    @ApiModelProperty(value = "tb_app_registration表id")
    public Long appId;

    @ApiModelProperty(value = "实时api主键", required = true)
    public Long apiId;

    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    public String appName;
    /**
     * 物理表名
     */
    @ApiModelProperty(value = "物理表名")
    public String tableName;

    /**
     * 物理表描述
     */
    @ApiModelProperty(value = "物理表描述")
    public String tableDes;

    /**
     * 如果是实时物理表，需要提供数据同步地址
     */
    @ApiModelProperty(value = "如果是实时物理表，需要提供数据同步地址")
    public String syncSrc;

    /**
     * 0是实时物理表，1是非实时物理表
     */
    @ApiModelProperty(value = "0是实时物理表，1是非实时物理表")
    public Integer isRealtime;

    /**
     * 0: 未发布  1: 发布成功  2: 发布失败
     */
    @ApiModelProperty(value = "0: 未发布  1: 发布成功  2: 发布失败")
    public Integer publish;
    /**
     * SQL脚本
     */
    @ApiModelProperty(value = "SQL脚本")
    public String sqlScript;

    /**
     * sheet页
     */
    @ApiModelProperty(value = "sheet页")
    public String sheet;
    /**
     * excel开始读取数据的行数
     */
    @ApiModelProperty(value = "excel开始读取数据的行数")
    public Integer startLine;

    @ApiModelProperty(value = "发布错误信息", required = true)
    public String publishErrorMsg;

    /**
     * 0: 发布;  1: 保存sql脚本
     */
    @ApiModelProperty(value = "0: 发布;  1: 保存sql脚本")
    public int flag;

    /**
     * 用于拦截sql保存时空字符
     */
    @ApiModelProperty(value = "用于拦截sql保存时空字符")
    public int sqlFlag;

    /**
     * oracle-cdc名称
     */
    @ApiModelProperty(value = "oracle-cdc名称")
    public String pipelineName;

    /**
     * oracle-cdc检查点时间
     */
    @ApiModelProperty(value = "oracle-cdc检查点时间")
    public Integer checkPointInterval;

    /**
     * oracle-cdc检查点时间单位
     */
    @ApiModelProperty(value = "oracle-cdc检查点时间单位")
    public String checkPointUnit;

    /**
     * 0:从最开始读 1:从最新的读
     */
    @ApiModelProperty(value = "0:从最开始读 1:从最新的读")
    public ScanStartupModeEnum scanStartupMode;

    /**
     * 现有表
     */
    @ApiModelProperty(value = "现有表")
    public boolean useExistTable;

    /**
     * 物理表显示名称
     */
    @ApiModelProperty(value = "物理表显示名称")
    public String displayName;

    /**
     * 同步方式
     */
    @ApiModelProperty(value = "同步方式")
    public Integer syncMode;

    /**
     * 系统数据源id
     */
    @ApiModelProperty(value = "系统数据源id")
    public Integer appDataSourceId;

    /**
     * 是否是重点接口 0否，1是
     */
    @ApiModelProperty(value = "是否是重点接口 0否，1是")
    public Integer isImportantInterface;


}
