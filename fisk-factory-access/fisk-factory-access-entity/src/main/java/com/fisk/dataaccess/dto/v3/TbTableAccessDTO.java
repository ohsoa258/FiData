package com.fisk.dataaccess.dto.v3;

import com.fisk.dataaccess.enums.ScanStartupModeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TbTableAccessDTO {
    public long id;

    @ApiModelProperty(value = "父id", required = true)
    public int pid;

    /**
     * tb_app_registration表id
     */
    public Long appId;

    @ApiModelProperty(value = "实时api主键", required = true)
    public Long apiId;

    /**
     * 应用名称
     */
    public String appName;
    /**
     * 物理表名
     */
    public String tableName;

    /**
     * 物理表描述
     */
    public String tableDes;

    /**
     * 如果是实时物理表，需要提供数据同步地址
     */
    public String syncSrc;

    /**
     * 0是实时物理表，1是非实时物理表
     */
    public Integer isRealtime;

    /**
     * 0: 未发布  1: 发布成功  2: 发布失败
     */
    public Integer publish;
    /**
     * SQL脚本
     */
    public String sqlScript;

    /**
     * sheet页
     */
    public String sheet;

    @ApiModelProperty(value = "发布错误信息", required = true)
    public String publishErrorMsg;

    /**
     * 0: 发布;  1: 保存sql脚本
     */
    public int flag;

    /**
     * 用于拦截sql保存时空字符
     */
    public int sqlFlag;

    /**
     * oracle-cdc名称
     */
    public String pipelineName;

    /**
     * oracle-cdc检查点时间
     */
    public Integer checkPointInterval;

    /**
     * oracle-cdc检查点时间单位
     */
    public String checkPointUnit;

    /**
     * 0:从最开始读 1:从最新的读
     */
    public ScanStartupModeEnum scanStartupMode;

    /**
     * 现有表
     */
    public boolean useExistTable;

    /**
     * 物理表显示名称
     */
    public String displayName;

    /**
     * 同步方式
     */
    public Integer syncMode;

    /**
     * 系统数据源id
     */
    public Integer appDataSourceId;


}
