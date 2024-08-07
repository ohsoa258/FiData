package com.fisk.datamanagement.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-08-01
 * @Description:
 */
@Data
public class CodeCollectionVO {
    private Integer collectionId;
    private String collectionName;
    List<CodeSetVO> codeSetVOList;
}
