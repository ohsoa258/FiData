#!/bin/bash

# Linux系统发布脚本
# lxy 更新于 2023-08-31
# 版本V3.0

echo -e "\e[;31m
███████╗██╗██████╗  █████╗ ████████╗ █████╗       ██████╗ ███████╗
██╔════╝██║██╔══██╗██╔══██╗╚══██╔══╝██╔══██╗      ██╔══██╗██╔════╝
█████╗  ██║██║  ██║███████║   ██║   ███████║█████╗██████╔╝███████╗
██╔══╝  ██║██║  ██║██╔══██║   ██║   ██╔══██║╚════╝██╔══██╗╚════██║
██║     ██║██████╔╝██║  ██║   ██║   ██║  ██║      ██████╔╝███████║
╚═╝     ╚═╝╚═════╝ ╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝      ╚═════╝ ╚══════╝
                                                                \e[0m"

# 设置颜色样式 黑色 = 30、红色 = 31、绿色 = 32、黄色 = 33、蓝色 = 34、洋红色 = 35、青色 = 36 和白色 = 37。
BOLD_RED="\e[1;31m"
BOLD_YELLOW="\e[1;33m"
BOLD_GREEN="\e[1;32m"
BOLD_BLUE="\e[1;34m"
RESET="\e[0m"

# 演示环境
# 项目基地址
BASE_DIR=$(pwd)
BASE_IP=$(hostname -I | awk '{print $1}')
BASE_profile="env"
# ========================================================环境配置块============================================================
case ${BASE_IP} in
        192.168.21.21)
                # fisk演示环境
                BASE_profile="demo"
                # 创建关联数组，将 ID 映射到 jar_name 和 port
                declare -A SERVICE_INFO
                SERVICE_INFO[1]="fisk-api-serveice:8097"
                SERVICE_INFO[2]="fisk-consume-serveice:8091"
                SERVICE_INFO[3]="fisk-consume-visual:8088"
                SERVICE_INFO[4]="fisk-datamanage-center:8095"
                SERVICE_INFO[5]="fisk-factory-access:8089"
                SERVICE_INFO[6]="fisk-factory-dispatch:8092"
                SERVICE_INFO[7]="fisk-factory-model:8090"
                SERVICE_INFO[8]="fisk-framework-authorization:8087"
                SERVICE_INFO[9]="fisk-framework-gateway:8083"
                SERVICE_INFO[10]="fisk-framework-registry:8082"
                SERVICE_INFO[11]="fisk-governance-center:8093"
                SERVICE_INFO[12]="fisk-license-registry:8098"
                SERVICE_INFO[13]="fisk-mdm-model:9001"
                SERVICE_INFO[14]="fisk-system-center:8085"
                SERVICE_INFO[15]="fisk-task-center:8099"      
            ;;
        192.168.11.130)
                # fisk开发环境
                BASE_profile="dev"
                # 创建关联数组，将 ID 映射到 jar_name 和 port
                declare -A SERVICE_INFO
                SERVICE_INFO[1]="fisk-api-serveice:8097"
                SERVICE_INFO[2]="fisk-consume-serveice:8091"
                SERVICE_INFO[3]="fisk-consume-visual:8088"
                SERVICE_INFO[4]="fisk-datamanage-center:8095"
                SERVICE_INFO[5]="fisk-factory-access:8089"
                SERVICE_INFO[6]="fisk-factory-dispatch:8092"
                SERVICE_INFO[7]="fisk-factory-model:8090"
                SERVICE_INFO[8]="fisk-framework-authorization:8087"
                SERVICE_INFO[9]="fisk-framework-gateway:8083"
                SERVICE_INFO[10]="fisk-framework-registry:8082"
                SERVICE_INFO[11]="fisk-governance-center:8093"
                SERVICE_INFO[12]="fisk-license-registry:8098"
                SERVICE_INFO[13]="fisk-mdm-model:9001"
                SERVICE_INFO[14]="fisk-system-center:8085"
                SERVICE_INFO[15]="fisk-task-center:8099"
                ;;
        192.168.21.13)
                # fisk UAT环境
                BASE_profile="uat"
                declare -A SERVICE_INFO
                SERVICE_INFO[1]="fisk-api-serveice:8097"
                SERVICE_INFO[2]="fisk-consume-serveice:8091"
                SERVICE_INFO[3]="fisk-consume-visual:8088"
                SERVICE_INFO[4]="fisk-datamanage-center:8095"
                SERVICE_INFO[5]="fisk-factory-access:8089"
                SERVICE_INFO[6]="fisk-factory-dispatch:8092"
                SERVICE_INFO[7]="fisk-factory-model:8090"
                SERVICE_INFO[8]="fisk-framework-authorization:8087"
                SERVICE_INFO[9]="fisk-framework-gateway:8083"
                SERVICE_INFO[10]="fisk-framework-registry:8082"
                SERVICE_INFO[11]="fisk-governance-center:8093"
                SERVICE_INFO[12]="fisk-license-registry:8098"
                SERVICE_INFO[13]="fisk-mdm-model:9001"
                SERVICE_INFO[14]="fisk-system-center:8085"
                SERVICE_INFO[15]="fisk-task-center:8099"
            ;;
        100.102.201.106)
                # fisk 浦东惠南
                BASE_profile="pdhn"
                declare -A SERVICE_INFO
                SERVICE_INFO[1]="fisk-api-serveice:"
                SERVICE_INFO[2]="fisk-consume-serveice:30009"
                SERVICE_INFO[3]="fisk-consume-visual:"
                SERVICE_INFO[4]="fisk-datamanage-center:"
                SERVICE_INFO[5]="fisk-factory-access:30007"
                SERVICE_INFO[6]="fisk-factory-dispatch:30008"
                SERVICE_INFO[7]="fisk-factory-model:"
                SERVICE_INFO[8]="fisk-framework-authorization:30003"
                SERVICE_INFO[9]="fisk-framework-gateway:30004"
                SERVICE_INFO[10]="fisk-framework-registry:30002"
                SERVICE_INFO[11]="fisk-governance-center:30010"
                SERVICE_INFO[12]="fisk-license-registry:"
                SERVICE_INFO[13]="fisk-mdm-model:"
                SERVICE_INFO[14]="fisk-system-center:30006"
                SERVICE_INFO[15]="fisk-task-center:30005"
            ;;
        192.168.1.18)
                # fisk 升达
                BASE_profile="sd"
                declare -A SERVICE_INFO
                SERVICE_INFO[1]="fisk-api-serveice:"
                SERVICE_INFO[2]="fisk-consume-serveice:7008"
                SERVICE_INFO[3]="fisk-consume-visual:"
                SERVICE_INFO[4]="fisk-datamanage-center:"
                SERVICE_INFO[5]="fisk-factory-access:7005"
                SERVICE_INFO[6]="fisk-factory-dispatch:7007"
                SERVICE_INFO[7]="fisk-factory-model:7010"
                SERVICE_INFO[8]="fisk-framework-authorization:7001"
                SERVICE_INFO[9]="fisk-framework-gateway:7002"
                SERVICE_INFO[10]="fisk-framework-registry:7000"
                SERVICE_INFO[11]="fisk-governance-center:"
                SERVICE_INFO[12]="fisk-license-registry:"
                SERVICE_INFO[13]="fisk-mdm-model:"
                SERVICE_INFO[14]="fisk-system-center:7004"
                SERVICE_INFO[15]="fisk-task-center:7003"
            ;;
        192.168.1.18)
                # fisk 延锋生产V2版本
                BASE_profile="yfv2"
                declare -A SERVICE_INFO
                SERVICE_INFO[1]="fisk-api-serveice:"
                SERVICE_INFO[2]="fisk-consume-serveice:7008"
                SERVICE_INFO[3]="fisk-consume-visual:"
                SERVICE_INFO[4]="fisk-datamanage-center:"
                SERVICE_INFO[5]="fisk-factory-access:7005"
                SERVICE_INFO[6]="fisk-factory-dispatch:7007"
                SERVICE_INFO[7]="fisk-factory-model:7010"
                SERVICE_INFO[8]="fisk-framework-authorization:7001"
                SERVICE_INFO[9]="fisk-framework-gateway:7002"
                SERVICE_INFO[10]="fisk-framework-registry:7000"
                SERVICE_INFO[11]="fisk-governance-center:"
                SERVICE_INFO[12]="fisk-license-registry:"
                SERVICE_INFO[13]="fisk-mdm-model:"
                SERVICE_INFO[14]="fisk-system-center:7004"
                SERVICE_INFO[15]="fisk-task-center:7003"
            ;;
        10.10.64.193)
                # fisk 康师傅
                BASE_profile="ksf"
                declare -A SERVICE_INFO
                SERVICE_INFO[1]="fisk-api-serveice:"
                SERVICE_INFO[2]="fisk-consume-serveice:7010"
                SERVICE_INFO[3]="fisk-consume-visual:"
                SERVICE_INFO[4]="fisk-datamanage-center:"
                SERVICE_INFO[5]="fisk-factory-access:7007"
                SERVICE_INFO[6]="fisk-factory-dispatch:7008"
                SERVICE_INFO[7]="fisk-factory-model:7009"
                SERVICE_INFO[8]="fisk-framework-authorization:7003"
                SERVICE_INFO[9]="fisk-framework-gateway:7002"
                SERVICE_INFO[10]="fisk-framework-registry:7004"
                SERVICE_INFO[11]="fisk-governance-center:"
                SERVICE_INFO[12]="fisk-license-registry:"
                SERVICE_INFO[13]="fisk-mdm-model:"
                SERVICE_INFO[14]="fisk-system-center:7006"
                SERVICE_INFO[15]="fisk-task-center:7005"
            ;;
        9)
            ;;
        10)
            ;;
        11)
            ;;
        *)
            echo -e "${BOLD_RED}当前ip：$BASE_IP 未找到对应的环境参数${RESET}"
            ;;
esac
# ========================================================环境配置块============================================================
echo -e "${BOLD_BLUE}后端项目部署脚本${RESET}"
echo -e "${BOLD_BLUE}当前路径是：$BASE_DIR 请确认本路径是否为项目基地址${RESET}"
echo -e "${BOLD_BLUE}当前IP是：$BASE_IP 请确认环境是否为项服务器IP${RESET}"
echo -e "${BOLD_BLUE}当前环境是：$BASE_profile 请确认环境是否为项目环境${RESET}"



# 备份目录
# BACKUP_DIR="/root/java/bs"

# 服务列表
SERVICES=("fisk-api-serveice" "fisk-consume-serveice" "fisk-consume-visual" "fisk-data-management"
          "fisk-factory-access" "fisk-factory-dispatch" "fisk-factory-model" "fisk-framework-authorization"
          "fisk-framework-gateway" "fisk-framework-registry" "fisk-governance-center" "fisk-license-registry"
          "fisk-mdm-model" "fisk-system-center" "fisk-task-center")

SERVICE_INFO_OTHER[1]="zookeeper:2181"
SERVICE_INFO_OTHER[2]="kafka:9092"
SERVICE_INFO_OTHER[3]="nifi:8443"
SERVICE_INFO_OTHER[4]="redis:6379"


# 函数：备份 jar 包
backup_jar() {
    local jar_name="$1"
    local bakjarid="$2"
    
    # 定义备份路径
    local BACKUP_DIR="$BASE_DIR/$jar_name-web/bak/$(date +%Y%m%d)"
    
    # 创建备份目录
    mkdir -p "$BACKUP_DIR"
    
    echo -e "\e[;34m 开始备份 $jar_name 的jar包文件到 $BACKUP_DIR\e[0m"
    case "$bakjarid" in
        [1-9]|1[0-5]) # 匹配1到15之间的数字
        jar_name="${SERVICES[bakjarid - 1]}"
        if cp -r "$BASE_DIR/$jar_name-web/$jar_name.jar" "$BACKUP_DIR/"; then
            echo "|已备份 $jar_name 到 $BACKUP_DIR|"
            echo -e "\e[;32m 备份成功\e[0m"
        else
            echo -e "\e[;31m 备份失败\e[0m"
        fi
        ;;
    *)
        echo -e "\e[;31m 备份编号无效\e[0m"
        ;;
    esac
}

# 函数：检查端口是否启动成功
check_port() {
    local port="$1"
    local max_attempts=11
    local attempts=0
    local interval=3
    ip="localhost"
    mark=''
    ratio=0
    echo -e "正在等待端口 $port 启动..."
    ratio=0;    
    while [[ $attempts -lt $max_attempts ]]; do
        if timeout 1 bash -c "</dev/tcp/$ip/$port" >/dev/null 2>&1; then
            ratio=100
            mark="####################################################################################################"
            printf "等待程序启动:[%-100s]%d%%\r" "${mark}" "${ratio}"
            sleep 1
            # echo -e "端口 $port 已成功启动"
            echo
            return 0
        fi        
        sleep $interval
        printf "等待程序重启:[%-100s]%d%%\r" "${mark}" "${ratio}"
        mark="##########${mark}"   
        attempts=$((attempts + 1))
        ratio=$((ratio+10))
    done
    echo
    echo -e "${BOLD_RED}等待超时，端口 $port 未能成功启动${RESET}"
    return 1
}

# 函数：检查端口是否启动成功
check_port_status() {
    local port="$1"
    ip="$BASE_IP"
    if timeout 1 bash -c "</dev/tcp/$ip/$port" >/dev/null 2>&1; then
        echo -e "服务状态：  \e[;32m up\e[0m" # 端口存在，输出绿色的 "up"
    else
        echo -e "服务状态：  \e[;31m down\e[0m" # 端口不存在，输出红色的 "down"
    fi
}


# 函数：启动jar包服务
start_jar_service() {
    local service_id="$1"
    local service_info=("${SERVICE_INFO[$service_id]}")
    local jar_name="${service_info%%:*}" # 从 service_info 中提取 jar_name
    local port="${service_info##*:}"      # 从 service_info 中提取 port
    local profile="$BASE_profile"  # 请根据需要设置适当的profile
    ip="localhost"
    if timeout 1 bash -c "</dev/tcp/$ip/$port" >/dev/null 2>&1; then
        echo -e "${BOLD_YELLOW}端口 $port 已经在运行，无需重复启动${RESET}"
    else
        echo -e "${BOLD_YELLOW}开始启动 $jar_name 服务${RESET}"
        echo -e "正在拼接启动命令..."
        echo "nohup java -jar -Dloader.path="${BASE_DIR}/${jar_name}-web/${jar_name}-web-libs" -Dspring.profiles.active="${profile}" -Dserver.port="${port}" -jar "${BASE_DIR}/${jar_name}-web/${jar_name}.jar" >/dev/null 2>&1 &"
        echo -e "正在启动..."
        nohup java -jar -Dloader.path="${BASE_DIR}/${jar_name}-web/${jar_name}-web-libs" -Dspring.profiles.active="${profile}" -Dserver.port="${port}" -jar "${BASE_DIR}/${jar_name}-web/${jar_name}.jar" >/dev/null 2>&1 &
        
        # 等待端口启动
        if check_port "$port"; then
            echo -e "${BOLD_GREEN}端口 $port 服务 $jar_name 成功启动${RESET}"
        else
            echo -e "${BOLD_RED}端口 $port 服务 $jar_name 启动失败${RESET}"
            echo -e "${BOLD_BLUE}正在获取最后100条启动日志...${RESET}"
            echo -e "${BOLD_BLUE}正在拼接命令...${RESET}"
            case ${jar_name} in
        fisk-api-serveice)
                jar_name_log="fisk-api-serveice"
                jar_name_log_txt="fisk-api-serveice.log"     
            ;;
        fisk-consume-serveice)
                jar_name_log="fisk-consume-serveice"
                jar_name_log_txt="fisk-consume-serveice.log"     
            ;;
        fisk-consume-visual)
                jar_name_log="consumeVisual"
                jar_name_log_txt="chartVisual.log"     
            ;;
        fisk-datamanage-center)
                jar_name_log="datamanagement"
                jar_name_log_txt="datamanagement.log"     
            ;;
        fisk-factory-dispatch)
                jar_name_log="dispatch"
                jar_name_log_txt="factoryDispatch.log"     
            ;;
        fisk-factory-model)
                jar_name_log="datamodel"
                jar_name_log_txt="datamodel.log"     
            ;;
        fisk-framework-authorization)
                jar_name_log="auth"
                jar_name_log_txt="auth.log"     
            ;;
        fisk-framework-gateway)
                jar_name_log="gateway"
                jar_name_log_txt="gateway.log"     
            ;;
        fisk-framework-registry)
                jar_name_log="registry"
                jar_name_log_txt="registry.log"     
            ;;
        fisk-governance-center)
                jar_name_log="governance"
                jar_name_log_txt="governance.log"     
            ;;
        fisk-license-registry)
                jar_name_log="fisk-license-registry"
                jar_name_log_txt="fisk-license-registry.log"     
            ;;
        fisk-mdm-model)
                jar_name_log="mdm"
                jar_name_log_txt="mdmModel.log"     
            ;;
        fisk-system-center)
                jar_name_log="systemCenter"
                jar_name_log_txt="system.log"     
            ;;
        fisk-task-center)
                jar_name_log="task"
                jar_name_log_txt="task.log"     
            ;;
        fisk-factory-access)
                jar_name_log="factoryAccess"
                jar_name_log_txt="factoryAccess.log"     
            ;;     
        *)
            echo -e "${BOLD_RED}当前jar：$jar_name 未找到对应的日志文件${RESET}"
            ;;
esac
            echo "tail -fn100 ${BASE_DIR}/logs/${jar_name_log}/${jar_name_log_txt}"
            mark=''
            for ((ratio=0;${ratio}<=100;ratio+=10))
            do
                    sleep 0.5
                    printf "加载日志文件:[%-100s]%d%%\r" "${mark}" "${ratio}"
                    mark="##########${mark}"
            done
            echo
            tail -fn100 ${BASE_DIR}/logs/${jar_name_log}/${jar_name_log_txt}
        fi
    fi
}

# 函数：重启jar包服务
restart_jar_service() {
    local service_id="$1"
    local service_info=("${SERVICE_INFO[$service_id]}")
    local jar_name="${service_info%%:*}" # 从 service_info 中提取 jar_name
    local port="${service_info##*:}"      # 从 service_info 中提取 port
    ip="localhost"
    if timeout 1 bash -c "</dev/tcp/$ip/$port" >/dev/null 2>&1; then
        echo -e "${BOLD_YELLOW}端口 $port 正在运行${RESET}"
        echo -e "${BOLD_YELLOW}开始重启 $jar_name 服务${RESET}"
        pkill -f "$jar_name" >/dev/null 2>&1
        # sleep 5  # 等待停止
        mark=''
        for ((ratio=0;${ratio}<=100;ratio+=4))
        do
            if timeout 1 bash -c "</dev/tcp/$ip/$port" >/dev/null 2>&1; then
                sleep 0.5
                printf "等待停止程序:[%-100s]%d%%\r" "${mark}" "${ratio}"
                mark="####${mark}"
            else
            mark="####################################################################################################"
            printf "等待停止程序:[%-100s]%d%%\r" "${mark}" "${ratio}"  
            fi         
        done
        echo
        start_jar_service "$service_id"  # 启动服务
    else
        echo -e "${BOLD_RED}端口 $port 未启动${RESET}"
        start_jar_service "$service_id"  # 启动服务
    fi    
    
}

while true; do
    echo -e "\e[;33m 请选择需要操作的内容：\e[0m"
    echo -e "\e[;33m 1.备份单个文件   2.备份全部文件   3.重启某个服务   4.重启所有服务   5.查询本机jar包服务状态  \e[;36m 6.退出此程序\e[0m"  
    echo -e "\e[;33m 7.检查本机配套服务状态   8.启动系统监控脚本   9.启动服务监控脚本守护进程  10.启动dotnet即席查询chatGpt\e[0m"
    read -p "输入需要的操作: " id
    case ${id} in
        1)
            echo -e "\e[;33m 开始执行 $id 备份单个jar包\e[0m"
            echo -e "${BOLD_YELLOW}请选择要备份的服务：${RESET}"
            for ((i = 0; i < ${#SERVICES[@]}; i++)); do
                echo "$((i + 1)). ${SERVICES[i]}"
            done
            read -p "输入需要备份的服务编号: " bakjarid
            if [[ $bakjarid -ge 1 && $bakjarid -le ${#SERVICES[@]} ]]; then
                jar_name="${SERVICES[bakjarid - 1]}"
                backup_jar "$jar_name" "$bakjarid"
            else
                echo -e "${BOLD_RED}输入无效的服务编号${RESET}"
            fi
            ;;
        2)
            echo -e "\e[;33m 开始执行 $id 备份全部jar包\e[0m"
            for ((i = 1; i <= 15; i++)); do
                jar_name="${SERVICE_INFO[$i]%%:*}"
                backup_jar "$jar_name" "$i"
            done
            ;;
        3)
            echo -e "${BOLD_YELLOW}开始执行重启某个jar包服务${RESET}"
            echo -e "${BOLD_YELLOW}请选择要重启的服务：${RESET}"
            for ((i = 0; i < ${#SERVICES[@]}; i++)); do
                echo "$((i + 1)). ${SERVICES[i]}"
            done
            read -p "输入需要重启的服务编号: " restartjarid
            
            if [[ $restartjarid -ge 1 && $restartjarid -le ${#SERVICES[@]} ]]; then
                jar_name="${SERVICES[restartjarid - 1]}"
                restart_jar_service "$restartjarid"
            else
                echo -e "${BOLD_RED}输入无效的服务编号${RESET}"
            fi
            ;;
        4)
            read -r -p "确认重启/部署所有的jar包吗? [Y/n] " input
            case $input in
                [yY][eE][sS]|[yY])
                    echo "Yes"
                    echo -e "${BOLD_YELLOW}开始执行重启所有服务${RESET}"
                    for ((i = 1; i <= 15; i++)); do
                        jar_name="${SERVICE_INFO[$i]%%:*}"
                        port="${SERVICE_INFO[$i]##*:}"
                        echo "$jar_name and $port"
                        if [ -n "$port" ]; then
                            restart_jar_service "$i"
                        else
                            echo "$jar_name 未部署"
                        fi
                    done
                    ;;
                [nN][oO]|[nN])
                    echo "No"
                       ;;
                *)
                    echo "请正确输入..."
                    ;;
            esac
            ;;
        5)
            for ((i = 1; i <= 15; i++)); do
                service_info="${SERVICE_INFO[$i]}"
                IFS=':' read -ra parts <<< "$service_info"
                jar_name="${parts[0]}"
                port="${parts[1]}"
                
                echo -n "服务 $i: "
                echo "--------------------------------"
                echo "Jar 名称: $jar_name"
                echo "端口号: $port"
                check_port_status "$port"
            done
            ;;
        6)
            echo -e "${BOLD_GREEN}bye${RESET}"
            break
            ;;
        7)
            for ((i = 1; i <= 4; i++)); do
                service_info_other="${SERVICE_INFO_OTHER[$i]}"
                IFS=':' read -ra parts <<< "$service_info_other"
                jar_name="${parts[0]}"
                port="${parts[1]}"
                
                echo -n "服务 $i: "
                echo "--------------------------------"
                echo "Jar 名称: $jar_name"
                echo "端口号: $port"
                check_port_status "$port"
            done
            ;;
        8)
            echo -e "${BOLD_YELLOW}启动服务监控脚本守护进程${RESET}"
            echo -e "${BOLD_YELLOW}检查服务监控脚本运行状态...${RESET}"
            # 检查脚本是否正在运行
            if pgrep -f "server_monitor_daemons.sh" > /dev/null; then
              echo "脚本 server_monitor_daemons.sh 正在运行,无需重复启动..."
            else
              echo "脚本 server_monitor_daemons.sh 未运行，正在启动..."
              nohup ${BASE_DIR}/monitor/server_monitor_daemons.sh >/dev/null 2>&1 &
            fi
            ;;
        9)
            echo -e "${BOLD_YELLOW}启动系统监控脚本${RESET}"
            echo -e "${BOLD_YELLOW}检查系统监控脚本运行状态...${RESET}"
            # 检查脚本是否正在运行
            if pgrep -f "system_monitor_linux.sh" > /dev/null; then
              echo "脚本 system_monitor_linux.sh 正在运行,无需重复启动..."
            else
              echo "脚本 system_monitor_linux.sh 未运行，正在启动..."
              nohup ${BASE_DIR}/monitor/system_monitor_linux.sh >/dev/null 2>&1 &
            fi
            ;;
        10)
            echo -e "${BOLD_YELLOW}启动即席查询...${RESET}"
              echo "脚本 system_monitor_linux.sh 未运行，正在启动..."
              nohup dotnet ${BASE_DIR}/chatGpt/semantic-kernel.dll --urls=http://*:9093 >log.txt &
              if check_port "9093"; then
                    echo -e "${BOLD_GREEN}端口 9093 服务 即席查询 成功启动${RESET}"
                else
                    echo -e "${BOLD_RED}端口 9093 服务 即席查询 启动失败${RESET}"
            fi
            ;;
        *)
            echo -e "${BOLD_RED}输入无效的选项${RESET}"
            ;;
    esac
done
