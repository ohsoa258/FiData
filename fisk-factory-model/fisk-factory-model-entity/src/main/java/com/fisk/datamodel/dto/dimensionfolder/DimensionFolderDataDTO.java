package com.fisk.datamodel.dto.dimensionfolder;

import com.fisk.datamodel.dto.dimension.DimensionListDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionFolderDataDTO {

    @ApiModelProperty(value = "id")
    public long id;
    /**
     * 维度文件夹中文名
     */
    @ApiModelProperty(value = "维度文件夹中文名")
    public String dimensionFolderCnName;
    /**
     * 是否共享
     */
    @ApiModelProperty(value = "是否共享")
    public boolean share;
    /**
     * 发布状态：1:未发布、2：发布成功、3：发布失败
     */
    //public int isPublish;
    @ApiModelProperty(value = "发布状态：1:未发布、2：发布成功、3：发布失败")
    public List<DimensionListDTO> dimensionListDTO;
}
