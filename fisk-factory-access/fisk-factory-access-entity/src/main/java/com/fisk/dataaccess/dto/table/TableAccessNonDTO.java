package com.fisk.dataaccess.dto.table;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BaseEntity;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.dataaccess.dto.api.ApiParameterDTO;
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

    /**
     * excel sheet 开始读取数据行数
     */
    @ApiModelProperty(value = "excel sheet开始读数据行数", required = true)
    public Integer startLine;

    @ApiModelProperty(value = "发布错误信息", required = true)
    public String publishErrorMsg;

    @ApiModelProperty(value = "stg数据保留天数", required = true)
    public String keepNumber;

    @ApiModelProperty(value = "应用数据源id", required = true)
    public Integer appDataSourceId;

    /**
     * 表字段对象
     */
    @ApiModelProperty(value = "表字段对象")
    public List<TableFieldsDTO> list;

    @ApiModelProperty(value = "源字段列表")
    public List<ApiParameterDTO> fieldList;

    /**
     * 业务时间对象
     */
    @ApiModelProperty(value = "业务时间对象")
    public TableBusinessDTO businessDTO;

    /**
     * 同步方式对象
     */
    @ApiModelProperty(value = "同步方式对象")
    public TableSyncmodeDTO tableSyncmodeDTO;

    /*
     * 接入的增量时间参数
     */
    @ApiModelProperty(value = "接入的增量时间参数")
    public List<DeltaTimeDTO> deltaTimes;

    /**
     * 0: 保存;   1: 保存&发布
     */
    @ApiModelProperty(value = "0: 保存;   1: 保存&发布")
    public int flag;

    @ApiModelProperty(value = "发布时,是否立即同步数据", required = true)
    public boolean openTransmission;

    @ApiModelProperty(value = "业务时间覆盖，需要传递拼接的sql条件", required = false)
    public String whereScript;

    @ApiModelProperty(value = "表历史")
    public List<TableHistoryDTO> tableHistorys;

    /**
     * 覆盖脚本
     */
    @ApiModelProperty(value = "覆盖脚本")
    public String coverScript;

    /**
     * sapbw-mdx语句集合
     */
    @ApiModelProperty(value = "sapbw-mdx语句集合", required = true)
    public List<String> mdxList;

    /**
     * powerbi 数据集id
     */
    @ApiModelProperty(value = "powerbi 数据集id")
    public String pbiDatasetId;

    /**
     * pbi 查询时所用的用户名
     */
    @ApiModelProperty(value = "pbi 查询时所用的用户名")
    public String pbiUsername;

    /**
     * mongo查询bson字符串
     * 举例:{"username": "Tom"}
     */
    @ApiModelProperty(value = "mongo查询bson字符串")
    public String mongoQueryCondition;

    /**
     * mongo指定返回字段
     * 举例:{"_id": 1, "username": 1, "product": 1, "price": 1, "type": 1}
     */
    @ApiModelProperty(value = "mongo指定返回字段")
    public String mongoNeededFileds;

    /**
     * 对应的mongodb集合名称
     */
    @ApiModelProperty(value = "对应的mongodb集合名称")
    public String mongoCollectionName;

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
