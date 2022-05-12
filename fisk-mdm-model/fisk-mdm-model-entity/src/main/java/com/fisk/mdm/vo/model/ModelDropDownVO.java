package com.fisk.mdm.vo.model;

import com.fisk.mdm.vo.entity.EntityDropDownVO;
import com.fisk.mdm.vo.modelVersion.ModelVersionDropDownVO;
import com.fisk.mdm.vo.resultObject.DropDownBaseVO;
import lombok.Data;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ModelDropDownVO extends DropDownBaseVO {

    public String icon;

    public List<EntityDropDownVO> children;

    public List<ModelVersionDropDownVO> versions;

}
