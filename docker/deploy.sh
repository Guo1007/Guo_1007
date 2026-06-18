#!/bin/bash
# 家具电商系统 - Docker 一键部署脚本
set -e

echo "=========================================="
echo "  家具电商系统 Docker 部署"
echo "=========================================="

# 检查 .env 文件
if [ ! -f .env ]; then
    echo "⚠️  未找到 .env 文件，正在从模板创建..."
    cp .env.example .env
    echo "📝 请编辑 .env 文件填入实际配置："
    echo "   vim .env"
    echo ""
    echo "必填项：DEEPSEEK_API_KEY、OSS_ACCESS_KEY、OSS_SECRET_KEY、OSS_BUCKET、OSS_URL"
    exit 1
fi

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ 未安装 Docker，请先安装："
    echo "   curl -fsSL https://get.docker.com | sh"
    exit 1
fi

echo "🔨 构建镜像..."
docker compose build --no-cache

echo "🚀 启动服务..."
docker compose up -d

echo ""
echo "=========================================="
echo "  ✅ 部署完成！"
echo "=========================================="
echo ""
echo "  前端访问: http://localhost"
echo "  后端 API: http://localhost:8080"
echo "  MySQL:    localhost:3307"
echo "  Redis:    localhost:6380"
echo ""
echo "  查看日志: docker compose logs -f"
echo "  停止服务: docker compose down"
echo "  重启服务: docker compose restart"
echo ""
