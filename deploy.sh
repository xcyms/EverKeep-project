#!/bin/bash

# 配置项
WORK_DIR="/data"  # 部署工作根目录
ZIP_URL="https://codeload.github.com/xcyms/EverKeep-project/zip/refs/heads/master"
TEMP_ZIP="source.zip"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}==== EverKeep 源码部署工具 (进入目录执行模式) ====${NC}"

# 1. 准备工作目录
mkdir -p $WORK_DIR
cd $WORK_DIR || exit 1

# 2. 下载源码压缩包
echo -e "${GREEN}正在下载最新源码...${NC}"
curl -L -o $TEMP_ZIP $ZIP_URL

if [ $? -ne 0 ]; then
    echo -e "${RED}下载失败，请检查网络！${NC}"
    exit 1
fi

# 3. 解压文件
echo -e "${GREEN}正在解压文件 (覆盖同名文件)...${NC}"
unzip -o -q $TEMP_ZIP
rm -f $TEMP_ZIP

# 4. 进入解压后的目录
# GitHub ZIP 默认文件夹名为：项目名-分支名 (例如 EverKeep-project-master)
EXTRACTED_DIR=$(ls -d */ | grep "EverKeep-project-" | head -n 1)

if [ -z "$EXTRACTED_DIR" ]; then
    echo -e "${RED}错误: 未找到解压后的项目目录！${NC}"
    exit 1
fi

echo -e "${YELLOW}进入项目目录: $EXTRACTED_DIR${NC}"
cd "$EXTRACTED_DIR" || exit 1

# 5. 检查 docker-compose.yml 是否存在
if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}错误: 在 $(pwd) 下找不到 docker-compose.yml${NC}"
    exit 1
fi

# 6. 执行部署
echo -e "${YELLOW}请选择部署模式:${NC}"
echo "1) 仅部署前端 (Web)"
echo "2) 仅部署后端 (Server)"
echo "3) 全部重新部署"
echo "4) 退出"
read -p "请输入序号 [1-4]: " choice

case $choice in
    1)
        echo -e "${GREEN}正在构建前端...${NC}"
        docker compose up -d --build everkeep-web
        ;;
    2)
        echo -e "${GREEN}正在构建后端...${NC}"
        docker compose up -d --build everkeep-server
        ;;
    3)
        echo -e "${GREEN}全量构建部署...${NC}"
        docker compose up -d --build
        ;;
    4)
        exit 0
        ;;
    *)
        echo -e "${RED}无效输入${NC}"
        exit 1
        ;;
esac

echo -e "${GREEN}==== 部署完成！当前目录: $(pwd) ====${NC}"
docker compose ps