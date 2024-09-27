package com.fisk.datamanagement.dto.classification;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.datafactory.dto.customworkflow.WorkflowDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-09-24
 * @Description:
 */
@Data
public class ApitreeListDTO {
    public Integer id;

    public String pid;

    public String appId;

    public String appName;

    public String apiId;

    public String apiName;

    public String attributeId;

    public String attributeName;
    @ApiModelProperty(value = "创建时间(不传值,后台生成)")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;
    public String createUser;
    public LocalDateTime updateTime;
    public String updateuser;

    @TableLogic
    public int delFlag;
}
