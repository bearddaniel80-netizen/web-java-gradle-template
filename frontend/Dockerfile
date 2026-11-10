# syntax=docker/dockerfile:1.7

# ---------- deps ----------
FROM node:22-alpine AS deps

RUN apk add --no-cache git

RUN git clone --depth=1 \
    https://github.com/bearddaniel80-netizen/web-template.git

WORKDIR /build

RUN cp /web-template/frontend/package.json .

RUN --mount=type=cache,target=/root/.npm \
    npm install

# ---------- React library ----------
FROM deps AS lib

WORKDIR /components

RUN git clone --depth=1 \
    https://github.com/bearddaniel80-netizen/react-component-lib.git

# ---------- React build ----------
FROM deps AS react-runtime

WORKDIR /app

COPY --from=deps /build/node_modules ./node_modules

COPY --from=deps /web-template/frontend .
# To generate docs
# RUN npm run docs

# COPY --from=lib /components/react-component-lib src

RUN npm run build

# ---------- Java runtime ----------
FROM gradle:jdk21 AS runtime

WORKDIR /app

COPY --chown=gradle:gradle java/ ./

# Copy React production build into Spring Boot static resources
COPY --from=react-runtime --chown=gradle:gradle \
    /app/dist/ ./src/main/resources/static/

CMD ["gradle", "bootRun"]