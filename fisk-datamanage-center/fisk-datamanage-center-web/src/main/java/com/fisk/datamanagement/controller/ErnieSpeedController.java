package com.fisk.datamanagement.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fisk.datamanagement.config.SwaggerConfig;
import com.fisk.datamanagement.config.WenXinConfig;
import com.fisk.datamanagement.dto.baidu.ChatHistory;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: wangjian
 * @Date: 2024-06-04
 * @Description:
 */

//千帆大模型 文心一言
@Api(tags = {SwaggerConfig.ERNIE_SPEED})
@RestController
@RequestMapping("/ErnieSpeed")
@Slf4j
public class ErnieSpeedController {
    @Resource
    private WenXinConfig wenXinConfig;

    //历史对话，需要按照user,assistant
    List<Map<String,String>> messages = new ArrayList<>();

    /**
     * 非流式问答
     * @param chatHistory 用户的问题
     * @return
     * @throws IOException
     */
    @PostMapping("/chat")
    public String chat(@org.springframework.web.bind.annotation.RequestBody ChatHistory chatHistory) throws IOException {
//        ChatHistory chatHistory = JSONObject.parseObject(chatJson, ChatHistory.class);
        String responseJson = null;
        //先获取令牌然后才能访问api
        if (wenXinConfig.flushAccessToken() != null) {
            List<Map<String,String>> chatList = new ArrayList<>();
            HashMap<String, String> chatMap = new HashMap<>();
            if (chatHistory.getHistory() != null){
                chatList = chatHistory.getHistory();
            }
            chatMap.put("role","user");
            chatMap.put("content",chatHistory.getInput());
            chatList.add(chatMap);
            String requestJson = constructRequestJson(1,0.7,1.0,1.0,false,chatList,chatHistory.getRole());
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestJson);
            Request request = new Request.Builder()
                    .url(wenXinConfig.ERNIE_SPEED + "?access_token=" + wenXinConfig.flushAccessToken())
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();
            try {
                responseJson = HTTP_CLIENT.newCall(request).execute().body().string();
                //将回复的内容转为一个JSONObject
                JSONObject responseObject = JSON.parseObject(responseJson);
                //将回复的内容添加到消息中
                HashMap<String, String> assistant = new HashMap<>();
                assistant.put("role","assistant");
                assistant.put("content",responseObject.getString("result"));
                messages.add(assistant);
            } catch (IOException e) {
                log.error("网络有问题");
                return "网络有问题，请稍后重试";
            }
        }
        return responseJson;
    }

    /**
     * 流式回答
     * @return
     */
    @PostMapping("/test2")
    public String test2(String question){
        OkHttpClient client = new OkHttpClient();

        HashMap<String, String> user = new HashMap<>();
        user.put("role","user");
        user.put("content",question);
        messages.add(user);
        String requestJson = constructRequestJson(1,0.7,1.0,1.0,true,messages);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestJson);
        Request request = new Request.Builder()
                .url(wenXinConfig.ERNIE_SPEED + "?access_token=" + wenXinConfig.flushAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        StringBuilder answer = new StringBuilder();
        // 发起异步请求
        try {
            Response response = client.newCall(request).execute();
            // 检查响应是否成功
            if (response.isSuccessful()) {
                // 获取响应流
                try (ResponseBody responseBody = response.body()) {
                    if (responseBody != null) {
                        InputStream inputStream = responseBody.byteStream();
                        // 以流的方式处理响应内容，输出到控制台
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            // 在控制台输出每个数据块
                            System.out.write(buffer, 0, bytesRead);
                            //将结果汇总起来
                            answer.append(new String(buffer, 0, bytesRead));
                        }
                    }
                }
            } else {
                System.out.println("Unexpected code " + response);
            }

        } catch (IOException e) {
            log.error("流式请求出错");
            throw new RuntimeException(e);
        }
        //将回复的内容添加到消息中
        HashMap<String, String> assistant = new HashMap<>();
        assistant.put("role","assistant");
        assistant.put("content","");
        //取出我们需要的内容,也就是result部分
        String[] answerArray = answer.toString().split("data: ");
        for (int i=1;i<answerArray.length;++i) {
            answerArray[i] = answerArray[i].substring(0,answerArray[i].length() - 2);
            assistant.put("content",assistant.get("content") + JSON.parseObject(answerArray[i]).get("result"));
        }
        messages.add(assistant);
        return assistant.get("content");
    }

    /**
     * 这个用来保存用户与服务器之间的连接信息
     */
    private static final Map<Long, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    //创建连接
    @GetMapping(value = "/sse/connect", produces="text/event-stream;charset=UTF-8")
    public SseEmitter sseConnect(Long clientId){
        //已经连接过，直接返回连接
        if (sseEmitterMap.containsKey(clientId)) {
            return sseEmitterMap.get(clientId);
        }
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE); // 设置超时时间，这里设为最大值避免超时
        try {
            // 设置SSE连接的回调
            sseEmitter.onCompletion(() -> {
                System.out.println("SSE连接已关闭，客户端ID: " + clientId);
                // 连接关闭时，从map中移除
                sseEmitterMap.remove(clientId);
            });
            sseEmitter.onError((ex) -> {
                log.error("SSE连接发生错误，客户端ID: {}, 错误信息: {}", clientId, ex.getMessage());
                sseEmitterMap.remove(clientId); // 出错也移除
            });
            // 初始化发送一个打开连接的事件给客户端
            sseEmitter.send(SseEmitter.event().name("OPEN").data("Connection established"));
        } catch (IOException e) {
            log.error("SseEmitter初始化发送事件失败，客户端ID: {}, 错误信息: {}", clientId, e.getMessage());
            sseEmitter.completeWithError(e);
        }
        // 添加到管理Map中
        sseEmitterMap.put(clientId, sseEmitter);
        return sseEmitter;
    }

//发送消息，这里采用异步的方式来进行发送
    /**
     * 用来异步发送消息
     */
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    /**
     * SSE方式向前端发送消息
     * @param clientId
     * @param question
     */
    @PostMapping(value = "/sse/chat")
    public void streamOutputToPage(Long clientId,String question){
        HashMap<String, String> user = new HashMap<>();
        user.put("role","user");
        user.put("content",question);
        messages.add(user);
        //异步发送消息
        executorService.execute(() -> {
            SseEmitter sseEmitter = sseEmitterMap.get(clientId);
            if(sseEmitter == null){
                sseEmitter = sseConnect(clientId);
            }

            OkHttpClient client = new OkHttpClient();

            String requestJson = constructRequestJson(1,0.95,0.8,1.0,true,messages);
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestJson);
            Request request = new Request.Builder()
                    .url(wenXinConfig.ERNIE_SPEED + "?access_token=" + wenXinConfig.flushAccessToken())
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            //将回复的内容添加到消息中
            HashMap<String, String> assistant = new HashMap<>();
            assistant.put("role","assistant");
            assistant.put("content","");

            // 发起异步请求
            try {
                Response response = client.newCall(request).execute();
                // 检查响应是否成功
                if (response.isSuccessful()) {
                    // 获取响应流
                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody != null) {
                            InputStream inputStream = responseBody.byteStream();
                            // 以流的方式处理响应内容，输出到控制台 这里的数组大小一定不能太小，否则会导致接收中文字符的时候产生乱码
                            byte[] buffer = new byte[2048];
                            int bytesRead;
                            StringBuilder temp = new StringBuilder();
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                //TODO:这部分不需要使用\n\n来进行分割了，只需要将缓冲区开的尽可能大即可
                                //消息分割采用标识符 \n\n 来分割 并且需要从后向前找\n\n，因为每条消息分割点的最后才是\n\n
                                temp.append(new String(buffer, 0, bytesRead));
                                String result = "";
                                if(temp.lastIndexOf("\n\n") != -1){
                                    //从6开始 因为有 data: 这个前缀 占了6个字符所以 0 + 6 = 6
                                    JSONObject jsonObject = JSON.parseObject(temp.substring(6, temp.lastIndexOf("\n\n")));
                                    temp = new StringBuilder(temp.substring(temp.lastIndexOf("\n\n") + 2));
                                    if(jsonObject != null && jsonObject.getString("result") != null){
                                        result = jsonObject.getString("result");
                                    }
                                }
                                if(!result.equals("")){
                                    //SSE协议默认是以两个\n换行符为结束标志 需要在进行一次转义才能成功发送给前端
                                    log.info("clientId"+clientId+"输出----:"+result);
                                    sseEmitter.send(SseEmitter.event().data(result.replace("\n","\\n")));
                                    //将结果汇总起来
                                    assistant.put("content",assistant.get("content") + result);
                                }
                            }
                            messages.add(assistant);
                        }
                    }
                } else {
                    System.out.println("Unexpected code " + response);
                }

            } catch (IOException e) {
                log.error("流式请求出错,断开与{}的连接",clientId);
                e.printStackTrace();
                //移除当前的连接
                sseEmitterMap.remove(clientId);
                //移除本次对话的内容
                // 移除最后一条消息
                if (!messages.isEmpty()) {
                    messages.remove(messages.size() - 1);
                }
            }
        });
    }

    /**
     * 构造请求的请求参数
     * @param userId
     * @param temperature
     * @param topP
     * @param penaltyScore
     * @param messages
     * @return
     */
    public String constructRequestJson(Integer userId,
                                       Double temperature,
                                       Double topP,
                                       Double penaltyScore,
                                       boolean stream,
                                       List<Map<String, String>> messages,
                                       String role) {
        Map<String,Object> request = new HashMap<>();
        request.put("user_id",userId.toString());
        request.put("temperature",0.7);
        request.put("top_p",topP);
        request.put("penalty_score",penaltyScore);
        request.put("stream",stream);

        request.put("system","你是一个SQL生成器，只根据提供的数据库表结构和用户请求生成SQL语句。不需要任何额外的解释或说明。生成的SQL语句格式应为:首位加上### 末尾加上 ###。"+role+";字段括号里面的中文是对字段的描述.如果数据库是SQLSERVER类型,请注意在进行字符串值过滤的时候在value的前面要加上N.\n" +
                "Please combine historical dialogue Help me write the corresponding SQL statement.");
        request.put("messages",messages);
        System.out.println(JSON.toJSONString(request));
        return JSON.toJSONString(request);
    }

    /**
     * 构造请求的请求参数
     * @param userId
     * @param temperature
     * @param topP
     * @param penaltyScore
     * @param messages
     * @return
     */
    public String constructRequestJson(Integer userId,
                                       Double temperature,
                                       Double topP,
                                       Double penaltyScore,
                                       boolean stream,
                                       List<Map<String, String>> messages) {
        Map<String,Object> request = new HashMap<>();
        request.put("user_id",userId.toString());
        request.put("temperature",temperature);
        request.put("top_p",topP);
        request.put("penalty_score",penaltyScore);
        request.put("stream",stream);

        request.put("system","你是一个sql数据分析程序，我给你提供数据库表信息，你需要根据该信息直接输出我需要的sql语句，需要强调的是除了sql语句之外，不要输出任何中英文内容，请以### {} ###格式{}替换为sql输出最优的可执行语句.Database Type:SQLSERVER\\nTable Name:sftp_file.safe_days\\nTable Fields:safe_days_number,no_fire_days,start_date,now_date;字段括号里面的中文是对字段的描述.如果数据库是SQLSERVER类型,请注意在进行字符串值过滤的时候在value的前面要加上N.\n" +
                "Please combine historical dialogue Help me write the corresponding SQL statement.");
        request.put("messages",messages);
        System.out.println(JSON.toJSONString(request));
        return JSON.toJSONString(request);
    }
}
