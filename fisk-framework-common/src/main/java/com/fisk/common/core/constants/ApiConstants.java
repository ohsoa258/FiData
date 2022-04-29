package com.fisk.common.core.constants;

/**
 * @author Lock
 * @version 1.3
 * @description API文档常量类
 * @date 2022/2/22 17:12
 */
public class ApiConstants {

    /**
     * api文档基本信息
     */
    public static final String DATAACCESS_APIBASICINFO = "{\n" +
            "    \"title\":\"FiData一体化数据赋能平台 API接口文档\",\n" +
            "    \"docVersion\":\"文档版本 V1.0\",\n" +
            "    \"isuCompany\":\"菲斯科（上海）软件有限公司编制\",\n" +
            "    \"isuDate\":\"发布日期：20220222\",\n" +
            "    \"footerName\":\"FiData接口文档\",\n" +
            "    \"docPurpose\":\"本文由本文由菲斯科（上海）软件有限公司编写，用于第三方平台对接FiData一体化数据赋能平台。\",\n" +
            "    \"readers\":\"预期读者包括需要推送数据到FiData一体化数据赋能平台的上游系统。\",\n" +
            "    \"standard\":\"接口采用HTTP协议，TCP连接方式。数据传输格式采用非加密的JSON格式。API请求方式为POST，文本编码格式为UTF-8；\",\n" +
            "    \"standard_query\":\"暂无。\",\n" +
            "    \"authStandard\":\"第三方系统在访问平台API时需要进行身份验证，通过调用“获取Token”接口，传递账号密码获取Token（60分钟有效期）。\",\n" +
            "    \"uatAddress\":\"测试环境：{api_uat_address}\",\n" +
            "    \"prdAddress\":\"生产环境：{api_prd_address}(预留地址,暂不使用)\",\n" +
            "    \"apiCatalogueDTOS\":[\n" +
            "        {\n" +
            "            \"grade\":1,\n" +
            "            \"catalogueIndex\":\"\",\n" +
            "            \"catalogueName\":\"目录\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"1.\",\n" +
            "            \"catalogueName\":\"文档概述\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"1.1.\",\n" +
            "            \"catalogueName\":\"文档目的\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"1.2.\",\n" +
            "            \"catalogueName\":\"读者对象\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"1.3.\",\n" +
            "            \"catalogueName\":\"相关联系人\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"2.\",\n" +
            "            \"catalogueName\":\"Restful API接口\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"2.1.\",\n" +
            "            \"catalogueName\":\"接口对接规范\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"2.2.\",\n" +
            "            \"catalogueName\":\"登录授权\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"2.3.\",\n" +
            "            \"catalogueName\":\"环境信息\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"2.4.\",\n" +
            "            \"catalogueName\":\"获取Token接口\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"3.\",\n" +
            "            \"catalogueName\":\"API返回代码示例\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"apiBasicInfoDTOS\":[\n" +
            "        {\n" +
            "            \"apiName\":\"获取Token接口\",\n" +
            "            \"apiNameCatalogue\":\"2.4.\",\n" +
            "            \"apiAddress\":\"/dataAccess/apiConfig/getToken\",\n" +
            "            \"apiAddressCatalogue\":\"2.4.1.\",\n" +
            "            \"apiDesc\":\"获取身份凭证，后续请求中将此凭证作为身份标识传给业务接口，业务接口将验证身份凭证是否合法，合法则返回业务数据。\",\n" +
            "            \"apiDescCatalogue\":\"2.4.2.\",\n" +
            "            \"apiRequestType\":\"POST\",\n" +
            "            \"apiRequestTypeCatalogue\":\"2.4.3.\",\n" +
            "            \"apiContentType\":\"application/json\",\n" +
            "            \"apiContentTypeCatalogue\":\"2.4.4\",\n" +
            "            \"apiHeader\":\"无\",\n" +
            "            \"apiHeaderCatalogue\":\"2.4.5.\",\n" +
            "            \"apiRequestExamplesCatalogue\":\"2.4.6.\",\n" +
            "            \"apiRequestDTOS\":[\n" +
            "                {\n" +
            "                    \"parmName\":\"useraccount\",\n" +
            "                    \"isRequired\":\"是\",\n" +
            "                    \"parmType\":\"string\",\n" +
            "                    \"parmDesc\":\"应用账号\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"parmName\":\"password\",\n" +
            "                    \"isRequired\":\"是\",\n" +
            "                    \"parmType\":\"string\",\n" +
            "                    \"parmDesc\":\"应用密码\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"apiRequestCatalogue\":\"2.4.7.\",\n" +
            "            \"apiResponseExamples\":\"\",\n" +
            "            \"apiResponseExamplesCatalogue\":\"2.4.8.\",\n" +
            "            \"apiResponseDTOS\":[\n" +
            "                {\n" +
            "                    \"parmName\":\"code\",\n" +
            "                    \"parmType\":\"int\",\n" +
            "                    \"parmDesc\":\"调用结果状态\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"parmName\":\"msg\",\n" +
            "                    \"parmType\":\"String\",\n" +
            "                    \"parmDesc\":\"调用结果描述\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"parmName\":\"data\",\n" +
            "                    \"parmType\":\"String\",\n" +
            "                    \"parmDesc\":\"api返回结果\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"apiResponseCatalogue\":\"2.4.9.\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"apiContactsDTOS\":[\n" +
            "        {\n" +
            "            \"category\":\"接口负责人\",\n" +
            "            \"company\":\"菲斯科\",\n" +
            "            \"fullName\":\"徐阳辉\",\n" +
            "            \"mailbox\":\"yhxu@fisksoft.com\",\n" +
            "            \"trStyle\":\"background-color: #fff\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"category\":\"接口负责人\",\n" +
            "            \"company\":\"菲斯科\",\n" +
            "            \"fullName\":\"赵飞鸿\",\n" +
            "            \"mailbox\":\"feihongz@fisksoft.com.cn\",\n" +
            "            \"trStyle\":\"background-color: #f8f8f8\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"apiVersionDTOS\":[\n" +
            "        {\n" +
            "            \"version\":\"0.1\",\n" +
            "            \"startDate\":\"2022/02/22\",\n" +
            "            \"endDate\":\"2022/02/22\",\n" +
            "            \"modifier\":\"zhaofeihong\",\n" +
            "            \"explain\":\"文档创建、编写\",\n" +
            "            \"state\":\"初稿\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"apiResponseCodeDTOS\":[\n" +
            "        {\n" +
            "            \"code\":\"0\",\n" +
            "            \"type\":\"int\",\n" +
            "            \"desc\":\"推送数据成功\",\n" +
            "            \"trStyle\":\"background-color: #fff\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"code\":\"500\",\n" +
            "            \"type\":\"int\",\n" +
            "            \"desc\":\"API服务器异常\",\n" +
            "            \"trStyle\":\"background-color: #f8f8f8\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"code\":\"1017\",\n" +
            "            \"type\":\"int\",\n" +
            "            \"desc\":\"获取token失败\",\n" +
            "            \"trStyle\":\"background-color: #fff\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"code\":\"5010\",\n" +
            "            \"type\":\"int\",\n" +
            "            \"desc\":\"API下不存在表\",\n" +
            "            \"trStyle\":\"background-color: #fff\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"code\":\"5023\",\n" +
            "            \"type\":\"int\",\n" +
            "            \"desc\":\"推送数据失败\",\n" +
            "            \"trStyle\":\"background-color: #f8f8f8\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"code\":\"5027\",\n" +
            "            \"type\":\"int\",\n" +
            "            \"desc\":\"API唯一标识未发送,请检查请求参数\",\n" +
            "            \"trStyle\":\"background-color: #f8f8f8\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"code\":\"5028\",\n" +
            "            \"type\":\"int\",\n" +
            "            \"desc\":\"当前API所属应用已删除\",\n" +
            "            \"trStyle\":\"background-color: #f8f8f8\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    public static final String DATASERVICE_APIBASICINFO = "{\n" +
            "    \"title\":\"FiData一体化数据赋能平台 API接口文档\",\n" +
            "    \"docVersion\":\"文档版本 V1.0\",\n" +
            "    \"isuCompany\":\"菲斯科（上海）软件有限公司编制\",\n" +
            "    \"isuDate\":\"发布日期：20220101\",\n" +
            "    \"footerName\":\"FiData接口文档\",\n" +
            "    \"docPurpose\":\"本文由本文由菲斯科（上海）软件有限公司编写，用于下游系统对接白泽接口。\",\n" +
            "    \"readers\":\"预期读者包括需要从白泽获取数据的下游系统。\",\n" +
            "    \"standard\":\"接口采用HTTP协议，TCP连接方式。数据传输格式采用非加密的JSON格式。API请求方式为POST，文本编码格式为UTF-8；\",\n" +
            "    \"standard_query\":\"查询接口携带分页功能，current和size为null默认查询全部。apiCode为私密信息，不在文档中体现，请在订阅API时自行保存。\",\n" +
            "    \"authStandard\":\"第三方系统在访问平台API时需要进行身份验证，通过调用“获取Token”接口，传递账号密码获取Token（60分钟有效期）。\",\n" +
            "    \"uatAddress\":\"测试环境：{api_uat_address}\",\n" +
            "    \"prdAddress\":\"生产环境：{api_prd_address}\",\n" +
            "    \"apiCatalogueDTOS\":[\n" +
            "        {\n" +
            "            \"grade\":1,\n" +
            "            \"catalogueIndex\":\"\",\n" +
            "            \"catalogueName\":\"目录\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"1.\",\n" +
            "            \"catalogueName\":\"文档概述\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"1.1.\",\n" +
            "            \"catalogueName\":\"文档目的\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"1.2.\",\n" +
            "            \"catalogueName\":\"读者对象\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"1.3.\",\n" +
            "            \"catalogueName\":\"相关联系人\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"2.\",\n" +
            "            \"catalogueName\":\"Restful API接口\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"2.1.\",\n" +
            "            \"catalogueName\":\"接口对接规范\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"2.2.\",\n" +
            "            \"catalogueName\":\"登录授权\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"2.3.\",\n" +
            "            \"catalogueName\":\"环境信息\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"2.4.\",\n" +
            "            \"catalogueName\":\"获取Token接口\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"3.\",\n" +
            "            \"catalogueName\":\"API返回代码示例\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"apiBasicInfoDTOS\":[\n" +
            "        {\n" +
            "            \"apiName\":\"获取Token接口\",\n" +
            "            \"apiNameCatalogue\":\"2.4.\",\n" +
            "            \"apiAddress\":\"/dataservice/apiService/getToken\",\n" +
            "            \"apiAddressCatalogue\":\"2.4.1.\",\n" +
            "            \"apiDesc\":\"获取身份凭证，后续请求中将此凭证作为身份标识传给业务接口，业务接口将验证身份凭证是否合法，合法则返回业务数据。\",\n" +
            "            \"apiDescCatalogue\":\"2.4.2.\",\n" +
            "            \"apiRequestType\":\"POST\",\n" +
            "            \"apiRequestTypeCatalogue\":\"2.4.3.\",\n" +
            "            \"apiContentType\":\"application/json\",\n" +
            "            \"apiContentTypeCatalogue\":\"2.4.4\",\n" +
            "            \"apiHeader\":\"无\",\n" +
            "            \"apiHeaderCatalogue\":\"2.4.5.\",\n" +
            "            \"apiRequestExamplesCatalogue\":\"2.4.6.\",\n" +
            "            \"apiRequestDTOS\":[\n" +
            "                {\n" +
            "                    \"parmName\":\"appAccount\",\n" +
            "                    \"isRequired\":\"是\",\n" +
            "                    \"parmType\":\"string\",\n" +
            "                    \"parmDesc\":\"应用账号\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"parmName\":\"appPassword\",\n" +
            "                    \"isRequired\":\"是\",\n" +
            "                    \"parmType\":\"string\",\n" +
            "                    \"parmDesc\":\"应用密码\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"apiRequestCatalogue\":\"2.4.7.\",\n" +
            "            \"apiResponseExamples\":\"\",\n" +
            "            \"apiResponseExamplesCatalogue\":\"2.4.8.\",\n" +
            "            \"apiResponseDTOS\":[\n" +
            "                {\n" +
            "                    \"parmName\":\"msg\",\n" +
            "                    \"parmType\":\"string\",\n" +
            "                    \"parmDesc\":\"调用结果描述\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"parmName\":\"code\",\n" +
            "                    \"parmType\":\"string\",\n" +
            "                    \"parmDesc\":\"调用结果状态\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"parmName\":\"data\",\n" +
            "                    \"parmType\":\"string\",\n" +
            "                    \"parmDesc\":\"token（调用成功后返回）\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"apiResponseCatalogue\":\"2.4.9.\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"apiContactsDTOS\":[\n" +
            "        {\n" +
            "            \"category\":\"接口负责人\",\n" +
            "            \"company\":\"菲斯科\",\n" +
            "            \"fullName\":\"徐阳辉\",\n" +
            "            \"mailbox\":\"yhxu@fisksoft.com\",\n" +
            "            \"trStyle\":\"background-color: #fff\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"category\":\"接口负责人\",\n" +
            "            \"company\":\"菲斯科\",\n" +
            "            \"fullName\":\"李家温\",\n" +
            "            \"mailbox\":\"dick@fisksoft.com\",\n" +
            "            \"trStyle\":\"background-color: #f8f8f8\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"apiVersionDTOS\":[\n" +
            "        {\n" +
            "            \"version\":\"0.1\",\n" +
            "            \"startDate\":\"2022/01/01\",\n" +
            "            \"endDate\":\"2022/01/01\",\n" +
            "            \"modifier\":\"lijiawen\",\n" +
            "            \"explain\":\"文档创建、编写\",\n" +
            "            \"state\":\"初稿\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"apiResponseCodeDTOS\":[\n" +
            "        {\n" +
            "            \"code\":\"200\",\n" +
            "            \"type\":\"int\",\n" +
            "            \"desc\":\"调用结果描述\",\n" +
            "            \"trStyle\":\"background-color: #fff\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"code\":\"401\",\n" +
            "            \"type\":\"int\",\n" +
            "            \"desc\":\"无权限访问此API\",\n" +
            "            \"trStyle\":\"background-color: #f8f8f8\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"code\":\"404\",\n" +
            "            \"type\":\"int\",\n" +
            "            \"desc\":\"API不存在或被取消订阅\",\n" +
            "            \"trStyle\":\"background-color: #fff\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"code\":\"500\",\n" +
            "            \"type\":\"int\",\n" +
            "            \"desc\":\"API服务器异常\",\n" +
            "            \"trStyle\":\"background-color: #f8f8f8\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

}
