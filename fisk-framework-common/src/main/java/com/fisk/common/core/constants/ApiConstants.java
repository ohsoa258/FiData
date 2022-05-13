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
            "    \"standard\":\"接口采用HTTP协议，TCP连接方式。数据传输格式采用非加密的JSON格式。API请求方式为POST，文本编码格式为UTF-8；\",\n" +
            "    \"standard_query\":\"暂无。\",\n" +
            "    \"authStandard\":\"第三方系统在访问平台API时需要进行身份验证，通过调用“获取Token”接口，传递账号密码获取Token（60分钟有效期）。\",\n" +
            "    \"uatAddress\":\"{api_uat_address}\",\n" +
//            "    \"prdAddress\":\"生产环境：{api_prd_address}(预留地址,暂不使用)\",\n" +
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
            "            \"company\":\"技术部\",\n" +
            "            \"fullName\":\"徐阳辉\",\n" +
            "            \"mailbox\":\"yhxu@fisksoft.com\",\n" +
            "            \"trStyle\":\"background-color: #fff\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"category\":\"接口负责人\",\n" +
            "            \"company\":\"技术部\",\n" +
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
            "    \"title\":\"一体化数据赋能平台 API接口文档\",\n" +
            "    \"docVersion\":\"文档版本 V1.0\",\n" +
            "    \"isuCompany\":\"\",\n" +
            "    \"isuDate\":\"发布日期：20220101\",\n" +
            "    \"footerName\":\"接口文档\",\n" +
            "    \"docPurpose\":\"本文用于第三方平台对接一体化数据赋能平台。\",\n" +
            "    \"readers\":\"预期读者包括需要从一体化数据赋能平台中获取数据的第三方平台。\",\n" +
            "    \"standard\":\"接口采用HTTP协议，TCP连接方式。数据传输格式采用非加密的JSON格式。API请求方式为POST，文本编码格式为UTF-8；\",\n" +
            "    \"standard_query\":\"查询接口携带分页功能，current页码从第1页开始，size每页数量建议不超过500条，current和size为null默认查询全部。\",\n" +
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
            "            #region Step 1：getToken\n" +
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
            "            #region Step 2：getData\n" +
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
            "                        new StreamWriter(httpRequest.GetRequestStream()))\n" +
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

    public static final String DATASERVICE_APICODEEXAMPLES_JAVA= "\n" +
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
            "        /*Step 1：getToken*/\n" +
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
            "        /*Step 2：getData*/\n" +
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

}
