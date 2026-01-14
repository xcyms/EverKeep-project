FROM docker.m.daocloud.io/library/node:20-alpine AS build-stage
WORKDIR /app
COPY package*.json ./
# 使用淘宝镜像源
RUN npm install --registry=https://registry.npmmirror.com
COPY . .
RUN npm run build

FROM docker.m.daocloud.io/library/nginx:stable-alpine AS production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]