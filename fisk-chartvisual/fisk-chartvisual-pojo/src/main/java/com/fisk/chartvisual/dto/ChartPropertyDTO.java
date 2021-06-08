package com.fisk.chartvisual.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * 报表属性
 *
 * @author gy
 */
@Data
public class ChartPropertyDTO {
    @Length(max = 50)
    public String name;
    @Length(max = 200)
    public String details;
    @Length(max = 2000)
    public String content;
}
