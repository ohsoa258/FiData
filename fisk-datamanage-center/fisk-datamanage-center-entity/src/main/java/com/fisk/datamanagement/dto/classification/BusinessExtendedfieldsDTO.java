package com.fisk.datamanagement.dto.classification;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author xgf
 * @date 2023年12月25日 20:08
 */
@Data
public class BusinessExtendedfieldsDTO {
    public Integer id;
    public String  dimdomaintype;
    public String  dimdomainid;
    public String  dimdomain;
    public String  dimtableid;
    public String  dimtable;
    public String  attributeid;
    public String  attribute;
    public String attributeEnName;
    @ApiModelProperty(value = "创建时间(不传值,后台生成)")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createdTime;
    public String indexid;
    public String createdUser;

    @TableLogic
    public int delFlag;


}
