package com.fisk.dataaccess.webservice.service;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author lsj
 * @description webService用来接收第三方获取临时token的实体类，注意实体类里面的属性强制使用private关键字，否则项目无法启动
 * 将实体类和实现类放一起是为了避免webservice的soap调用方式中，被代理的实体类和代码中实际使用的实体类路径不同导致的报错
 * <p>
 * 如果要在Spring Boot中使用@WebService注解，MyClass类的属性应该是私有的（private）。
 * 这是因为@WebService注解会使用Java Bean的属性访问器（getter和setter方法）来暴露和访问属性。如果属性是公共的（public），
 * 则可能会导致在项目启动时WebService报错。
 * @date 2023/10/9
 */
@Data
public class WebServiceUserDTO {
    /**
     * 账号
     */
    @ApiModelProperty(value = "账号", required = true)
    private String useraccount;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码", required = true)
    private String password;

    /**
     * 针对webservice来说，这个注解必须放在get方法上而不能放在属性上，否则项目启动时webservice会报错
     * nillable = false表示不能为空值
     * required = true表示强制需要该属性
     */
    @XmlElement(name = "useraccount", nillable = false,required = true)
    public String getUseraccount() {
        return useraccount;
    }

    public void setUseraccount(String useraccount) {
        this.useraccount = useraccount;
    }

    /**
     * 针对webservice来说，这个注解必须放在get方法上而不能放在属性上，否则项目启动时webservice会报错
     * nillable = false表示不能为空值
     * required = true表示强制需要该属性
     */
    @XmlElement(name = "password", nillable = false,required = true)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}