package com.fisk.datamanagement.dto.classification;

import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

public class FacttreeListDTOs {
    public String pid;

    public String businessNameId;

    public String businessName;

    public String factTabNameId;

    public String factTabName;

    public String factFieldEnNameId;

    public String factFieldEnName;
    @ApiModelProperty(value = "创建时间(不传值,后台生成)")
    public LocalDateTime createdTime;
    public String createdUser;
    public LocalDateTime updateTime;
    public String updateuser;

    @TableLogic
    public int delFlag;


}
