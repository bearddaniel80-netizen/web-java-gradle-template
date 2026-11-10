# syntax=docker/dockerfile:1.7

FROM caddy:2-builder AS builder

RUN --mount=type=cache,target=/root/go/pkg/mod \
    --mount=type=cache,target=/root/.cache/go-build \
    xcaddy build \
        --with github.com/mholt/caddy-ratelimit

FROM caddy:2-alpine

COPY --from=builder /usr/bin/caddy /usr/bin/caddy
COPY Caddyfile /etc/caddy/Caddyfile