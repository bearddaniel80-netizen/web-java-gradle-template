# syntax=docker/dockerfile:1.7

# ---------- Common Git Stage ----------
FROM alpine:latest AS git-base

RUN --mount=type=cache,target=/var/cache/apt \
    apk update && \
    apk add git

# ------- caddy file ---------------
FROM git-base AS caddy-file

ENV GIT_REPO=web-java-lib

ENV GIT_URL=https://github.com/bearddaniel80-netizen/${GIT_REPO}.git

WORKDIR /downloads

RUN git clone -b common ${GIT_URL}

WORKDIR /app

RUN cp /downloads/${GIT_REPO}/Caddyfile .

# ----------- caddy builder -----------    
FROM caddy:2-builder AS builder

RUN --mount=type=cache,target=/root/go/pkg/mod \
    --mount=type=cache,target=/root/.cache/go-build \
    xcaddy build \
        --with github.com/mholt/caddy-ratelimit

FROM caddy:2-alpine

COPY --from=builder /usr/bin/caddy /usr/bin/caddy
COPY --from=caddy-file /app/Caddyfile /etc/caddy/Caddyfile