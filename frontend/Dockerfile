# ---------- 1단계: 빌드 (Node 로 Vue 를 정적 파일로 만들기) ----------
FROM node:24-slim AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---------- 2단계: 실행 (nginx 가 빌드 결과를 제공) ----------
FROM nginx:stable-alpine
ENV TZ=Asia/Seoul

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80