package com.fisk.datamanagement.dto.classification;

import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author xgf
 * @date 2023年12月25日 20:08
 */
@Data
public class BusinessExtendedfieldsDTO {
    public String  dimdomaintype;
    public String  dimdomainid;
    public String  dimdomain;
    public String  dimtableid;
    public String  dimtable;
    public String  attributeid;
    public String  attribute;
    @ApiModelProperty(value = "创建时间(不传值,后台生成)")
    public LocalDateTime createdTime;
    public  String indexid;
    public String createdUser;

    @TableLogic
    public int delFlag;


}
