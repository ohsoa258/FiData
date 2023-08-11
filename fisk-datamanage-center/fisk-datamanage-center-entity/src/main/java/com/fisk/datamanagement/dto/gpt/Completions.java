package com.fisk.datamanagement.dto.gpt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-08-10
 * @Description:
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Completions {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    // getter and setter methods

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private String text;
        private int index;
        private String finish_reason;
        private Object logprobs;

        // getter and setter methods
    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        private int completion_tokens;
        private int prompt_tokens;
        private int total_tokens;

        // getter and setter methods
    }
}
