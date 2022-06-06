package com.fisk.mdm.vo.entity;

import com.fisk.mdm.vo.resultObject.DropDownBaseVO;
import com.fisk.mdm.vo.viwGroup.ViewGroupDropDownVO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class EntityDropDownVO extends DropDownBaseVO {

    private List<ViewGroupDropDownVO> viewGroups;

}
