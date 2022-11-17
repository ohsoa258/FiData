package com.fisk.dataaccess.dto.table;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TableSyncmodeDTO extends BaseDTO {

    /**
     * id
     */
    @ApiModelProperty(value = "同步方式id")
    public long id;

    /**
     * 1：全量、2：时间戳增量、3：业务时间覆盖、4：自定义覆盖；
     */
    @ApiModelProperty(value = "1：全量、2：时间戳增量、3：业务时间覆盖、4：自定义覆盖；", required = true)
    @NotNull
    public int syncMode;

    /**
     * 时间戳字段
     */
    @ApiModelProperty(value = "时间戳字段")
    public String syncField;

    /**
     * 自定义删除条件：定义每次同步的时候删除我们已有的数据条件
     */
    @ApiModelProperty(value = "自定义删除条件：定义每次同步的时候删除我们已有的数据条件")
    public String customDeleteCondition;

    /**
     * 自定义插入条件：定义删除之后获取插入条件的数据进行插入
     */
    @ApiModelProperty(value = "自定义插入条件：定义删除之后获取插入条件的数据进行插入")
    public String customInsertCondition;

    /**
     * timer driver
     */
    @ApiModelProperty(value = "timer driver")
    public String timerDriver;

    /**
     * corn表达式
     */
    @ApiModelProperty(value = "corn表达式")
    public String cornExpression;

    /**
     * 保留历史数据 0 不保留历史版本 1 保留历史版本
     */
    @ApiModelProperty(value = "保留历史数据 0 不保留历史版本 1 保留历史版本")
    public int retainHistoryData;

    /**
     * 保留时间
     */
    @ApiModelProperty(value = "保留时间")
    public  int retainTime;

    /**
     * 保留单位 年/季/月/周/日
     */
    @ApiModelProperty(value = "保留单位 年/季/月/周/日")
    public String retainUnit;

    /**
     * 版本单位 年/季/月/周/日/自定义
     */
    @ApiModelProperty(value = "版本单位 年/季/月/周/日/自定义")
    public String versionUnit;

    /**
     * 单个数据流文件加载最大数据行
     */
    @ApiModelProperty(value = "单个数据流文件加载最大数据行")
    public Integer maxRowsPerFlowFile;

    /**
     * 单词从结果集中提取的最大数据行
     */
    @ApiModelProperty(value = "单词从结果集中提取的最大数据行")
    public Integer fetchSize;

    /**
     * 版本自定义规则
     */
    @ApiModelProperty(value = "版本自定义规则")
    public String versionCustomRule;

    public TableSyncmodeDTO(BaseEntity entity) {
        super(entity);
    }

    public static <T extends BaseEntity> List<TableSyncmodeDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(TableSyncmodeDTO::new).collect(Collectors.toList());
    }

}
