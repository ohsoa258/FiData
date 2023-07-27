#!/bin/bash

# Linux系统状态收集脚本
# 演示环境
# lxy 更新于 2023 07 24

while true; do
	# 查询操作系统版本
	# os_version=$(uname -a)

	# 查看Linux操作系统ip地址
	ip_addr=$(hostname -I | awk '{print $1}')

	# 查询系统运行时间   1.upTime
	sys_uptime=$(uptime)
	if [[ $sys_uptime =~ up\ +([0-9]+ +[a-zA-Z]+) ]]; then
    	uptime="${BASH_REMATCH[1]}"
    	#echo "系统运行时间：${uptime}"
	else
    	echo "未找到系统运行时间"
	fi

	# 查询CPU核数 2.cpuCores 
	cpu_cores=$(nproc)

	# 查询物理内存
	rawTotal=$(free | grep "Mem:" | awk '{print $2}')


	# 查询交换空间
	swapTotal=$(free | grep "Swap:" | awk '{print $2}')

	# 检查系统CPU使用情况
	cpu_usage=$(top -bn1 | grep "Cpu(s)" | awk '{print $2 + $3}')

	# 查询 rawUsed
	rawUsed=$(free | grep "Mem:" | awk '{print $3}')

	# 查询 swapUsed
	swapUsed=$(free | grep "Swap:" | awk '{print $3}')

	# 检查系统内存使用情况
	# memory_usage=$(free | awk '/Mem/{printf("%.2f"), $3/$2 * 100}')

	# 检查系统磁盘使用情况
	# disk_usage=$(df -h / | awk '/\//{print $5}')

	# 构建JSON格式的输出
	json_output="{"
	# json_output+="\"Operating System Version\":\"$os_version\","
	json_output+="\"ip\":\"$ip_addr\","
	json_output+="\"upTime\":\"$uptime\","
	json_output+="\"cpuCores\":\"$cpu_cores\","
	json_output+="\"rawTotal\":\"$rawTotal\","
	json_output+="\"swapTotal\":\"$swapTotal\","
	json_output+="\"cpuBusy\":\"$cpu_usage\","
	json_output+="\"rawUsed\":\"$rawUsed\","
	json_output+="\"swapUsed\":\"$swapUsed\""
	# json_output+="\"memory_usage\":\"$memory_usage\","
	# json_output+="\"Disk Usage\":\"$disk_usage\","
	# json_output+="\"Network Connections\":\"$network_connections\""
	# json_output+="\"System uptime info\":\"$sys_uptime\""
	json_output+="}"

	# echo "$json_output"
	curl -X POST "http://192.168.11.130:8093/systemMonitor/saveSystemMonitor" -H "accept: */*" -H "Content-Type: application/json" -d "$json_output"

	sleep 3;
done