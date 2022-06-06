package com.fisk.mdm.vo.model;

import com.fisk.mdm.vo.entity.EntityDropDownVO;
import com.fisk.mdm.vo.modelVersion.ModelVersionDropDownVO;
import com.fisk.mdm.vo.resultObject.DropDownBaseVO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ModelDropDownVO extends DropDownBaseVO {

    private String icon;

    private List<EntityDropDownVO> entity;

    private List<ModelVersionDropDownVO> versions;

}
