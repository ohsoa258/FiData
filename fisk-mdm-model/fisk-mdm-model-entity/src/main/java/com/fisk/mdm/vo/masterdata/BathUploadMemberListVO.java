package com.fisk.mdm.vo.masterdata;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BathUploadMemberListVO {

    public String key;

    public List<BathUploadMemberVO> list;

}
