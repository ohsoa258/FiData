package pd.tangqiao.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: wangjian
 * @Date: 2024-10-29
 * @Description:
 */
@Data
public class TqAppConfigVO {
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "应用名称")
    private String appName;
    @ApiModelProperty(value = "应用描述")
    private String appDesc;
    @ApiModelProperty(value = "应用申请人")
    public String appPrincipal;
    @ApiModelProperty(value = "应用账号")
    public String appAccount;
    @ApiModelProperty(value = "密码/加密")
    public String appPassword;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
