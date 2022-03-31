package com.fisk.datagovernance.dto.datasecurity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 数据脱敏字段配置表
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Data
public class RowSecurityConfigDTO {

    @ApiModelProperty(value = "主键(修改时必传)", required = true)
    public long id;

    @ApiModelProperty(value = "缺省设置(0: 空  1: 所有可读  2: 所有不可读--此时无论权限如何,当前表不可读取),默认值: 1", required = true)
    public long defaultConfig;

    @ApiModelProperty(value = "数据源id", required = true)
    public long datasourceId;

    @ApiModelProperty(value = "表id", required = true)
    public long tableId;

    @ApiModelProperty(value = "权限名称", required = true)
    public String permissionsName;

    @ApiModelProperty(value = "权限描述", required = true)
    public String permissionsDes;

    @ApiModelProperty(value = "是否有效", required = true)
    public Boolean valid;

    /**
     * 过滤条件对象集合
     */
    public List<RowfilterConfigDTO> filterConditionDtoList;

    /**
     * 权限表格对象集合
     */
    public List<RowUserAssignmentDTO> rowUserAssignmentDTOList;
}
