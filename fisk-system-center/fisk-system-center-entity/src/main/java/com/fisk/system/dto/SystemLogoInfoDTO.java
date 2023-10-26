package com.fisk.system.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author SongJianJian
 */
@TableName("tb_system_logo")
@Data
public class SystemLogoInfoDTO {

    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "题目")
    private String title;
    @ApiModelProperty(value = "图标")
    private String logo;
    @ApiModelProperty(value = "颜色")
    private String color;
    @ApiModelProperty(value = "大小")
    private String size;
    @ApiModelProperty(value = "字体家族")
    private String fontFamily;
    @ApiModelProperty(value = "加粗")
    private Boolean overStriking;
    @ApiModelProperty(value = "是否展示logo图片")
    private Integer ifShowLogo;
    @ApiModelProperty(value = "标签页名称")
    private String tabName;
    @ApiModelProperty(value = "标签页logo")
    private String tabLogo;
}
