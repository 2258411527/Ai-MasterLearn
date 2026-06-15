@echo off
setlocal

echo ==============================================
echo 校园在线考试系统 - 启动脚本
echo ==============================================

:: 设置Java选项，解决Java 9+模块系统限制
set JAVA_OPTS=-Xms512m -Xmx1024m ^
    --add-opens java.base/java.lang=ALL-UNNAMED ^
    --add-opens java.base/java.lang.reflect=ALL-UNNAMED ^
    --add-opens java.base/java.util=ALL-UNNAMED ^
    --add-opens java.base/java.net=ALL-UNNAMED ^
    --add-opens java.base/java.io=ALL-UNNAMED ^
    --add-opens java.base/java.security=ALL-UNNAMED ^
    --add-opens java.base/sun.net.www.protocol=ALL-UNNAMED ^
    --add-opens java.sql/java.sql=ALL-UNNAMED ^
    --add-opens java.desktop/java.awt.image=ALL-UNNAMED ^
    --add-exports java.base/sun.net.www.protocol=ALL-UNNAMED ^
    --illegal-access=permit

:: 查找JAR文件
set JAR_FILE=
for %%f in (target\exam-1.0-SNAPSHOT.jar) do set JAR_FILE=%%f

if not defined JAR_FILE (
    echo 错误：未找到JAR文件，请先运行 mvn clean package
    pause
    exit /b 1
)

echo 正在启动应用...
echo JVM选项: %JAVA_OPTS%
echo JAR文件: %JAR_FILE%
echo.

java %JAVA_OPTS% -jar %JAR_FILE%

pause