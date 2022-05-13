package com.fisk.mdm.vo.masterdata;

import com.fisk.mdm.vo.model.ModelDropDownVO;
import com.fisk.mdm.vo.modelVersion.ModelVersionDropDownVO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ModelEntityVersionStructVO {

    public ModelDropDownVO model;

    public List<ModelVersionDropDownVO> modelVersions;

}
