package com.fisk.chartvisual.dto.chartvisual;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 报表属性
 *
 * @author gy
 */
@Data
public class ChartPropertyDTO {

    @ApiModelProperty(value = "名字")
    @Length(max = 50)
    public String name;

    @ApiModelProperty(value = "详细说明")
    @Length(max = 200)
    public String details;
    /**
     * @Length(max = 10000)
     */
    @ApiModelProperty(value = "目录")
    public String content;

    @ApiModelProperty(value = "图片")
    public String image;
    @ApiModelProperty(value = "背景图")
    public String backgroundImage;
}
