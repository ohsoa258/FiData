package com.fisk.datamanagement.dto.email;

import lombok.Data;

import java.util.List;

@Data
public class EmailGroupUserMapAddDTO {

    /**
     * 组id
     */
    private Integer groupId;

    private List<Integer> userIds;

}
