package com.fisk.chartvisual.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

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
    @Length(max = 10000)
    public String content;
    public String image;
}
