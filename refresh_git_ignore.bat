@echo off
echo "准备更新git ignore缓存"
git rm -r --cached .
git add .
echo "更新缓存完成"