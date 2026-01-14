# 构建阶段
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# 跳过测试进行打包
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# 设置环境变量，指定激活 prod 配置文件
ENV JAVA_OPTS="-Dspring.profiles.active=prod"
EXPOSE 8070
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]