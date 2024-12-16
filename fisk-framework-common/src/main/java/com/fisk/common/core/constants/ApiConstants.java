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
            "    \"title\":\"一体化数据赋能平台 API接口文档\",\n" +
            "    \"docVersion\":\"文档版本 V1.0\",\n" +
            "    \"isuCompany\":\"\",\n" +
            "    \"isuDate\":\"发布日期：20220222\",\n" +
            "    \"footerName\":\"接口文档\",\n" +
            "    \"docPurpose\":\"本文用于第三方平台对接一体化数据赋能平台。\",\n" +
            "    \"readers\":\"预期读者包括需要推送数据到一体化数据赋能平台的第三方平台。\",\n" +
            "    \"standard\":\"接口采用HTTP协议，TCP连接方式。数据传输格式: 1、若不开启加密，则采用非加密的JSON格式。 2、若开启加密，则采用AES128加密数据。 API请求方式为POST，文本编码格式为UTF-8；\",\n" +
            "    \"standard_query\":\"暂无。\",\n" +
            "    \"authStandard\":\"第三方系统在访问平台API时需要进行身份验证，通过调用“获取Token”接口，传递账号密码获取Token（60分钟有效期）。\",\n" +
            "    \"uatAddress\":\"{api_uat_address}/{apiaddress}\",\n" +
//            "    \"prdAddress\":\"生产环境：{api_prd_address}(预留地址,暂不使用)\",\n" +
            "    \"apiCatalogueDTOS\":[\n" +
            "        {\n" +
            "            \"grade\":1,\n" +
            "            \"catalogueIndex\":\"\",\n" +
            "            \"catalogueName\":\"目录\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"一.\",\n" +
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
//            "        {\n" +
//            "            \"grade\":3,\n" +
//            "            \"catalogueIndex\":\"1.3.\",\n" +
//            "            \"catalogueName\":\"相关联系人\"\n" +
//            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"二.\",\n" +
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
            "            \"catalogueName\":\"接口地址\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"2.4.\",\n" +
            "            \"catalogueName\":\"获取Token接口\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"三.\",\n" +
            "            \"catalogueName\":\"C#代码调用示例\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"四.\",\n" +
            "            \"catalogueName\":\"java代码调用示例\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"五.\",\n" +
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
//            "    \"apiContactsDTOS\":[\n" +
//            "        {\n" +
//            "            \"category\":\"接口负责人\",\n" +
//            "            \"company\":\"技术部\",\n" +
//            "            \"fullName\":\"徐阳辉\",\n" +
//            "            \"mailbox\":\"yhxu@fisksoft.com\",\n" +
//            "            \"trStyle\":\"background-color: #fff\"\n" +
//            "        },\n" +
//            "        {\n" +
//            "            \"category\":\"接口负责人\",\n" +
//            "            \"company\":\"技术部\",\n" +
//            "            \"fullName\":\"赵飞鸿\",\n" +
//            "            \"mailbox\":\"feihongz@fisksoft.com.cn\",\n" +
//            "            \"trStyle\":\"background-color: #f8f8f8\"\n" +
//            "        }\n" +
//            "    ],\n" +
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
            "    \"title\":\"强生交通智慧中台 API接口文档\",\n" +
            "    \"docVersion\":\"文档版本 V1.0\",\n" +
            "    \"isuCompany\":\"\",\n" +
            "    \"isuDate\":\"发布日期：{release_Date}\",\n" +
            "    \"footerName\":\"接口文档\",\n" +
//            "    \"docPurpose\":\"向需要从强生交通智慧中台获取数据的下游提供清晰的调用指南，包括认证授权、终端点请求、响应错误处理等。\",\n" +
//            "    \"readers\":\"预期读者包括需要从强生交通智慧中台中获取数据的下游。\",\n" +
            "    \"docPurpose\":\"本文用于第三方平台对接一体化数据赋能平台。\",\n" +
            "    \"readers\":\"预期读者包括需要从一体化数据赋能平台中获取数据的第三方平台。\",\n" +
            "    \"standard\":\"接口采用HTTP协议，TCP连接方式。数据传输格式采用非加密的JSON格式。API请求方式为POST，文本编码格式为UTF-8；\",\n" +
            "    \"standard_query\":\"查询接口携带分页功能，current页码从第1页开始，size每页数量建议不超过500条，current和size为null默认查询1000条。\",\n" +
            "    \"authStandard\":\"第三方系统在访问平台API时需要进行身份验证，通过调用“获取Token”接口，传递账号密码获取Token（60分钟有效期）。\",\n" +
//            "    \"uatAddress\":\"{api_uat_address}/{apiaddress}\",\n" +
            "    \"prdAddress\":\"{api_prd_address}/{apiaddress}\",\n" +
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
//            "        {\n" +
//            "            \"grade\":3,\n" +
//            "            \"catalogueIndex\":\"1.3.\",\n" +
//            "            \"catalogueName\":\"相关联系人\"\n" +
//            "        },\n" +
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
            "            \"catalogueName\":\"接口地址\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"2.4.\",\n" +
            "            \"catalogueName\":\"获取Token接口\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"2.5.\",\n" +
            "            \"catalogueName\":\"获取密钥接口\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"3.\",\n" +
            "            \"catalogueName\":\"C#代码调用示例\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"4.\",\n" +
            "            \"catalogueName\":\"java代码调用示例\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"5.\",\n" +
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
            "            \"apiResponseHeaderDesc\":\"返回参数说明\",\n" +
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
            "        },\n" +
            "        {\n" +
            "            \"apiName\":\"获取密钥接口\",\n" +
            "            \"apiNameCatalogue\":\"2.5.\",\n" +
            "            \"apiAddress\":\"/dataservice/apiService/getEncryptKey\",\n" +
            "            \"apiAddressCatalogue\":\"2.5.1.\",\n" +
            "            \"apiDesc\":\"获取密钥值，后续如果字段需要解密则使用该密钥通过AES算法进行解密。\",\n" +
            "            \"apiDescCatalogue\":\"2.5.2.\",\n" +
            "            \"apiRequestType\":\"POST\",\n" +
            "            \"apiRequestTypeCatalogue\":\"2.5.3.\",\n" +
            "            \"apiContentType\":\"application/json\",\n" +
            "            \"apiContentTypeCatalogue\":\"2.5.5\",\n" +
            "            \"apiHeader\":\"Authorization: Bearer {token}\",\n" +
            "            \"apiHeaderCatalogue\":\"2.5.5.\",\n" +
            "            \"apiRequestExamplesCatalogue\":\"2.5.6.\",\n" +
            "            \"apiRequestDTOS\":[\n" +
            "                {\n" +
            "                    \"parmName\":\"apiCode\",\n" +
            "                    \"isRequired\":\"是\",\n" +
            "                    \"parmType\":\"string\",\n" +
            "                    \"parmDesc\":\"API标识\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"apiRequestCatalogue\":\"2.5.7.\",\n" +
            "            \"apiResponseExamples\":\"\",\n" +
            "            \"apiResponseExamplesCatalogue\":\"2.5.8.\",\n" +
            "            \"apiResponseHeaderDesc\":\"返回参数说明\",\n" +
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
            "                    \"parmDesc\":\"密钥值（调用成功后返回）\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"apiResponseCatalogue\":\"2.5.9.\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"apiContactsDTOS\":[\n" +
            "        {\n" +
            "            \"category\":\"接口负责人\",\n" +
            "            \"company\":\"技术部\",\n" +
            "            \"fullName\":\"徐阳辉\",\n" +
            "            \"mailbox\":\"yhxu@fisksoft.com\",\n" +
            "            \"trStyle\":\"background-color: #fff\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"category\":\"接口负责人\",\n" +
            "            \"company\":\"技术部\",\n" +
            "            \"fullName\":\"李家温\",\n" +
            "            \"mailbox\":\"dick@fisksoft.com\",\n" +
            "            \"trStyle\":\"background-color: #f8f8f8\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"apiVersionDTOS\":[\n" +
            "        {\n" +
            "            \"version\":\"0.1\",\n" +
            "            \"startDate\":\"{apiVersion_StartDate}\",\n" +
            "            \"endDate\":\"{apiVersion_EndDate}\",\n" +
            "            \"modifier\":\"{apiVersion_Modified}\",\n" +
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
    public static final String DATASERVICE_APICODEEXAMPLES_NET = "\n" +
            "using Newtonsoft.Json;\n" +
            "using Newtonsoft.Json.Serialization;\n" +
            "using System;\n" +
            "using System.Collections.Generic;\n" +
            "using System.IO;\n" +
            "using System.Net;\n" +
            "using System.Text;\n" +
            "\n" +
            "namespace ConsoleApp1\n" +
            "{\n" +
            "    public class Program\n" +
            "    {\n" +
            "        public static void Main(string[] args)\n" +
            "        {\n" +
            "            #region getToken\n" +
            "            string url = \"{api_prd_address}/dataservice/apiService/getToken\";\n" +
            "            // set request parameters\n" +
            "            GetTokenRequest getTokenRequest = new GetTokenRequest\n" +
            "            {\n" +
            "                appAccount = \"test0233\",\n" +
            "                appPassword = \"test0233\"\n" +
            "            };\n" +
            "            string getTokenParams = JsonConvert.SerializeObject(getTokenRequest);\n" +
            "            // send request\n" +
            "            string token = sendPostWebRequest<GetTokenResponse>(url, getTokenParams)?.data;\n" +
            "            #endregion\n" +
            "\n" +
            "            #region getData\n" +
            "            url = \"{api_prd_address}/dataservice/apiService/getData\";\n" +
            "            // set request parameters\n" +
            "            GetDataRequest getDataRequest = new GetDataRequest()\n" +
            "            {\n" +
            "                apiCode = \"c5bbe530bc0b47e6bfbd256364270365\",\n" +
            "            };\n" +
            "            Dictionary<string, object> parmList = new Dictionary<string, object>();\n" +
            "            parmList.Add(\"product_colour\", \"blue\");\n" +
            "            parmList.Add(\"product_price\", \"7000\");\n" +
            "            getDataRequest.parmList = parmList;\n" +
            "            getDataRequest.current = 1;\n" +
            "            getDataRequest.size = 500;\n" +
            "            string getDataParams = JsonConvert.SerializeObject(getDataRequest);\n" +
            "            // send request\n" +
            "            GetDataResponse data = sendPostWebRequest<GetDataResponse>\n" +
            "                (url, getDataParams, token);\n" +
            "            #endregion\n" +
            "        }\n" +
            "\n" +
            "        public static T sendPostWebRequest<T>(string url, string parameters,\n" +
            "            string token = null)\n" +
            "        {\n" +
            "            T result;\n" +
            "            HttpWebResponse httpResponse = null;\n" +
            "            HttpWebRequest httpRequest = null;\n" +
            "            try\n" +
            "            {\n" +
            "                if (url.ToLower().Contains(\"https://\"))\n" +
            "                {\n" +
            "                    ServicePointManager.SecurityProtocol = SecurityProtocolType.Tls12;\n" +
            "                    httpRequest = (HttpWebRequest)WebRequest.CreateDefault(new Uri(url));\n" +
            "                }\n" +
            "                else\n" +
            "                {\n" +
            "                    httpRequest = (HttpWebRequest)WebRequest.Create(url);\n" +
            "                }\n" +
            "                httpRequest.Method = \"POST\";\n" +
            "                httpRequest.ContentType = \"application/json\";\n" +
            "                if (!string.IsNullOrEmpty(token))\n" +
            "                    httpRequest.Headers.Add(\"Authorization\", token);\n" +
            "                httpRequest.ServicePoint.Expect100Continue = false;\n" +
            "                using (StreamWriter dataStream = \n" +
            "                                     new StreamWriter(httpRequest.GetRequestStream()))\n" +
            "                {\n" +
            "                    dataStream.Write(parameters);\n" +
            "                    dataStream.Close();\n" +
            "                }\n" +
            "\n" +
            "                httpResponse = (HttpWebResponse)httpRequest.GetResponse();\n" +
            "\n" +
            "                var responseStream = httpResponse.GetResponseStream();\n" +
            "                var responseResult = new StreamReader(responseStream,\n" +
            "                    Encoding.GetEncoding(\"utf-8\")).ReadToEnd();\n" +
            "                result = JsonConvert.DeserializeObject<T>(responseResult,\n" +
            "                            new JsonSerializerSettings\n" +
            "                            {\n" +
            "                                ContractResolver = new DefaultContractResolver { }\n" +
            "                            });\n" +
            "            }\n" +
            "            catch (Exception ex)\n" +
            "            {\n" +
            "                throw ex;\n" +
            "            }\n" +
            "            finally\n" +
            "            {\n" +
            "                try\n" +
            "                {\n" +
            "                    if (httpResponse != null)\n" +
            "                    {\n" +
            "                        httpResponse.Close();\n" +
            "                    }\n" +
            "                }\n" +
            "                catch (IOException e)\n" +
            "                {\n" +
            "                    throw e;\n" +
            "                }\n" +
            "            }\n" +
            "            return result;\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    public class GetTokenRequest\n" +
            "    {\n" +
            "        public string appAccount { get; set; }\n" +
            "\n" +
            "        public string appPassword { get; set; }\n" +
            "    }\n" +
            "\n" +
            "    public class GetTokenResponse\n" +
            "    {\n" +
            "        public int code { get; set; }\n" +
            "\n" +
            "        public string msg { get; set; }\n" +
            "\n" +
            "        public string data { get; set; }\n" +
            "    }\n" +
            "\n" +
            "    public class GetDataRequest\n" +
            "    {\n" +
            "        public string apiCode;\n" +
            "\n" +
            "        public Dictionary<string, object> parmList;\n" +
            "\n" +
            "        public int? current;\n" +
            "\n" +
            "        public int? size;\n" +
            "    }\n" +
            "\n" +
            "    public class GetDataResponse\n" +
            "    {\n" +
            "        public int code { get; set; }\n" +
            "\n" +
            "        public string msg { get; set; }\n" +
            "\n" +
            "        public Data data { get; set; }\n" +
            "    }\n" +
            "\n" +
            "    public class Data\n" +
            "    {\n" +
            "        public int? current { get; set; }\n" +
            "\n" +
            "        public int? size { get; set; }\n" +
            "\n" +
            "        public int? total { get; set; }\n" +
            "\n" +
            "        public int? page { get; set; }\n" +
            "\n" +
            "        public List<DataArrayItem> dataArray { get; set; }\n" +
            "    }\n" +
            "\n" +
            "    public class DataArrayItem\n" +
            "    {\n" +
            "        public string product_name { get; set; }\n" +
            "\n" +
            "        public string product_price { get; set; }\n" +
            "    }\n" +
            "}\n";
    public static final String DATASERVICE_APICODEEXAMPLES_NET_ENCRYPT = "using System;\n" +
            "using System.Security.Cryptography;\n" +
            "using System.Text;\n" +
            "using Newtonsoft.Json.Linq;\n" +
            "\n" +
            "namespace AesDecryptExample\n" +
            "{\n" +
            "    class Program\n" +
            "    {\n" +
            "        static void Main(string[] args)\n" +
            "        {\n" +
            "            string encryptedTableData = @\"\n" +
            "            {\n" +
            "                \"\"code\"\": 200,\n" +
            "                \"\"msg\"\": \"\"请求成功\"\",\n" +
            "                \"\"data\"\": {\n" +
            "                    \"\"current\"\": 1,\n" +
            "                    \"\"size\"\": 10,\n" +
            "                    \"\"total\"\": 1,\n" +
            "                    \"\"page\"\": 1,\n" +
            "                    \"\"encryptedFields\"\": [ \n" +
            "                        \"\"table_name\"\",\n" +
            "                        \"\"table_name_alias\"\",\n" +
            "                        \"\"table_type\"\"\n" +
            "                    ],\n" +
            "                    \"\"dataArray\"\": [\n" +
            "                        {\n" +
            "                            \"\"table_name_alias\"\": \"\"srS1j7V53QsaUka6HjLUJnLZCq9kQV+mcAMmImMzX1V3rj2tmnVi2uMbdI9bNZSb\"\",\n" +
            "                            \"\"table_name\"\": \"\"srS1j7V53QsaUka6HjLUJnLZCq9kQV+mcAMmImMzX1V3rj2tmnVi2uMbdI9bNZSb\"\",\n" +
            "                            \"\"table_type\"\": \"\"lMNgph4i2FMlIt8zlZu+Ig==\"\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"\"table_name_alias\"\": \"\"srS1j7V53QsaUka6HjLUJjDR/6/k1CNjrIqcFbFrugQ=\"\",\n" +
            "                            \"\"table_name\"\": \"\"srS1j7V53QsaUka6HjLUJjDR/6/k1CNjrIqcFbFrugQ=\"\",\n" +
            "                            \"\"table_type\"\": \"\"lMNgph4i2FMlIt8zlZu+Ig==\"\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            }\";\n" +
            "            string encryptKey = \"mysecretpassword\";\n" +
            "            string[] columnNames;\n" +
            "\n" +
            "            try\n" +
            "            {\n" +
            "                JObject jsonArray = JObject.Parse(encryptedTableData);\n" +
            "                columnNames = jsonArray[\"data\"][\"encryptedFields\"].ToObject<string[]>();\n" +
            "                JArray jsonNodes = (JArray)jsonArray[\"data\"][\"dataArray\"];\n" +
            "                foreach (JObject jsonNode in jsonNodes)\n" +
            "                {\n" +
            "                    foreach (string columnName in columnNames)\n" +
            "                    {\n" +
            "                        if (jsonNode.ContainsKey(columnName))\n" +
            "                        {\n" +
            "                            string illegallyNum = jsonNode.Value<string>(columnName);\n" +
            "                            string decryptedValue = DecryptField(illegallyNum, encryptKey);\n" +
            "                            jsonNode[columnName] = decryptedValue;\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "                string json = jsonArray.ToString();\n" +
            "                Console.WriteLine(json);\n" +
            "            }\n" +
            "            catch (Exception e)\n" +
            "            {\n" +
            "                Console.WriteLine(e);\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        private static string DecryptField(string encryptedValue, string key)\n" +
            "        {\n" +
            "            using (Aes aesAlg = Aes.Create())\n" +
            "            {\n" +
            "                aesAlg.Key = Encoding.UTF8.GetBytes(key);\n" +
            "                aesAlg.Mode = CipherMode.ECB;\n" +
            "                aesAlg.Padding = PaddingMode.PKCS7;\n" +
            "\n" +
            "                ICryptoTransform decryptor = aesAlg.CreateDecryptor(aesAlg.Key, aesAlg.IV);\n" +
            "\n" +
            "                byte[] encryptedBytes = Convert.FromBase64String(encryptedValue);\n" +
            "\n" +
            "                string plaintext = null;\n" +
            "\n" +
            "                using (MemoryStream msDecrypt = new MemoryStream(encryptedBytes))\n" +
            "                {\n" +
            "                    using (CryptoStream csDecrypt = new CryptoStream(msDecrypt, decryptor, CryptoStreamMode.Read))\n" +
            "                    {\n" +
            "                        using (StreamReader srDecrypt = new StreamReader(csDecrypt))\n" +
            "                        {\n" +
            "                            plaintext = srDecrypt.ReadToEnd();\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "\n" +
            "                return plaintext;\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";
    public static final String DATASERVICE_APICODEEXAMPLES_JAVA = "\n" +
            "package com.fisk.datagovernance.test;\n" +
            "\n" +
            "import com.alibaba.fastjson.JSONObject;\n" +
            "import org.apache.http.client.config.RequestConfig;\n" +
            "import org.apache.http.client.methods.CloseableHttpResponse;\n" +
            "import org.apache.http.client.methods.HttpPost;\n" +
            "import org.apache.http.entity.StringEntity;\n" +
            "import org.apache.http.impl.client.CloseableHttpClient;\n" +
            "import org.apache.http.impl.client.HttpClients;\n" +
            "import org.apache.http.util.EntityUtils;\n" +
            "import org.junit.Test;\n" +
            "import org.junit.runner.RunWith;\n" +
            "import org.springframework.boot.test.context.SpringBootTest;\n" +
            "import org.springframework.test.context.junit4.SpringRunner;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "import java.util.HashMap;\n" +
            "import java.util.List;\n" +
            "\n" +
            "@SpringBootTest\n" +
            "@RunWith(SpringRunner.class)\n" +
            "public class DataServiceUnitTesting {\n" +
            "\n" +
            "    @Test\n" +
            "    public void getData()\n" +
            "    {\n" +
            "        /*getToken*/\n" +
            "        String url = \"{api_prd_address}/dataservice/apiService/getToken\";\n" +
            "        // set request parameters\n" +
            "        GetTokenRequest getTokenRequest = new GetTokenRequest();\n" +
            "        getTokenRequest.appAccount = \"test0233\";\n" +
            "        getTokenRequest.appPassword = \"test0233\";\n" +
            "        String getTokenParams = JSONObject.toJSONString(getTokenRequest);\n" +
            "        // send request\n" +
            "        GetTokenResponse getTokenResponse = sendPostWebRequest(GetTokenResponse.class,\n" +
            "                url, getTokenParams, null);\n" +
            "\n" +
            "        /*getData*/\n" +
            "        url = \"{api_prd_address}/dataservice/apiService/getData\";\n" +
            "        // set request parameters\n" +
            "        GetDataRequest getDataRequest = new GetDataRequest();\n" +
            "        getDataRequest.apiCode = \"c5bbe530bc0b47e6bfbd256364270365\";\n" +
            "        HashMap<String, Object> parmList = new HashMap<>();\n" +
            "        parmList.put(\"product_colour\", \"blue\");\n" +
            "        parmList.put(\"product_price\", \"7000\");\n" +
            "        getDataRequest.parmList = parmList;\n" +
            "        getDataRequest.current = 1;\n" +
            "        getDataRequest.size = 500;\n" +
            "        String getDataParams = JSONObject.toJSONString(getDataRequest);\n" +
            "        // send request\n" +
            "        GetDataResponse getDataResponse = sendPostWebRequest(GetDataResponse.class,\n" +
            "                url, getDataParams, getTokenResponse.data);\n" +
            "    }\n" +
            "\n" +
            "    public static <T> T sendPostWebRequest(Class<T> c, String url,\n" +
            "                                           String parameters, String token)\n" +
            "    {\n" +
            "        T t = null;\n" +
            "        CloseableHttpClient httpClient = HttpClients.createDefault();\n" +
            "        CloseableHttpResponse response = null;\n" +
            "        String resultString = \"\";\n" +
            "        try {\n" +
            "            HttpPost httpPost = new HttpPost(url);\n" +
            "            httpPost.setHeader(\"Content-Type\", \"application/json;charset=utf-8\");\n" +
            "            httpPost.setHeader(\"Accept\", \"application/json;charset=utf-8\");\n" +
            "            if (token != null && token != \"\") {\n" +
            "                httpPost.setHeader(\"Authorization\", token);\n" +
            "            }\n" +
            "            RequestConfig config = RequestConfig.custom().\n" +
            "                    setConnectTimeout(35000).setConnectionRequestTimeout(35000).\n" +
            "                    setSocketTimeout(60000).build();\n" +
            "            httpPost.setConfig(config);\n" +
            "\n" +
            "            StringEntity entity = new StringEntity(parameters, \"UTF-8\");\n" +
            "            httpPost.setEntity(entity);\n" +
            "            response = httpClient.execute(httpPost);\n" +
            "            resultString = EntityUtils.toString(response.getEntity(), \"utf-8\");\n" +
            "        } catch (Exception e) {\n" +
            "            e.printStackTrace();\n" +
            "        } finally {\n" +
            "            try {\n" +
            "                if (response!=null){\n" +
            "                    response.close();\n" +
            "                }\n" +
            "            } catch (IOException e) {\n" +
            "                e.printStackTrace();\n" +
            "            }\n" +
            "        }\n" +
            "        if (resultString != null && resultString != \"\") {\n" +
            "            t = JSONObject.parseObject(resultString, c);\n" +
            "        }\n" +
            "        return t;\n" +
            "    }\n" +
            "\n" +
            "    public class GetTokenRequest\n" +
            "    {\n" +
            "        public String appAccount;\n" +
            "\n" +
            "        public String appPassword;\n" +
            "    }\n" +
            "\n" +
            "    public static class GetTokenResponse\n" +
            "    {\n" +
            "        public int code;\n" +
            "\n" +
            "        public String msg;\n" +
            "\n" +
            "        public String data;\n" +
            "    }\n" +
            "\n" +
            "    public class GetDataRequest\n" +
            "    {\n" +
            "        public String apiCode;\n" +
            "\n" +
            "        public HashMap<String, Object> parmList;\n" +
            "\n" +
            "        public Integer current;\n" +
            "\n" +
            "        public Integer size;\n" +
            "    }\n" +
            "\n" +
            "    public static class GetDataResponse\n" +
            "    {\n" +
            "        public int code;\n" +
            "\n" +
            "        public String msg;\n" +
            "\n" +
            "        public Data data;\n" +
            "    }\n" +
            "\n" +
            "    public static class Data\n" +
            "    {\n" +
            "        public Integer current;\n" +
            "\n" +
            "        public Integer size;\n" +
            "\n" +
            "        public Integer total;\n" +
            "\n" +
            "        public Integer page;\n" +
            "\n" +
            "        public List<DataArrayItem> dataArray;\n" +
            "    }\n" +
            "\n" +
            "    public static class DataArrayItem\n" +
            "    {\n" +
            "        public String product_name;\n" +
            "\n" +
            "        public String product_price;\n" +
            "    }\n" +
            "}\n";

    public static final String DATAACCESS_APICODEEXAMPLES_NET = "\n" +
            "using Newtonsoft.Json;\n" +
            "using Newtonsoft.Json.Serialization;\n" +
            "using System;\n" +
            "using System.Collections.Generic;\n" +
            "using System.IO;\n" +
            "using System.Net;\n" +
            "using System.Text;\n" +
            "\n" +
            "namespace ConsoleApp1\n" +
            "{\n" +
            "    public class Program\n" +
            "    {\n" +
            "        public static void Main(string[] args)\n" +
            "        {\n" +
            "            #region getToken\n" +
            "            string url = \"{api_prd_address}/dataAccess/apiConfig/getToken\";\n" +
            "            // set request parameters\n" +
            "            GetTokenRequest getTokenRequest = new GetTokenRequest\n" +
            "            {\n" +
            "                appAccount = \"testLock\",\n" +
            "                appPassword = \"testPwd\"\n" +
            "            };\n" +
            "            string getTokenParams = JsonConvert.SerializeObject(getTokenRequest);\n" +
            "            // send request\n" +
            "            string token = sendPostWebRequest<GetTokenResponse>(url, getTokenParams)?.data;\n" +
            "            #endregion\n" +
            "\n" +
            "            #region getData\n" +
            "            url = \"{api_prd_address}/dataAccess/apiConfig/getData\";\n" +
            "            // set request parameters\n" +
            "            GetDataRequest getDataRequest = new GetDataRequest()\n" +
            "            {\n" +
            "                apiCode = \"c5bbe530bc0b47e6bfbd256364270365\",\n" +
            "            };\n" +
            "            Dictionary<string, object> parmList = new Dictionary<string, object>();\n" +
            "            parmList.Add(\"product_colour\", \"blue\");\n" +
            "            parmList.Add(\"product_price\", \"7000\");\n" +
            "            getDataRequest.parmList = parmList;\n" +
            "            getDataRequest.current = 1;\n" +
            "            getDataRequest.size = 500;\n" +
            "            string getDataParams = JsonConvert.SerializeObject(getDataRequest);\n" +
            "            // send request\n" +
            "            GetDataResponse data = sendPostWebRequest<GetDataResponse>\n" +
            "                (url, getDataParams, token);\n" +
            "            #endregion\n" +
            "        }\n" +
            "\n" +
            "        public static T sendPostWebRequest<T>(string url, string parameters,\n" +
            "            string token = null)\n" +
            "        {\n" +
            "            T result;\n" +
            "            HttpWebResponse httpResponse = null;\n" +
            "            HttpWebRequest httpRequest = null;\n" +
            "            try\n" +
            "            {\n" +
            "                if (url.ToLower().Contains(\"https://\"))\n" +
            "                {\n" +
            "                    ServicePointManager.SecurityProtocol = SecurityProtocolType.Tls12;\n" +
            "                    httpRequest = (HttpWebRequest)WebRequest.CreateDefault(new Uri(url));\n" +
            "                }\n" +
            "                else\n" +
            "                {\n" +
            "                    httpRequest = (HttpWebRequest)WebRequest.Create(url);\n" +
            "                }\n" +
            "                httpRequest.Method = \"POST\";\n" +
            "                httpRequest.ContentType = \"application/json\";\n" +
            "                if (!string.IsNullOrEmpty(token))\n" +
            "                    httpRequest.Headers.Add(\"Authorization\", token);\n" +
            "                httpRequest.ServicePoint.Expect100Continue = false;\n" +
            "                using (StreamWriter dataStream = \n" +
            "                                     new StreamWriter(httpRequest.GetRequestStream()))\n" +
            "                {\n" +
            "                    dataStream.Write(parameters);\n" +
            "                    dataStream.Close();\n" +
            "                }\n" +
            "\n" +
            "                httpResponse = (HttpWebResponse)httpRequest.GetResponse();\n" +
            "\n" +
            "                var responseStream = httpResponse.GetResponseStream();\n" +
            "                var responseResult = new StreamReader(responseStream,\n" +
            "                    Encoding.GetEncoding(\"utf-8\")).ReadToEnd();\n" +
            "                result = JsonConvert.DeserializeObject<T>(responseResult,\n" +
            "                            new JsonSerializerSettings\n" +
            "                            {\n" +
            "                                ContractResolver = new DefaultContractResolver { }\n" +
            "                            });\n" +
            "            }\n" +
            "            catch (Exception ex)\n" +
            "            {\n" +
            "                throw ex;\n" +
            "            }\n" +
            "            finally\n" +
            "            {\n" +
            "                try\n" +
            "                {\n" +
            "                    if (httpResponse != null)\n" +
            "                    {\n" +
            "                        httpResponse.Close();\n" +
            "                    }\n" +
            "                }\n" +
            "                catch (IOException e)\n" +
            "                {\n" +
            "                    throw e;\n" +
            "                }\n" +
            "            }\n" +
            "            return result;\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    public class GetTokenRequest\n" +
            "    {\n" +
            "        public string appAccount { get; set; }\n" +
            "\n" +
            "        public string appPassword { get; set; }\n" +
            "    }\n" +
            "\n" +
            "    public class GetTokenResponse\n" +
            "    {\n" +
            "        public int code { get; set; }\n" +
            "\n" +
            "        public string msg { get; set; }\n" +
            "\n" +
            "        public string data { get; set; }\n" +
            "    }\n" +
            "\n" +
            "    public class GetDataRequest\n" +
            "    {\n" +
            "        public string apiCode;\n" +
            "\n" +
            "        public Dictionary<string, object> parmList;\n" +
            "\n" +
            "        public int? current;\n" +
            "\n" +
            "        public int? size;\n" +
            "    }\n" +
            "\n" +
            "    public class GetDataResponse\n" +
            "    {\n" +
            "        public int code { get; set; }\n" +
            "\n" +
            "        public string msg { get; set; }\n" +
            "\n" +
            "        public Data data { get; set; }\n" +
            "    }\n" +
            "\n" +
            "    public class Data\n" +
            "    {\n" +
            "        public int? current { get; set; }\n" +
            "\n" +
            "        public int? size { get; set; }\n" +
            "\n" +
            "        public int? total { get; set; }\n" +
            "\n" +
            "        public int? page { get; set; }\n" +
            "\n" +
            "        public List<DataArrayItem> dataArray { get; set; }\n" +
            "    }\n" +
            "\n" +
            "    public class DataArrayItem\n" +
            "    {\n" +
            "        public string product_name { get; set; }\n" +
            "\n" +
            "        public string product_price { get; set; }\n" +
            "    }\n" +
            "}\n";

    public static final String DATASERVICE_APICODEEXAMPLES_JAVA_ENCRYPT = "package com.fisk.dataservice.util;\n" +
            "import com.fasterxml.jackson.databind.JsonNode;\n" +
            "import com.fasterxml.jackson.databind.ObjectMapper;\n" +
            "import com.fasterxml.jackson.databind.node.ObjectNode;\n" +
            "import javax.crypto.Cipher;\n" +
            "import javax.crypto.spec.SecretKeySpec;\n" +
            "import java.util.Base64;\n" +
            "public class AESEncrypt {\n" +
            "    public static void main(String[] args) {\n" +
            "        String encryptedTableData = \"{\\n\" +\n" +
            "                \" \\\"code\\\": 200,\\n\" +\n" +
            "                \" \\\"msg\\\": \\\"\\\",\\n\" +\n" +
            "                \" \\\"data\\\": {\\n\" +\n" +
            "                \" \\\"current\\\": 1,\\n\" +\n" +
            "                \" \\\"size\\\": 10,\\n\" +\n" +
            "                \" \\\"total\\\": 1,\\n\" +\n" +
            "                \" \\\"page\\\": 1,\\n\" +\n" +
            "                \"\\\"encryptedFields\\\": [ \\n\" +\n" +
            "                \"\\\"table_name\\\",\\n\" +\n" +
            "                \"\\\"table_name_alias\\\",\\n\" +\n" +
            "                \"\\\"table_type\\\"\\n\" +\n" +
            "                \" ], \\n\" +\n" +
            "                \" \\\"dataArray\\\": [\\n\" +\n" +
            "                \" {\\n\" +\n" +
            "                \" \\\"table_name_alias\\\":\\\"srS1j7V53QsaUka6HjLUJnLZCq9kQV+mcAMmImMzX1V3rj2tmnVi2uMbdI9bNZSb\\\",\\n\" +\n" +
            "                \" \\\"table_name\\\":\\\"srS1j7V53QsaUka6HjLUJnLZCq9kQV+mcAMmImMzX1V3rj2tmnVi2uMbdI9bNZSb\\\",\\n\" +\n" +
            "                \" \\\"table_type\\\": \\\"lMNgph4i2FMlIt8zlZu+Ig==\\\"\\n\" +\n" +
            "                \" },\\n\" +\n" +
            "                \" {\\n\" +\n" +
            "        \" \\\"table_name_alias\\\":\\\"srS1j7V53QsaUka6HjLUJjDR/6/k1CNjrIqcFbFrugQ=\\\",\\n\" +\n" +
            "                \" \\\"table_name\\\":\\\"srS1j7V53QsaUka6HjLUJjDR/6/k1CNjrIqcFbFrugQ=\\\",\\n\" +\n" +
            "                \" \\\"table_type\\\": \\\"lMNgph4i2FMlIt8zlZu+Ig==\\\"\\n\" +\n" +
            "                \" }\\n\" +\n" +
            "                \" ]\\n\" +\n" +
            "                \" }\\n\" +\n" +
            "                \"}\";\n" +
            "        String encryptKey = \"mysecretpassword\";\n" +
            "        String[] columnNames;\n" +
            "        try {\n" +
            "            ObjectMapper objectMapper = new ObjectMapper();\n" +
            "            JsonNode jsonArray = objectMapper.readTree(encryptedTableData);\n" +
            "            JsonNode encryptedFields = jsonArray.get(\"data\").get(\"encryptedFields\");\n" +
            "//            encryptedFields = null;\n" +
            "            if (encryptedFields != null){\n" +
            "                columnNames = objectMapper.readValue(encryptedFields.toString(), String[].class);\n" +
            "                JsonNode jsonNodes = jsonArray.get(\"data\").get(\"dataArray\");\n" +
            "                for (JsonNode jsonNode : jsonNodes) {\n" +
            "                    for (String columnName : columnNames) {\n" +
            "                        if (jsonNode.has(columnName)) {\n" +
            "                            String illegallyNum = jsonNode.get(columnName).asText();\n" +
            "                            String encryptedIllegallyNum = decryptField(illegallyNum, encryptKey);\n" +
            "                            ((ObjectNode) jsonNode).put(columnName, encryptedIllegallyNum);\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "            String json = objectMapper.writeValueAsString(jsonArray);\n" +
            "            System.out.println(json);\n" +
            "        } catch (Exception e) {\n" +
            "            e.printStackTrace();\n" +
            "        }\n" +
            "    }\n" +
            "    private static String decryptField(String encryptedValue, String key) throws Exception {\n" +
            "        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), \"AES\");\n" +
            "        Cipher cipher = Cipher.getInstance(\"AES\");\n" +
            "        cipher.init(Cipher.DECRYPT_MODE, secretKey);\n" +
            "        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));\n" +
            "        return new String(decryptedBytes);\n" +
            "    }\n" +
            "}";
    public static final String DATAACCESS_APICODEEXAMPLES_JAVA = "\n" +
            "package com.fisk.dataaccess.test;\n" +
            "\n" +
            "import com.alibaba.fastjson.JSONObject;\n" +
            "import org.apache.http.client.config.RequestConfig;\n" +
            "import org.apache.http.client.methods.CloseableHttpResponse;\n" +
            "import org.apache.http.client.methods.HttpPost;\n" +
            "import org.apache.http.entity.StringEntity;\n" +
            "import org.apache.http.impl.client.CloseableHttpClient;\n" +
            "import org.apache.http.impl.client.HttpClients;\n" +
            "import org.apache.http.util.EntityUtils;\n" +
            "import org.junit.Test;\n" +
            "import org.junit.runner.RunWith;\n" +
            "import org.springframework.boot.test.context.SpringBootTest;\n" +
            "import org.springframework.test.context.junit4.SpringRunner;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "import java.util.HashMap;\n" +
            "import java.util.List;\n" +
            "\n" +
            "@SpringBootTest\n" +
            "@RunWith(SpringRunner.class)\n" +
            "public class DataServiceUnitTesting {\n" +
            "\n" +
            "    @Test\n" +
            "    public void getData()\n" +
            "    {\n" +
            "        /*getToken*/\n" +
            "        String url = \"{api_prd_address}/dataAccess/apiConfig/getToken\";\n" +
            "        // set request parameters\n" +
            "        GetTokenRequest getTokenRequest = new GetTokenRequest();\n" +
            "        getTokenRequest.useraccount = \"testLock\";\n" +
            "        getTokenRequest.password = \"testPwd\";\n" +
            "        String getTokenParams = JSONObject.toJSONString(getTokenRequest);\n" +
            "        // send request\n" +
            "        GetTokenResponse getTokenResponse = sendPostWebRequest(GetTokenResponse.class,\n" +
            "                url, getTokenParams, null);\n" +
            "\n" +

            "        /*getAesKey*/\n" +
            "        String url = \"{api_prd_address}/dataAccess/appRegistration/getAesKey\";\n" +
            "        // set request parameters\n" +
            "        GetAesKeyRequest getAesKeyRequest = new GetAesKeyRequest();\n" +
            "        getAesKeyRequest.apiCode = \"94\";\n" +
            "        // send request\n" +
            "        GetAesKeyResponse getAesKeyResponse = sendPostWebRequest(GetAesKeyResponse.class,\n" +
            "                url, getAesKeyRequest, getTokenResponse.data);\n" +
            "\n" +

            "        /*pushData*/\n" +
            "        url = \"{api_prd_address}/dataAccess/apiConfig/pushdata\";\n" +
            "        // set request parameters\n" +
            "        GetDataRequest getDataRequest = new GetDataRequest();\n" +
            "        getDataRequest.apiCode = \"94\";\n" +
            "        getDataRequest.pushData = " +
            "       \"{\\\"data\\\": []\\n}\\n\";\n" +
            "        /////////////////////////////////////This code is used on demand,\n         //depending on whether AES encryption is enabled for data transfer\n" +
            "        // Get AES key\n" +
            "        String aesKey= getAesKeyResponse.data;\n" +
            "        SecretKeySpec secretKeySpec = decryptionKey(aseKey);\n" +
            "        // Encrypt data\n" +
            "        getDataRequest.pushData = encryptJsonData(jsonData, secretKeySpec);\n" +
            "        /////////////////////////////////////This code is used on demand,\n         //depending on whether AES encryption is enabled for data transfer\n" +
            "\n" +
            "        String getDataParams = JSONObject.toJSONString(getDataRequest);\n" +
            "        // send request\n" +
            "        GetDataResponse getDataResponse = sendPostWebRequest(GetDataResponse.class,\n" +
            "                url, getDataParams, getTokenResponse.data);\n" +
            "    }\n" +
            "\n" +
            "    // AES encrypts JSON data\n" +
            "    private static String encryptJsonData(String jsonData, SecretKey secretKey) {\n" +
            "\n" +
            "        Cipher cipher = null;\n" +
            "        try {\n" +
            "            // Use ECB mode and PKCS5Padding padding\n" +
            "            cipher = Cipher.getInstance(\"AES/ECB/PKCS5Padding\");\n" +
            "            cipher.init(Cipher.ENCRYPT_MODE, secretKey);\n" +
            "            // Convert JSON data into byte arrays\n" +
            "            byte[] dataBytes = jsonData.getBytes(StandardCharsets.UTF_8);\n" +
            "            // Encrypt data\n" +
            "            byte[] encryptedBytes = cipher.doFinal(dataBytes);\n" +
            "            // Convert encrypted data to Base64 encoding\n" +
            "            return Base64.getEncoder().encodeToString(encryptedBytes);\n" +
            "        } catch (Exception e) {\n" +
            "            e.printStackTrace();\n" +
            "        }\n" +
            "        return null;\n" +
            "    }" +
            "\n" +
            "\n"+
            "    // Convert the base64-encoded key to an AES key object\n" +
            "    private static SecretKeySpec decryptionKey(String base64EncodedKey) {\n" +
            "        byte[] keyBytes = Base64.getDecoder().decode(base64EncodedKey);\n" +
            "        return new SecretKeySpec(keyBytes, \"AES\");\n" +
            "    }\n" +
            "\n"+
            "    public static <T> T sendPostWebRequest(Class<T> c, String url,\n" +
            "                                           String parameters, String token)\n" +
            "    {\n" +
            "        T t = null;\n" +
            "        CloseableHttpClient httpClient = HttpClients.createDefault();\n" +
            "        CloseableHttpResponse response = null;\n" +
            "        String resultString = \"\";\n" +
            "        try {\n" +
            "            HttpPost httpPost = new HttpPost(url);\n" +
            "            httpPost.setHeader(\"Content-Type\", \"application/json;charset=utf-8\");\n" +
            "            httpPost.setHeader(\"Accept\", \"application/json;charset=utf-8\");\n" +
            "            if (token != null && token != \"\") {\n" +
            "                httpPost.setHeader(\"Authorization\", token);\n" +
            "            }\n" +
            "            RequestConfig config = RequestConfig.custom().\n" +
            "                    setConnectTimeout(35000).setConnectionRequestTimeout(35000).\n" +
            "                    setSocketTimeout(60000).build();\n" +
            "            httpPost.setConfig(config);\n" +
            "\n" +
            "            StringEntity entity = new StringEntity(parameters, \"UTF-8\");\n" +
            "            httpPost.setEntity(entity);\n" +
            "            response = httpClient.execute(httpPost);\n" +
            "            resultString = EntityUtils.toString(response.getEntity(), \"utf-8\");\n" +
            "        } catch (Exception e) {\n" +
            "            e.printStackTrace();\n" +
            "        } finally {\n" +
            "            try {\n" +
            "                if (response!=null){\n" +
            "                    response.close();\n" +
            "                }\n" +
            "            } catch (IOException e) {\n" +
            "                e.printStackTrace();\n" +
            "            }\n" +
            "        }\n" +
            "        if (resultString != null && resultString != \"\") {\n" +
            "            t = JSONObject.parseObject(resultString, c);\n" +
            "        }\n" +
            "        return t;\n" +
            "    }\n" +
            "\n" +
            "    public class GetTokenRequest\n" +
            "    {\n" +
            "        public String useraccount;\n" +
            "\n" +
            "        public String password;\n" +
            "    }\n" +
            "\n" +
            "    public static class GetTokenResponse\n" +
            "    {\n" +
            "        public int code;\n" +
            "\n" +
            "        public String msg;\n" +
            "\n" +
            "        public String data;\n" +
            "    }\n" +
            "\n" +
            "    public class GetDataRequest\n" +
            "    {\n" +
            "        public String apiCode;\n" +
            "\n" +
            "        public String pushData;\n" +
            "    }\n" +
            "\n" +
            "    public static class GetDataResponse\n" +
            "    {\n" +
            "        public int code;\n" +
            "\n" +
            "        public String msg;\n" +
            "\n" +
            "        public Data data;\n" +
            "    }\n" +
            "\n" +

            "\n" +
            "    public class GetAesKeyRequest\n" +
            "    {\n" +
            "        public String apiCode;\n" +
            "    }\n" +
            "\n" +

            "\n" +
            "    public static class GetAesKeyResponse\n" +
            "    {\n" +
            "        public int code;\n" +
            "\n" +
            "        public String msg;\n" +
            "\n" +
            "        public Data data;\n" +
            "    }\n" +
            "\n" +

            "    public static class Data\n" +
            "    {\n" +
            "        public Integer current;\n" +
            "\n" +
            "        public Integer size;\n" +
            "\n" +
            "        public Integer total;\n" +
            "\n" +
            "        public Integer page;\n" +
            "\n" +
            "        public List<DataArrayItem> dataArray;\n" +
            "    }\n" +
            "\n" +
            "    public static class DataArrayItem\n" +
            "    {\n" +
            "        public String product_name;\n" +
            "\n" +
            "        public String product_price;\n" +
            "    }\n" +
            "}\n";

    public static final String DATAACCESS_WEBSERVICE_BASICINFO = "{\n" +
            "    \"title\":\"汎得立体库前置机 webService(SOAP协议)接口文档\",\n" +
            "    \"docVersion\":\"文档版本 V1.0\",\n" +
            "    \"isuCompany\":\"\",\n" +
            "    \"isuDate\":\"发布日期：20231012\",\n" +
            "    \"footerName\":\"webservice接口文档\",\n" +
            "    \"docPurpose\":\"本文用于第三方平台对接汎得立体库前置机。\",\n" +
            "    \"readers\":\"预期读者包括需要推送数据到汎得立体库前置机的第三方平台。\",\n" +
            "    \"standard\":\"接口采用SOAP协议，数据传输格式采用非加密的xml格式。文本编码格式为UTF-8；\",\n" +
            "    \"standard_query\":\" 暂无\",\n" +
            "    \"authStandard\":\"第三方系统在访问前置机WEBSERVICE时需要进行身份验证，通过调用“获取Token”接口，传递账号密码获取Token（6小时有效期）。\",\n" +
            "    \"uatAddress\":\"{api_uat_address}/{apiaddress}\",\n" +
//            "    \"prdAddress\":\"生产环境：{api_prd_address}(预留地址,暂不使用)\",\n" +
            "    \"apiCatalogueDTOS\":[\n" +
            "        {\n" +
            "            \"grade\":1,\n" +
            "            \"catalogueIndex\":\"\",\n" +
            "            \"catalogueName\":\"目录\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"一.\",\n" +
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
//            "        {\n" +
//            "            \"grade\":3,\n" +
//            "            \"catalogueIndex\":\"1.3.\",\n" +
//            "            \"catalogueName\":\"相关联系人\"\n" +
//            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"二.\",\n" +
            "            \"catalogueName\":\"WebService 接口\"\n" +
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
            "            \"catalogueName\":\"接口地址\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"2.4.\",\n" +
            "            \"catalogueName\":\"获取Token接口\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"三.\",\n" +
            "            \"catalogueName\":\"C#代码调用示例\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"四.\",\n" +
            "            \"catalogueName\":\"java代码调用示例\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"五.\",\n" +
            "            \"catalogueName\":\"WebService返回代码示例\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"apiBasicInfoDTOS\":[\n" +
            "        {\n" +
            "            \"apiName\":\"获取Token接口\",\n" +
            "            \"apiNameCatalogue\":\"2.4.\",\n" +
            "            \"apiAddress\":\"/http://192.168.11.130:8089/webservice/fidata-api?wsdl        【webservice获取token的方法名称：webServiceGetToken】\",\n" +
            "            \"apiAddressCatalogue\":\"2.4.1.\",\n" +
            "            \"apiDesc\":\"获取身份凭证，后续请求中将此凭证作为身份标识传给业务接口，业务接口将验证身份凭证是否合法，合法则返回处理结果。\",\n" +
            "            \"apiDescCatalogue\":\"2.4.2.\",\n" +
            "            \"apiRequestType\":\"WebService(SOAP)\",\n" +
            "            \"apiRequestTypeCatalogue\":\"2.4.3.\",\n" +
            "            \"apiContentType\":\"xml\",\n" +
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
            "                    \"parmName\":\"token\",\n" +
            "                    \"parmType\":\"String\",\n" +
            "                    \"parmDesc\":\"用于验证的token\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"apiResponseCatalogue\":\"2.4.9.\"\n" +
            "        }\n" +
            "    ],\n" +
//            "    \"apiContactsDTOS\":[\n" +
//            "        {\n" +
//            "            \"category\":\"接口负责人\",\n" +
//            "            \"company\":\"技术部\",\n" +
//            "            \"fullName\":\"徐阳辉\",\n" +
//            "            \"mailbox\":\"yhxu@fisksoft.com\",\n" +
//            "            \"trStyle\":\"background-color: #fff\"\n" +
//            "        },\n" +
//            "        {\n" +
//            "            \"category\":\"接口负责人\",\n" +
//            "            \"company\":\"技术部\",\n" +
//            "            \"fullName\":\"赵飞鸿\",\n" +
//            "            \"mailbox\":\"feihongz@fisksoft.com.cn\",\n" +
//            "            \"trStyle\":\"background-color: #f8f8f8\"\n" +
//            "        }\n" +
//            "    ],\n" +
            "    \"apiVersionDTOS\":[\n" +
            "        {\n" +
            "            \"version\":\"0.1\",\n" +
            "            \"startDate\":\"2023/10/12\",\n" +
            "            \"endDate\":\"2023/10/12\",\n" +
            "            \"modifier\":\"TenLi\",\n" +
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

    public static final String DATAACCESS_WEBSERVICECODEEXAMPLES_JAVA = "\n" +
            "package com.fisk.dataaccess.test;\n" +
            "\n" +
            "import com.alibaba.fastjson.JSON;\n" +
            "import com.fisk.dataaccess.webservice.service.WebServiceReceiveDataDTO;\n" +
            "import com.fisk.dataaccess.webservice.service.WebServiceUserDTO;\n" +
            "import lombok.extern.slf4j.Slf4j;\n" +
            "import org.apache.cxf.endpoint.Client;\n" +
            "import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;\n" +
            "import org.apache.cxf.transport.http.HTTPConduit;\n" +
            "import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;\n" +
            "\n" +
            "@Slf4j\n" +
            "public class WebServiceDemo {\n" +
            "\n" +
            "    /**\n" +
            "     * FiData java code example\n" +
            "     * Third-party companies call FiData webService to push data interface by Java code\n" +
            "     *\n" +
            "     * @Date 2023/10/13\n" +
            "     * @Version 1.0\n" +
            "     */\n" +
            "    public static void main(String[] args) {\n" +
            "        Client client = null;\n" +
            "        try {\n" +
            "            JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();\n" +
            "            client = dcf.createClient(\"{web_service_address}\");\n" +
            "\n" +
            "            // config HTTPConduit\n" +
            "            HTTPConduit conduit = (HTTPConduit) client.getConduit();\n" +
            "            HTTPClientPolicy policy = new HTTPClientPolicy();\n" +
            "            policy.setAllowChunking(false);\n" +
            "            // ConnectionTimeout 60 seconds\n" +
            "            policy.setConnectionTimeout(60000);\n" +
            "            // ReceiveTimeout 60 seconds\n" +
            "            policy.setReceiveTimeout(60000);\n" +
            "            conduit.setClient(policy);\n" +
            "\n" +
            "            // webServiceGetToken - variable\n" +
            "            WebServiceUserDTO webServiceUserDTO = new WebServiceUserDTO();\n" +
            "            webServiceUserDTO.setPassword(\"pwd\");\n" +
            "            webServiceUserDTO.setUseraccount(\"username\");\n" +
            "            // invoke methods : webServiceGetToken\n" +
            "            Object[] objects = client.invoke(\"webServiceGetToken\", webServiceUserDTO);\n" +
            "            // webServiceGetToken receive data : token\n" +
            "            String token = objects[0].toString();\n" +
            "\n" +
            "            // webServicePushData - variable\n" +
            "            WebServiceReceiveDataDTO webServiceReceiveDataDTO = new WebServiceReceiveDataDTO();\n" +
            "            // WebServiceCode\n" +
            "            webServiceReceiveDataDTO.setWebServiceCode(123L);\n" +
            "            // your Json data\n" +
            "            webServiceReceiveDataDTO.setData(\"your Json data\");\n" +
            "            // token\n" +
            "            webServiceReceiveDataDTO.setToken(token);\n" +
            "            // Start calling method - invoke(\"method name\", parameter 1, parameter 2, parameter 3....);\n" +
            "            Object[] result = client.invoke(\"webServicePushData\", webServiceReceiveDataDTO);\n" +
            "            // webServicePushData receive data : result\n" +
            "            String msg = JSON.toJSONString(result);\n" +
            "            // print result or do sth else\n" +
            "            System.out.println(msg);\n" +
            "        } catch (Exception e) {\n" +
            "            log.error(\"webService error--\" + e);\n" +
            "        }finally {\n" +
            "            if (client != null) {\n" +
            "                try {\n" +
            "                    client.close();\n" +
            "                } catch (Exception e) {\n" +
            "                    log.error(\"webService error--\" + e);\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "}\n" +
            "\n" +
            "package com.fisk.dataaccess.webservice.service;\n" +
            "\n" +
            "import io.swagger.annotations.ApiModelProperty;\n" +
            "import lombok.Data;\n" +
            "\n" +
            "import javax.xml.bind.annotation.XmlElement;\n" +
            "import javax.xml.bind.annotation.XmlRootElement;\n" +
            "\n" +
            "/**\n" +
            " * @author TenLi\n" +
            " * @date 2023/10/9\n" +
            " */\n" +
            "@Data\n" +
            "public class WebServiceUserDTO {\n" +
            "    /**\n" +
            "     * 账号\n" +
            "     */\n" +
            "    @ApiModelProperty(value = \"账号\", required = true)\n" +
            "    private String useraccount;\n" +
            "\n" +
            "    /**\n" +
            "     * 密码\n" +
            "     */\n" +
            "    @ApiModelProperty(value = \"密码\", required = true)\n" +
            "    private String password;\n" +
            "\n" +
            "    @XmlElement(name = \"useraccount\", nillable = false,required = true)\n" +
            "    public String getUseraccount() {\n" +
            "        return useraccount;\n" +
            "    }\n" +
            "\n" +
            "    public void setUseraccount(String useraccount) {\n" +
            "        this.useraccount = useraccount;\n" +
            "    }\n" +
            "\n" +
            "    @XmlElement(name = \"password\", nillable = false,required = true)\n" +
            "    public String getPassword() {\n" +
            "        return password;\n" +
            "    }\n" +
            "\n" +
            "    public void setPassword(String password) {\n" +
            "        this.password = password;\n" +
            "    }\n" +
            "}" +
            "package com.fisk.dataaccess.webservice.service;\n" +
            "\n" +
            "import io.swagger.annotations.ApiModelProperty;\n" +
            "import lombok.Data;\n" +
            "\n" +
            "import javax.xml.bind.annotation.XmlElement;\n" +
            "import java.io.Serializable;\n" +
            "\n" +
            "/**\n" +
            " * @author TenLi\n" +
            " * @date 2023/10/9\n" +
            " */\n" +
            "@Data\n" +
            "public class WebServiceReceiveDataDTO implements Serializable {\n" +
            "\n" +
            "    /**\n" +
            "     * webServiceCode\n" +
            "     */\n" +
            "    @ApiModelProperty(value = \"webServiceCode\", required = true)\n" +
            "    private Long webServiceCode;\n" +
            "\n" +
            "    /**\n" +
            "     * webService推送的数据\n" +
            "     */\n" +
            "    @ApiModelProperty(value = \"webService推送的数据\", required = true)\n" +
            "    private String data;\n" +
            "\n" +
            "\n" +
            "    /**\n" +
            "     * webService携带的token\n" +
            "     */\n" +
            "    @ApiModelProperty(value = \"webService携带的token\", required = true)\n" +
            "    private String token;\n" +
            "\n" +
            "    @XmlElement(name = \"webServiceCode\", nillable = false, required = true)\n" +
            "    public Long getWebServiceCode() {\n" +
            "        return webServiceCode;\n" +
            "    }\n" +
            "\n" +
            "    public void setWebServiceCode(Long webServiceCode) {\n" +
            "        this.webServiceCode = webServiceCode;\n" +
            "    }\n" +
            "\n" +
            "    @XmlElement(name = \"data\", nillable = false, required = true)\n" +
            "    public String getData() {\n" +
            "        return data;\n" +
            "    }\n" +
            "\n" +
            "    public void setData(String data) {\n" +
            "        this.data = data;\n" +
            "    }\n" +
            "\n" +
            "    @XmlElement(name = \"token\", nillable = false, required = true)\n" +
            "    public String getToken() {\n" +
            "        return token;\n" +
            "    }\n" +
            "\n" +
            "    public void setToken(String token) {\n" +
            "        this.token = token;\n" +
            "    }\n" +
            "}\n";


    public static final String GOVERNANCE_APIBASICINFO = "{\n" +
            "    \"title\":\"一体化数据赋能平台 API接口文档\",\n" +
            "    \"docVersion\":\"文档版本 V1.0\",\n" +
            "    \"isuCompany\":\"\",\n" +
            "    \"isuDate\":\"发布日期：{release_Date}\",\n" +
            "    \"footerName\":\"接口文档\",\n" +
            "    \"docPurpose\":\"本文用于第三方平台对接一体化数据赋能平台。\",\n" +
            "    \"readers\":\"预期读者包括需要从一体化数据赋能平台中获取数据的第三方平台。\",\n" +
            "    \"standard\":\"接口采用HTTP协议，TCP连接方式。数据传输格式采用非加密的JSON格式。API请求方式为POST，文本编码格式为UTF-8；\",\n" +
            "    \"authStandard\":\"第三方系统在访问平台API时需要进行身份验证，通过调用“获取Token”接口，传递账号密码获取Token（60分钟有效期）。\",\n" +
            "    \"prdAddress\":\"{api_prd_address}/{apiaddress}\",\n" +
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
            "            \"catalogueName\":\"接口地址\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":3,\n" +
            "            \"catalogueIndex\":\"2.4.\",\n" +
            "            \"catalogueName\":\"获取Token接口\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"3.\",\n" +
            "            \"catalogueName\":\"C#代码调用示例\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"4.\",\n" +
            "            \"catalogueName\":\"java代码调用示例\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"grade\":2,\n" +
            "            \"catalogueIndex\":\"5.\",\n" +
            "            \"catalogueName\":\"API返回代码示例\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"apiBasicInfoDTOS\":[\n" +
            "        {\n" +
            "            \"apiName\":\"获取Token接口\",\n" +
            "            \"apiNameCatalogue\":\"2.4.\",\n" +
            "            \"apiAddress\":\"/datagovernance/datacheckapi/getToken\",\n" +
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
            "            \"apiResponseHeaderDesc\":\"返回参数说明\",\n" +
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
            "            \"company\":\"技术部\",\n" +
            "            \"fullName\":\"徐阳辉\",\n" +
            "            \"mailbox\":\"yhxu@fisksoft.com\",\n" +
            "            \"trStyle\":\"background-color: #fff\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"category\":\"接口负责人\",\n" +
            "            \"company\":\"技术部\",\n" +
            "            \"fullName\":\"李家温\",\n" +
            "            \"mailbox\":\"dick@fisksoft.com\",\n" +
            "            \"trStyle\":\"background-color: #f8f8f8\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"apiVersionDTOS\":[\n" +
            "        {\n" +
            "            \"version\":\"0.1\",\n" +
            "            \"startDate\":\"{apiVersion_StartDate}\",\n" +
            "            \"endDate\":\"{apiVersion_EndDate}\",\n" +
            "            \"modifier\":\"{apiVersion_Modified}\",\n" +
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


    public static final String GOVERNANCE_APICODEEXAMPLES_NET = "\n" +
            "using Newtonsoft.Json;\n" +
            "using Newtonsoft.Json.Serialization;\n" +
            "using System;\n" +
            "using System.Collections.Generic;\n" +
            "using System.IO;\n" +
            "using System.Net;\n" +
            "using System.Text;\n" +
            "\n" +
            "namespace ConsoleApp1\n" +
            "{\n" +
            "    public class Program\n" +
            "    {\n" +
            "        public static void Main(string[] args)\n" +
            "        {\n" +
            "            #region getToken\n" +
            "            string url = \"{api_prd_address}/datagovernance/datacheckapi/getToken\";\n" +
            "            // set request parameters\n" +
            "            GetTokenRequest getTokenRequest = new GetTokenRequest\n" +
            "            {\n" +
            "                appAccount = \"test0233\",\n" +
            "                appPassword = \"test0233\"\n" +
            "            };\n" +
            "            string getTokenParams = JsonConvert.SerializeObject(getTokenRequest);\n" +
            "            // send request\n" +
            "            string token = sendPostWebRequest<GetTokenResponse>(url, getTokenParams)?.data;\n" +
            "            #endregion\n" +
            "\n" +
            "            #region getData\n" +
            "            url = \"{api_prd_address}/datagovernance/datacheckapi/getData\";\n" +
            "            // set request parameters\n" +
            "            GetDataRequest getDataRequest = new GetDataRequest()\n" +
            "            {\n" +
            "                apiCode = \"c5bbe530bc0b47e6bfbd256364270365\",\n" +
            "            };\n" +
            "            Dictionary<string, object> data = new Dictionary<string, object>();\n" +
            "            data.Add(\"product_colour\", \"blue\");\n" +
            "            data.Add(\"product_price\", \"7000\");\n" +
            "            getDataRequest.data = data;\n" +
            "            string getDataParams = JsonConvert.SerializeObject(getDataRequest);\n" +
            "            // send request\n" +
            "            GetDataResponse data = sendPostWebRequest<GetDataResponse>\n" +
            "                (url, getDataParams, token);\n" +
            "            #endregion\n" +
            "        }\n" +
            "\n" +
            "        public static T sendPostWebRequest<T>(string url, string parameters,\n" +
            "            string token = null)\n" +
            "        {\n" +
            "            T result;\n" +
            "            HttpWebResponse httpResponse = null;\n" +
            "            HttpWebRequest httpRequest = null;\n" +
            "            try\n" +
            "            {\n" +
            "                if (url.ToLower().Contains(\"https://\"))\n" +
            "                {\n" +
            "                    ServicePointManager.SecurityProtocol = SecurityProtocolType.Tls12;\n" +
            "                    httpRequest = (HttpWebRequest)WebRequest.CreateDefault(new Uri(url));\n" +
            "                }\n" +
            "                else\n" +
            "                {\n" +
            "                    httpRequest = (HttpWebRequest)WebRequest.Create(url);\n" +
            "                }\n" +
            "                httpRequest.Method = \"POST\";\n" +
            "                httpRequest.ContentType = \"application/json\";\n" +
            "                if (!string.IsNullOrEmpty(token))\n" +
            "                    httpRequest.Headers.Add(\"Authorization\", token);\n" +
            "                httpRequest.ServicePoint.Expect100Continue = false;\n" +
            "                using (StreamWriter dataStream = \n" +
            "                                     new StreamWriter(httpRequest.GetRequestStream()))\n" +
            "                {\n" +
            "                    dataStream.Write(parameters);\n" +
            "                    dataStream.Close();\n" +
            "                }\n" +
            "\n" +
            "                httpResponse = (HttpWebResponse)httpRequest.GetResponse();\n" +
            "\n" +
            "                var responseStream = httpResponse.GetResponseStream();\n" +
            "                var responseResult = new StreamReader(responseStream,\n" +
            "                    Encoding.GetEncoding(\"utf-8\")).ReadToEnd();\n" +
            "                result = JsonConvert.DeserializeObject<T>(responseResult,\n" +
            "                            new JsonSerializerSettings\n" +
            "                            {\n" +
            "                                ContractResolver = new DefaultContractResolver { }\n" +
            "                            });\n" +
            "            }\n" +
            "            catch (Exception ex)\n" +
            "            {\n" +
            "                throw ex;\n" +
            "            }\n" +
            "            finally\n" +
            "            {\n" +
            "                try\n" +
            "                {\n" +
            "                    if (httpResponse != null)\n" +
            "                    {\n" +
            "                        httpResponse.Close();\n" +
            "                    }\n" +
            "                }\n" +
            "                catch (IOException e)\n" +
            "                {\n" +
            "                    throw e;\n" +
            "                }\n" +
            "            }\n" +
            "            return result;\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    public class GetTokenRequest\n" +
            "    {\n" +
            "        public string appAccount { get; set; }\n" +
            "\n" +
            "        public string appPassword { get; set; }\n" +
            "    }\n" +
            "\n" +
            "    public class GetTokenResponse\n" +
            "    {\n" +
            "        public int code { get; set; }\n" +
            "\n" +
            "        public string msg { get; set; }\n" +
            "\n" +
            "        public string data { get; set; }\n" +
            "    }\n" +
            "\n" +
            "    public class GetDataRequest\n" +
            "    {\n" +
            "        public string apiCode;\n" +
            "\n" +
            "        public Dictionary<string, object> data;\n" +
            "    }\n" +
            "\n" +
            "    public class GetDataResponse\n" +
            "    {\n" +
            "        public int code { get; set; }\n" +
            "\n" +
            "        public string msg { get; set; }\n" +
            "\n" +
            "        public Data data { get; set; }\n" +
            "    }\n" +
            "\n" +
            "    public class Data\n" +
            "    {\n" +
            "\n" +
            "        public List<DataArrayItem> data { get; set; }\n" +
            "    }\n" +
            "\n" +
            "    public class DataArrayItem\n" +
            "    {\n" +
            "        public string product_name { get; set; }\n" +
            "\n" +
            "        public string product_price { get; set; }\n" +
            "    }\n" +
            "}\n";

    public static final String GOVERNANCE_APICODEEXAMPLES_JAVA = "\n" +
            "package com.fisk.datagovernance.test;\n" +
            "\n" +
            "import com.alibaba.fastjson.JSONObject;\n" +
            "import org.apache.http.client.config.RequestConfig;\n" +
            "import org.apache.http.client.methods.CloseableHttpResponse;\n" +
            "import org.apache.http.client.methods.HttpPost;\n" +
            "import org.apache.http.entity.StringEntity;\n" +
            "import org.apache.http.impl.client.CloseableHttpClient;\n" +
            "import org.apache.http.impl.client.HttpClients;\n" +
            "import org.apache.http.util.EntityUtils;\n" +
            "import org.junit.Test;\n" +
            "import org.junit.runner.RunWith;\n" +
            "import org.springframework.boot.test.context.SpringBootTest;\n" +
            "import org.springframework.test.context.junit4.SpringRunner;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "import java.util.HashMap;\n" +
            "import java.util.List;\n" +
            "\n" +
            "@SpringBootTest\n" +
            "@RunWith(SpringRunner.class)\n" +
            "public class DataServiceUnitTesting {\n" +
            "\n" +
            "    @Test\n" +
            "    public void getData()\n" +
            "    {\n" +
            "        /*getToken*/\n" +
            "        String url = \"{api_prd_address}/datagovernance/datacheckapi/getToken\";\n" +
            "        // set request parameters\n" +
            "        GetTokenRequest getTokenRequest = new GetTokenRequest();\n" +
            "        getTokenRequest.appAccount = \"test0233\";\n" +
            "        getTokenRequest.appPassword = \"test0233\";\n" +
            "        String getTokenParams = JSONObject.toJSONString(getTokenRequest);\n" +
            "        // send request\n" +
            "        GetTokenResponse getTokenResponse = sendPostWebRequest(GetTokenResponse.class,\n" +
            "                url, getTokenParams, null);\n" +
            "\n" +
            "        /*getData*/\n" +
            "        url = \"{api_prd_address}/datagovernance/datacheckapi/getData\";\n" +
            "        // set request parameters\n" +
            "        GetDataRequest getDataRequest = new GetDataRequest();\n" +
            "        getDataRequest.apiCode = \"c5bbe530bc0b47e6bfbd256364270365\";\n" +
            "        HashMap<String, Object> data = new HashMap<>();\n" +
            "        data.put(\"product_colour\", \"blue\");\n" +
            "        data.put(\"product_price\", \"7000\");\n" +
            "        getDataRequest.data = data;\n" +
            "        String getDataParams = JSONObject.toJSONString(getDataRequest);\n" +
            "        // send request\n" +
            "        GetDataResponse getDataResponse = sendPostWebRequest(GetDataResponse.class,\n" +
            "                url, getDataParams, getTokenResponse.data);\n" +
            "    }\n" +
            "\n" +
            "    public static <T> T sendPostWebRequest(Class<T> c, String url,\n" +
            "                                           String parameters, String token)\n" +
            "    {\n" +
            "        T t = null;\n" +
            "        CloseableHttpClient httpClient = HttpClients.createDefault();\n" +
            "        CloseableHttpResponse response = null;\n" +
            "        String resultString = \"\";\n" +
            "        try {\n" +
            "            HttpPost httpPost = new HttpPost(url);\n" +
            "            httpPost.setHeader(\"Content-Type\", \"application/json;charset=utf-8\");\n" +
            "            httpPost.setHeader(\"Accept\", \"application/json;charset=utf-8\");\n" +
            "            if (token != null && token != \"\") {\n" +
            "                httpPost.setHeader(\"Authorization\", token);\n" +
            "            }\n" +
            "            RequestConfig config = RequestConfig.custom().\n" +
            "                    setConnectTimeout(35000).setConnectionRequestTimeout(35000).\n" +
            "                    setSocketTimeout(60000).build();\n" +
            "            httpPost.setConfig(config);\n" +
            "\n" +
            "            StringEntity entity = new StringEntity(parameters, \"UTF-8\");\n" +
            "            httpPost.setEntity(entity);\n" +
            "            response = httpClient.execute(httpPost);\n" +
            "            resultString = EntityUtils.toString(response.getEntity(), \"utf-8\");\n" +
            "        } catch (Exception e) {\n" +
            "            e.printStackTrace();\n" +
            "        } finally {\n" +
            "            try {\n" +
            "                if (response!=null){\n" +
            "                    response.close();\n" +
            "                }\n" +
            "            } catch (IOException e) {\n" +
            "                e.printStackTrace();\n" +
            "            }\n" +
            "        }\n" +
            "        if (resultString != null && resultString != \"\") {\n" +
            "            t = JSONObject.parseObject(resultString, c);\n" +
            "        }\n" +
            "        return t;\n" +
            "    }\n" +
            "\n" +
            "    public class GetTokenRequest\n" +
            "    {\n" +
            "        public String appAccount;\n" +
            "\n" +
            "        public String appPassword;\n" +
            "    }\n" +
            "\n" +
            "    public static class GetTokenResponse\n" +
            "    {\n" +
            "        public int code;\n" +
            "\n" +
            "        public String msg;\n" +
            "\n" +
            "        public String data;\n" +
            "    }\n" +
            "\n" +
            "    public class GetDataRequest\n" +
            "    {\n" +
            "        public String apiCode;\n" +
            "\n" +
            "        public HashMap<String, Object> data;\n" +
            "    }\n" +
            "\n" +
            "    public static class GetDataResponse\n" +
            "    {\n" +
            "        public int code;\n" +
            "\n" +
            "        public String msg;\n" +
            "\n" +
            "        public Data data;\n" +
            "    }\n" +
            "\n" +
            "    public static class Data\n" +
            "    {\n" +
            "        public List<DataArrayItem> data;\n" +
            "    }\n" +
            "\n" +
            "    public static class DataArrayItem\n" +
            "    {\n" +
            "        public String product_name;\n" +
            "\n" +
            "        public String product_price;\n" +
            "    }\n" +
            "}\n";
}
