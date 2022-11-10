package com.fisk.dataaccess.dto.table;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BaseEntity;
import com.fisk.dataaccess.dto.oraclecdc.CdcJobScriptDTO;
import com.fisk.dataaccess.enums.ScanStartupModeEnum;
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
 * <p>
 * 实时对象
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TableAccessDTO extends BaseDTO {

    @ApiModelProperty(value = "主键")
    public long id;

    /**
     * tb_app_registration表id
     */
    @ApiModelProperty(value = "应用id", required = true)
    public long appId;

    @ApiModelProperty(value = "实时apiId", required = true)
    public Long apiId;

    @ApiModelProperty(value = "父id", required = true)
    public int pid;

    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称", required = true)
    public String appName;
    /**
     * 物理表名
     */
    @ApiModelProperty(value = "物理表名", required = true)
    public String tableName;

    /**
     * 物理表描述
     */
    @ApiModelProperty(value = "物理表描述", required = true)
    public String tableDes;

    /**
     * 如果是实时物理表，需要提供数据同步地址
     */
    @ApiModelProperty(value = "实时物理表，需要提供数据同步地址")
    public String syncSrc;

    /**
     * 0是实时物理表，1是非实时物理表
     */
    @ApiModelProperty(value = "0是实时物理表，1是非实时物理表", required = true)
    public int isRealtime;

    /**
     * 0: 未发布  1: 发布成功  2: 发布失败
     */
    @ApiModelProperty(value = "0: 未发布  1: 发布成功  2: 发布失败", required = true)
    public Integer publish;
    /**
     * SQL脚本or文件全限定名称
     */
    @ApiModelProperty(value = "SQL脚本or文件全限定名称", required = true)
    public String sqlScript;

    /**
     * excel sheet页名称
     */
    @ApiModelProperty(value = "excel sheet页名称", required = true)
    public String sheet;

    @ApiModelProperty(value = "发布错误信息", required = true)
    public String publishErrorMsg;

    /**
     * oracle-cdc任务脚本
     */
    public CdcJobScriptDTO cdcJobScript;

    /**
     * oracle-cdc管道名称
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
    public ScanStartupModeEnum scanStartupMode = ScanStartupModeEnum.STARTING_POSITION;

    /**
     * 是否使用已存在表
     */
    public boolean useExistTable = false;

    @ApiModelProperty(value = "stg数据保留天数", required = true)
    public String keepNumber;

    /**
     * 表字段对象
     */
    public List<TableFieldsDTO> list;

    /**
     * 同步频率
     */
    public TableSyncmodeDTO tableSyncmodeDTO;

    public TableAccessDTO(BaseEntity entity) {
        super(entity);
    }

    /**
     * 将PO集合转为DTO对象
     *
     * @param list PO对象集合
     * @param <T>  PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<TableAccessDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(TableAccessDTO::new).collect(Collectors.toList());
    }
}
