# 使用 daocloud 的代拉地址
FROM docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# 加上阿里云 Maven 镜像加快下载速度
RUN mvn clean package -DskipTests -Pprod

FROM docker.m.daocloud.io/library/eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV JAVA_OPTS="-Dspring.profiles.active=prod"
EXPOSE 8070
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]