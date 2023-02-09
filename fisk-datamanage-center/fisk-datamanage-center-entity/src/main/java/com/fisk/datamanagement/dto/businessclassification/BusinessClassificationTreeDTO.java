package com.fisk.datamanagement.dto.businessclassification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Tloml
 * @Date: 2023
 * @Copyright: 2023 by 湖~Tloml
 * @Description:
 **/
@Data
public class BusinessClassificationTreeDTO {

    public String id;

    public String pid;

    public String name;

    public String description;

    @JsonIgnore
    public LocalDateTime createTime;

    public List<BusinessClassificationTreeDTO> child;

}
