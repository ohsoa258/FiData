#!/bin/bash

# Linux发布前端脚本 [FIDATA-BS]
# 演示
# lxy 更新于 2023 08 31


echo -e "\e[字背景颜色;32m
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

# 检查bak文件夹是否存在，不存在则创建
if [ ! -d "bak" ]; then
  mkdir bak
fi

# 1. 备份文件夹
backup_folder() {
  local folder_to_backup="$1"
  local backup_folder_name="bak/$(date '+%Y%m%d%H%M%S')"
  cp -r "$folder_to_backup" "$backup_folder_name"
  echo -e "${BOLD_GREEN}备份成功：$folder_to_backup -> $backup_folder_name"
}

# 2. 解压缩zip文件
extract_zip() {
  local zip_file="$1"
  local folder_name="${zip_file%.zip}"
  unzip "$zip_file" -d "$folder_name"
  echo "解压缩成功：$zip_file -> $folder_name"
}

# 3. 删除文件夹
delete_folder() {
  local folder_to_delete="$1"
  rm -r "$folder_to_delete"
  echo -e "删除成功：$folder_to_delete"
}

# 4. 删除所有zip文件
delete_all_zip() {
  find . -type f -name "*.zip" -exec rm -f {} \;
  echo -e "已删除所有zip文件"
}

# 获取目录下的文件夹名称作为选项
# 获取目录下的文件夹名称作为选项（排除"bak"文件夹）
get_folder_options() {
    local dir="$1"
    local options=()
    for folder in "$dir"/*; do
        if [ -d "$folder" ] && [ "$(basename "$folder")" != "bak" ]; then
            options+=("$(basename "$folder")")
        fi
    done
    echo "${options[@]}"
}


while true; do
    echo -e "\e[;33m 前端发布流程：1.备份单个文件》》2.删除原文件夹》》3.解压缩zip文件》》4.删除所有zip文件 \e[;36m 5.退出\e[0m"
    echo -e "\e[;33m 请选择要发布的脚本名称：\e[0m"

    PS3="请输入选项： "
    options=("退出" $(get_folder_options "."))  # 获取当前目录下的文件夹名称作为选项
    select opt in "${options[@]}"; do
        case $opt in
            "退出")
                echo -e "${BOLD_GREEN}bye${RESET}"
                exit 0
                ;;
            *)
                folder_to_backup="$opt" # 选中的文件夹名称
                backup_folder "$folder_to_backup"
                delete_folder "$folder_to_backup"
                echo -e "\e[;33m 解压缩的zip文件路径${folder_to_backup}.zip：\e[0m"
                extract_zip "$folder_to_backup"
                delete_all_zip
                cp -r "./${folder_to_backup}/${folder_to_backup}/." "./${folder_to_backup}/"
                rm -rf "./${folder_to_backup}/${folder_to_backup}"
                ;;
        esac
    done
done