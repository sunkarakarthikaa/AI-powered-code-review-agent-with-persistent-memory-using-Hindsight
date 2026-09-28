# --- Stage 1: build ---
FROM node:20-alpine AS build

WORKDIR /app

# Same caching principle as the backend Dockerfile: copy package files
# first so npm install is only re-run when dependencies actually change.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# --- Stage 2: serve ---
# The build output is static HTML/JS/CSS — it doesn't need Node at runtime
# at all, just a web server. nginx is a standard, lightweight choice here.
FROM nginx:1.27-alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]