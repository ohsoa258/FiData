package com.fisk.datamanagement.dto.classification;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
@Data
public class FacttreeListDTOs {
    public Integer id;

    public String pid;

    public String businessNameId;

    public String businessName;

    public String factTabNameId;

    public String factTabName;

    public String factFieldEnNameId;

    public String factFieldEnName;
    @ApiModelProperty(value = "创建时间(不传值,后台生成)")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createdTime;
    public String createdUser;
    public LocalDateTime updateTime;
    public String updateuser;

    @TableLogic
    public int delFlag;
}
