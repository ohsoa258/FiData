package com.fisk.datamanagement.dto.baidu;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author: wangjian
 * @Date: 2024-06-05
 * @Description:
 */
@Data
public class ChatHistory {
    private String role;
    private List<Map<String,String>> history;
    private String input;
}
