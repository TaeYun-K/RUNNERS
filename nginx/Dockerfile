FROM nginx:1.25-alpine

# nginx conf
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

# static html
COPY frontend-static/ /usr/share/nginx/html/
