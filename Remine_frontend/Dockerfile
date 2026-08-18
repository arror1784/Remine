# syntax=docker/dockerfile:1

FROM node:22-alpine AS build
WORKDIR /src

# Node's Alpine images don't reliably bundle Yarn Classic across tags — pin it explicitly.
RUN corepack enable && corepack prepare yarn@1.22.22 --activate

COPY package.json yarn.lock ./
RUN yarn install --frozen-lockfile

COPY . .

# Relative path, not an absolute domain: nginx reverse-proxies /api on the same
# origin the page is served from, so the build never needs to know the domain.
# `http.ts` falls back to localhost:8080 on an EMPTY string (`|| 'http://...'`),
# so this must be a non-empty value.
ARG VITE_API_BASE_URL=/
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN yarn build

FROM nginx:alpine
COPY --from=build /src/dist /usr/share/nginx/html
