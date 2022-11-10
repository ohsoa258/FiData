package com.fisk.mdm.vo.masterdata;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BathUploadMemberListVO {

    private String key;

    private List<BathUploadMemberVO> list;

}
