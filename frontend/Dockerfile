# syntax=docker/dockerfile:1.7
# ---------- deps ----------
FROM node:22-alpine AS deps

ENV GIT_BASE=github.com/bearddaniel80-netizen

RUN apk add --no-cache git

RUN git clone --depth=1 https://${GIT_BASE}/web-template.git

WORKDIR /build

RUN cp /web-template/frontend/package.json .

RUN --mount=type=cache,target=/root/.npm \
    npm install

# ---------- React build ----------
FROM deps AS react-runtime

WORKDIR /app

COPY --from=deps /build/node_modules ./node_modules

COPY --from=deps /web-template/frontend .
# To generate docs
# RUN npm run docs

RUN npm run build

# ------- assembly ---------------
FROM deps AS assembly

ARG DOMAIN
ARG APP_LANG
ARG BUILD_FILE
ARG EXT

ENV BUILD_FILE=${BUILD_FILE}

ENV APP_LANG=${APP_LANG}

ENV EXT=${EXT}

ENV DOMAIN=${DOMAIN}

ENV BUILD_DIR=/build-downloads
ENV COMMON_DIR=/common-downloads
ENV DOMAIN_DIR=/${DOMAIN}-downloads

ENV GIT_REPO=web-${APP_LANG}-lib

ENV GIT_URL=https://${GIT_BASE}/${GIT_REPO}.git

RUN git clone -b build --single-branch ${GIT_URL} ${BUILD_DIR}

RUN git clone -b common --single-branch ${GIT_URL} ${COMMON_DIR}

RUN git clone -b ${DOMAIN} --single-branch ${GIT_URL} ${DOMAIN_DIR}

# ........ build file ..........
WORKDIR /app/${APP_LANG}

RUN cp ${BUILD_DIR}/${BUILD_FILE} .

# ........ actual code ..........
WORKDIR /app/${APP_LANG}/src/main/${APP_LANG}/example

RUN cp ${COMMON_DIR}/Application.${EXT} .

# ........ resources ..............
WORKDIR /app/${APP_LANG}/src/main

RUN cp -R ${DOMAIN_DIR}/resources .

# ---------- Java runtime ----------
FROM gradle:jdk21 AS runtime

ARG APP_LANG
ENV APP_LANG=${APP_LANG}

WORKDIR /app

COPY --chown=gradle:gradle ${APP_LANG}/ ./

COPY --from=assembly /app/${APP_LANG} .

# Copy React production build into Spring Boot static resources
COPY --from=react-runtime --chown=gradle:gradle \
    /app/dist/ ./src/main/resources/static/

CMD ["gradle", "bootRun"]