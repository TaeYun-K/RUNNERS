# 1) build stage
FROM node:20-alpine AS build
WORKDIR /app

ARG VITE_GOOGLE_CLIENT_ID
ENV VITE_GOOGLE_CLIENT_ID=$VITE_GOOGLE_CLIENT_ID

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# 2) runtime stage (nginx)
FROM nginx:1.25-alpine
COPY --from=build /app/dist/ /usr/share/nginx/html/

EXPOSE 80
