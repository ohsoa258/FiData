#!/bin/bash

# Linux部署新环境脚本 [FIDATA-BS]
# 演示
# lxy 更新于 2023 08 31


echo -e "\e[字背景颜色;34m
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
echo -e "${BOLD_BLUE}前端项目部署脚本${RESET}"
echo -e "${BOLD_BLUE}当前路径是：$BASE_DIR 请确认本路径是否为项目基地址${RESET}"
echo -e "${BOLD_BLUE}当前IP是：$BASE_IP 请确认环境是否为项服务器IP${RESET}"
echo -e "${BOLD_BLUE}当前环境是：$BASE_profile 请确认环境是否为项目环境${RESET}"

echo -e "${BOLD_BLUE}开始部署新环境${RESET}"


# 指定软件包的安装路径
INSTALL_DIR=$BASE_DIR

# 创建安装目录（如果不存在）
mkdir -p $INSTALL_DIR

echo -e "${BOLD_YELLOW}开始创建文件夹${RESET}"
# 创建基地子所再的文件夹
mkdir -p $BASE_DIR/gcc
mkdir -p $BASE_DIR/jdk
mkdir -p $BASE_DIR/kafka
mkdir -p $BASE_DIR/mysql
mkdir -p $BASE_DIR/nginx
mkdir -p $BASE_DIR/nifi
mkdir -p $BASE_DIR/redis
mkdir -p $BASE_DIR/zookeeper
ls


# 定义函数来检查命令是否已安装
check_command() {
    command -v $1 >/dev/null 2>&1
}

# 函数：检查端口是否被占用
check_port_in_use() {
    local port=$1
    if [[ $(netstat -tuln | grep ":$port ") ]]; then
        return 0  # 端口被占用
    else
        return 1  # 端口未被占用
    fi
}

# 安装 gcc
if  check_command "gcc"; then
    echo -e "${BOLD_GREEN}已安装... gcc${RESET}"
    gcc_version=$(gcc --version | grep -oP '(?<=gcc \(GCC\) )[^ ]+')
    echo -e "${BOLD_GREEN}已安装 GCC${RESET}"
    echo "GCC 版本：$gcc_version"
    read -p "建议安装指定版本 gcc，是否卸载现有版本？ (y/n): " remove_gcc
    if [ "$remove_gcc" == "y" ]; then
        sudo yum remove gcc
    fi
fi

if ! check_command "gcc"; then
    read -p "未检测到 gcc，是否安装？ (y/n): " install_gcc
    if [ "$install_gcc" == "y" ]; then
        output1="gcc "
        echo -e "${BOLD_YELLOW}安装 gcc...${RESET}"
        mkdir "${BASE_DIR}/gcc"
        cd "${BASE_DIR}/gcc"
        echo "成功创建文件夹 gcc"
        # 尝试解压缩 gcc.tar.gz
        if sudo tar -xzvf "${BASE_DIR}/soft/gcc.tar.gz" -C "${BASE_DIR}"; then
            echo -e "${BOLD_GREEN}已解压... gcc${RESET}"
            # 尝试安装 gcc rpm 包
            if sudo rpm -Uvh *.rpm --nodeps --force; then
                echo -e "${BOLD_GREEN}已安装... gcc${RESET}"
                gcc_version=$(gcc --version | grep -oP '(?<=gcc \(GCC\) )[^ ]+')
                echo -e "${BOLD_GREEN}已安装 GCC${RESET}"
                echo "GCC 版本：$gcc_version"
            else
                echo -e "${BOLD_RED}安装 gcc 失败${RESET}"
            fi
        else
            echo -e "${BOLD_RED}解压 gcc 失败${RESET}"
        fi
    else
        echo -e "${BOLD_YELLOW}跳过安装 GCC${RESET}"
    fi
else
    gcc_version=$(gcc --version | grep -oP '(?<=gcc \(GCC\) )[^ ]+')
    echo -e "${BOLD_GREEN}已安装 GCC${RESET}"
    echo "GCC 版本：$gcc_version"
fi


# 安装 JDK
# 设置 Java 安装路径
java_home="${BASE_DIR}/jdk"
if ! check_command "java"; then
    read -p "未检测到 Java，是否安装 JDK？ (y/n): " install_java
    if [ "$install_java" == "y" ]; then
        output1+="jdk "
        echo -e "${BOLD_GREEN}安装 JDK...${RESET}"
        
        # 创建文件夹并拷贝 JDK 安装文件
        mkdir -p "$java_home"
        cp "${BASE_DIR}/soft/jdk-8u202-linux-x64.rpm" "$java_home"

        # 下载并安装 JDK
        cd "$java_home"
        sudo rpm -ivh jdk-8u202-linux-x64.rpm
        echo -e "${BOLD_GREEN}安装路径 /usr/java/jdk1.8.0_202-amd64${RESET}"
        # 验证安装是否成功
        if check_command "java"; then
            # 检查 Java 版本
            java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
            echo -e "${BOLD_GREEN}已安装 JDK${RESET}"
            echo "Java 版本：$java_version"
        else
            echo -e "${BOLD_RED}安装 JDK 失败${RESET}"
            exit 1
        fi
    else
        echo -e "${BOLD_YELLOW}跳过安装 JDK${RESET}"
    fi
else
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo -e "${BOLD_GREEN}已安装 JDK${RESET}"
    echo "JDK 版本：$java_version"
fi

# 安装 Nginx
read -p "请确认是否Nginx，是否安装？ (y/n): " install_nginx
if [ "$install_nginx" == "y" ]; then
    output1+="nginx "
    echo -e "${BOLD_GREEN}安装 Nginx...${RESET}"
    mkdir -p "${BASE_DIR}/nginx"
    tar -xzvf ${BASE_DIR}/soft/nginx-1.20.1.tar.gz -C ${BASE_DIR}/nginx
    cd ${BASE_DIR}/nginx/nginx-1.20.1
    ./configure
    make
    make install
    echo -e "${BOLD_GREEN}安装完成 Nginx. 安装路径 /usr/local/nginx/sbin ${RESET}"
    cd ${BASE_DIR}
fi

# 安装kafka
if ! check_port_in_use 9092; then
    echo -e "${BOLD_YELLOW}9092未被占用...${RESET}"
else
    echo -e "${BOLD_RED}9092已被占用...请检查相关端口对应的服务...${RESET}"
fi
read -p "是否安装 Kafka 服务？ (y/n): " install_kafka
    if [ "$install_kafka" == "y" ]; then
        output1+="kafka "
        echo -e "${BOLD_YELLOW}安装 Kafka 服务...${RESET}"
        tar -xzvf ${BASE_DIR}/soft/kafka_2.11-2.2.1.tgz -C ${BASE_DIR}/kafka
        echo -e "${BOLD_GREEN}安装完成 kafka. 安装路径 ${BASE_DIR}/kafka  ${RESET}"
        cd ${BASE_DIR}
    fi

# 安装 MySQL
if ! check_port_in_use 3306; then
    echo -e "${BOLD_YELLOW}3306未被占用...${RESET}"
else
    echo -e "${BOLD_RED}3306已被占用...请检查相关端口对应的服务...${RESET}"
fi
read -p "是否安装 MySQL 服务？ (y/n): " install_mysql
    if [ "$install_mysql" == "y" ]; then
        output1+="mysql "
        echo -e "${BOLD_YELLOW}安装 MySQL 服务...${RESET}"
        mkdir -p "${BASE_DIR}/mysql"
        cd ${BASE_DIR}/soft
        echo "正在解压...[xz -d mysql-8.0.20-linux-glibc2.12-x86_64.tar.xz]"
        xz -d mysql-8.0.20-linux-glibc2.12-x86_64.tar.xz
        echo "正在解压...[tar -xzvf ${BASE_DIR}/soft/mysql-8.0.20-linux-glibc2.12-x86_64.tar -C ${BASE_DIR}/mysql]"
        tar -xzvf ${BASE_DIR}/soft/mysql-8.0.20-linux-glibc2.12-x86_64.tar -C ${BASE_DIR}/mysql
        echo -e "${BOLD_GREEN}安装完成 mysql 安装路径 ${BASE_DIR}/mysql  ${RESET}"
        echo -e "${BOLD_YELLOW} mysql 初始化请手动操作  ${RESET}"
        cd ${BASE_DIR}
    fi

# 安装 NiFi
if ! check_port_in_use 8443; then
    echo -e "${BOLD_YELLOW}8443未被占用...${RESET}"
else
    echo -e "${BOLD_RED}8443已被占用...请检查相关端口对应的服务...${RESET}"
fi
read -p " 是否安装 NiFi 服务？ (y/n): " install_nifi
    if [ "$install_nifi" == "y" ]; then
        output1+="nifi "
        echo "安装 NiFi..."
        unzip ${BASE_DIR}/soft/nifi-1.17.0-bin.zip -d ${BASE_DIR}/nifi
        echo -e "${BOLD_GREEN}安装 NiFi 成功${RESET}"
        cd ${BASE_DIR}
        echo -e "${BOLD_YELLOW}正在同步nifi驱动...${RESET}"
        sudo cp -r ${BASE_DIR}/soft/opt ${BASE_DIR}/nifi/nifi-1.17.0
        output2=$(ls ${BASE_DIR}/nifi/nifi-1.17.0/opt/nifi/nifi-current/jdbcdriver)
        echo -e "${BOLD_GREEN}同步nifi驱动完成${RESET}"
    fi


# 安装 Redis
if ! check_port_in_use 6379; then
    echo -e "${BOLD_YELLOW}6379未被占用...${RESET}"
else
    echo -e "${BOLD_RED}6379已被占用...请检查相关端口对应的服务...${RESET}"
fi
read -p " 是否安装 Redis 服务？ (y/n): " install_Redis
    if [ "$install_Redis" == "y" ]; then
        output1+="redis "
        echo "解压 Redis..."
        tar -xzf ${BASE_DIR}/soft/redis-6.2.6.tar.gz -C ${BASE_DIR}/redis
        echo "解压 Redis...成功"
        cd ${BASE_DIR}/redis/redis-6.2.6
        make
        make install 
        echo "安装 Redis 成功"
        cd ${BASE_DIR}
    fi


# 安装 ZooKeeper
if ! check_port_in_use 2181; then
    echo "2181未被占用..."
else
    echo "2181已被占用...请检查相关端口对应的服务..."
fi
read -p " 是否安装 zookeeper 服务？ (y/n): " install_ZooKeeper
    if [ "$install_ZooKeeper" == "y" ]; then
        output1+="zookeeper "
        echo "安装 ZooKeeper..."
        tar -xzf ${BASE_DIR}/soft/zookeeper-3.4.5.tar.gz -C ${BASE_DIR}/zookeeper
        cd ${BASE_DIR}/zookeeper/zookeeper-3.4.5/conf
        mv zoo_sample.cfg zoo.cfg
        echo -e "${BOLD_GREEN}安装 ZooKeeper 成功${RESET}"
        cd ${BASE_DIR}
    fi

echo -e "${BOLD_GREEN}安装完成！${RESET}"
echo -e "${BOLD_RED}====================================部署报告====================================${RESET}"
echo -e "${BOLD_RED}本次安装程序: ${RESET}"
echo -e "${BOLD_GREEN}$output1 ${RESET}"
echo -e "${BOLD_RED}gcc版本: ${RESET}"
echo -e "${BOLD_GREEN}$gcc_version ${RESET}"
echo -e "${BOLD_RED}jdk版本: ${RESET}"
echo -e "${BOLD_GREEN}$java_version ${RESET}"
echo -e "${BOLD_RED}导入的nifi驱动程序: ${RESET}"
echo -e "${BOLD_GREEN}$output2 ${RESET}"
echo -e "${BOLD_RED}其他:${RESET}"
echo -e "${BOLD_RED}其他:${RESET}"
echo -e "${BOLD_RED}================================================================================${RESET}"