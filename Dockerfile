# 第一阶段：使用 Maven 和 Java 17 构建项目
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

# 设置工作目录
WORKDIR /app

# 先复制 pom.xml 下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建项目
RUN mvn clean package -DskipTests -B

# 第二阶段：使用 Java 17 运行项目
FROM eclipse-temurin:17-jdk-alpine

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 jar 文件
COPY --from=builder /app/target/*.jar app.jar

# 暴露应用端口
EXPOSE 8080

# 设置 JVM 参数（可根据需要调整）
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
