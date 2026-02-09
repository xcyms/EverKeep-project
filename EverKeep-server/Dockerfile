# 使用 Debian 为基础的镜像，因为 JavaCV 的 FFmpeg 依赖 glibc，Alpine 的 musl 会导致无法初始化
FROM docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 1. 首先只复制 pom.xml
COPY pom.xml .

# 2. 预下载依赖
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -DskipTests -Pprod

# 3. 复制源码并打包
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -Pprod

# 运行阶段也使用非 Alpine 镜像
FROM docker.m.daocloud.io/library/eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV JAVA_OPTS="-Dspring.profiles.active=prod"
EXPOSE 8070
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]