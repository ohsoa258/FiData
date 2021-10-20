package com.fisk.datamodel.dto.dimensionfolder;

import com.fisk.datamodel.dto.dimension.DimensionListDTO;
import lombok.Data;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionFolderDataDTO {

    public long id;
    /**
     * 维度文件夹中文名
     */
    public String dimensionFolderCnName;
    /**
     * 是否共享
     */
    public boolean share;

    public List<DimensionListDTO> dimensionListDTO;
}
