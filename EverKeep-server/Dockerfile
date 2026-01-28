# 使用 BuildKit 缓存挂载优化依赖下载
FROM docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# 1. 首先只复制 pom.xml。这是利用缓存的关键：只要 pom.xml 不变，后续下载依赖的层就不会失效
COPY pom.xml .

# 2. 预下载依赖。使用 --mount=type=cache 挂载 Maven 本地仓库 (/root/.m2)，
# 这样即使 Docker 层缓存失效，已下载的 jar 包也会保留在宿主机的 BuildKit 缓存中。
# 加上阿里云镜像参数可以进一步加快国内下载速度
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -DskipTests -Pprod

# 3. 复制源码并打包
# 此时只有源码变更，前面的依赖层会被直接复用
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -Pprod

FROM docker.m.daocloud.io/library/eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV JAVA_OPTS="-Dspring.profiles.active=prod"
EXPOSE 8070
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]