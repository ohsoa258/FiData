package com.fisk.dataaccess.dto.table;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BaseEntity;
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
 * 非实时对象
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TableAccessNonDTO extends BaseDTO {

    @ApiModelProperty(value = "物理表id")
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

    @ApiModelProperty(value = "物理表显示名称", required = true)
    public String displayName;

    /**
     * 物理表描述
     */
    @ApiModelProperty(value = "物理表描述", required = true)
    public String tableDes;

    /**
     * 如果是实时物理表，需要提供数据同步地址
     */
    @ApiModelProperty(value = "如果是实时物理表，需要提供数据同步地址")
    public String syncSrc;

    /**
     * 0是实时物理表，1是非实时物理表
     */
    @ApiModelProperty(value = "0是实时物理表，1是非实时物理表", required = true)
    public int isRealtime;

    /**
     * 0: 未发布  1: 发布成功  2: 发布失败
     */
    @ApiModelProperty(value = "0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布", required = true)
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

    @ApiModelProperty(value = "stg数据保留天数", required = true)
    public Integer keepNumber;

    /**
     * 表字段对象
     */
    public List<TableFieldsDTO> list;

    /**
     * 业务时间对象
     */
    public TableBusinessDTO businessDTO;

    /**
     * 同步方式对象
     */
    public TableSyncmodeDTO tableSyncmodeDTO;

    /**
     * 0: 保存;   1: 保存&发布
     */
    public int flag;

    @ApiModelProperty(value = "发布时,是否立即同步数据", required = true)
    public boolean openTransmission;

    public TableAccessNonDTO(BaseEntity entity) {
        super(entity);
    }

    /**
     * 将PO集合转为DTO对象
     *
     * @param list PO对象集合
     * @param <T>  PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<TableAccessNonDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(TableAccessNonDTO::new).collect(Collectors.toList());
    }
}
