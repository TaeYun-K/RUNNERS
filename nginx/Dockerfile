FROM nginx:1.25-alpine

# nginx conf
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

# static html
COPY frontend/ /usr/share/nginx/html/
