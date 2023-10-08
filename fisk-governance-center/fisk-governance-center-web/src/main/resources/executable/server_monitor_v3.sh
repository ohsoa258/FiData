#!/bin/bash

# failed_connections=()

# while true; do

    ip_ports_str=$(curl -X GET "http://192.168.21.21:8093/systemMonitor/getServerMonitorConfig" -H "accept: */*")
    echo "ip_port返回结果: ${ip_ports_str}"

    # 静态数组
    # ip_ports=("192.168.11.130:8083:fisk-framework-gateway" "192.168.11.130:8085:fisk-system-center" "192.168.11.130:8087:fisk-framework-authorization" "192.168.11.130:8088:fisk-consume-visual" "192.168.11.130:8089:fisk-factory-access" "192.168.11.130:8090:fisk-factory-model" "192.168.11.130:8091:fisk-consume-serveice" "192.168.11.130:8092:fisk-factory-dispatch" "192.168.11.130:8093:fisk-governance-center" "192.168.11.130:8095:fisk-data-management" "192.168.11.130:8097:fisk-api-serveice" "192.168.11.130:8098:fisk-license-registry" "192.168.11.130:8099:fisk-task-center" "192.168.11.130:9001:fisk-mdm-model" "192.168.11.130:2181:zookeeper" "192.168.11.130:9092:kafka" "192.168.11.130:6379:redis" "192.168.11.130:3306:mysql" "192.168.1.92:8443:nifi" "192.168.11.130:8082:fisk-framework-registry" )

    # 使用read命令将字符串转换成数组
    read -ra ip_ports <<< "$ip_ports_str"

    # 遍历数组
    for ip_port in "${ip_ports[@]}"; do
        echo "$ip_port"
    done

    # 构建JSON数组
    json_array="["
    first_element=1

    # 遍历IP地址和端口列表
    for ip_port in "${ip_ports[@]}"; do
        ip=$(echo "$ip_port" | cut -d ':' -f 1)
        port=$(echo "$ip_port" | cut -d ':' -f 2)
        name=$(echo "$ip_port" | cut -d ':' -f 3)

        # 使用timeout命令检查端口是否可连接
        if timeout 1 bash -c "</dev/tcp/$ip/$port" >/dev/null 2>&1; then
            status="1"
            # 从失败的连接中移除已经恢复的服务
            # failed_connections=("${failed_connections[@]/$ip_port}")
        else
            status="2"
            # 检查连接是否已经记录在失败的连接中
            if ! [[ " ${failed_connections[@]} " =~ " ${ip_port} " ]]; then
                # failed_connections+=("$ip_port")
                json_output="{"
                json_output+="\"服务\":\"$name\","
                json_output+="\"运行环境\":\"开发环境\","
                json_output+="\"错误信息\":\"$ip 的 $port 连接异常\""
                json_output+="}"
                echo "$json_output"
                curl -X POST "http://192.168.21.21:8093/systemMonitor/sendSystemMonitorSendEmails" -H "accept: */*" -H "Content-Type: application/json" -d "$json_output"
            fi
        fi

        # 构建JSON对象
        json_object="{\"serverName\":\"$name\",\"serverIp\":\"$ip\",\"serverPort\":\"$port\",\"status\":\"$status\"}"

        # 拼接JSON数组
        if [[ $first_element -eq 1 ]]; then
            json_array+="$json_object"
            first_element=0
        else
            json_array+=",$json_object"
        fi
    done
    echo "-------------------------------------------------------------------------------------------"
    # 结束JSON数组
    json_array+="]"

    echo "$json_array"
    curl -X POST "http://192.168.21.21:8093/systemMonitor/saveServerMonitor" -H "accept: */*" -H "Content-Type: application/json" -d "$json_array"
    echo "===========================================运行成功========================================="
    # for failed_connection in "${failed_connections[@]}"; do
    #     echo "$failed_connection"
    # done
    echo "运行结束"
    #sleep 30
# done
