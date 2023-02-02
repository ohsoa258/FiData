package com.fisk.system.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * @author SongJianJian
 */
@TableName("tb_system_logo")
@Data
public class SystemLogoInfoDTO {

    private Integer id;

    private String title;

    private String logo;

    private String color;

    private String size;

    private String fontFamily;

    private Boolean overStriking;
}
