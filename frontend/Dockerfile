# Multi-stage Dockerfile for React Frontend SPA

# Stage 1: Build stage with Node 20
FROM node:20-alpine AS builder
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm ci

# Copy source code and build production bundle
COPY . .
ENV VITE_API_BASE_URL=/api/claims
RUN npm run build

# Stage 2: Production Nginx web server
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove default nginx static assets
RUN rm -rf ./*

# Copy compiled dist assets from builder stage
COPY --from=builder /app/dist .

# Copy custom Nginx configuration listening on port 7001
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose frontend port 7001
EXPOSE 7001

CMD ["nginx", "-g", "daemon off;"]
