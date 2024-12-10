package com.fisk.dataaccess.dto.table;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BaseEntity;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
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
 * 表名及表对应字段
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TablePyhNameDTO extends BaseDTO {

    /**
     * 表名
     */
    @ApiModelProperty(value = "非实时应用所属下的表名", required = true)
    public String tableName;

    /**
     * sapbw cube名称
     */
    @ApiModelProperty(value = "sapbw cube名称", required = true)
    public String cubeName;

    //暂时用不到
//    /**
//     * sapbw cat名称
//     */
//    @ApiModelProperty(value = "sapbw cat名称", required = true)
//    public String catName;

    /**
     * cube的描述 DSCRPTN
     */
    @ApiModelProperty(value = "cube数据最近的修改人")
    public String cubeDscrptn;

    /**
     * 表字段
     */
    @ApiModelProperty(value = "表字段")
    public List<TableStructureDTO> fields;

    /**
     * powerbi 数据集各种item 的id
     */
    @ApiModelProperty(value = "powerbi 数据集各种item 的id")
    public String guid;

    /**
     * item type
     */
    @ApiModelProperty(value = "item type")
    public String type;

    public TablePyhNameDTO(BaseEntity entity) {
        super(entity);
    }

    public static <T extends BaseEntity> List<TablePyhNameDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(TablePyhNameDTO::new).collect(Collectors.toList());
    }

}
