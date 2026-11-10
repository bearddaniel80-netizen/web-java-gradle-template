# syntax=docker/dockerfile:1.7
# ---------- deps ----------
FROM python:3.12-slim AS deps

WORKDIR /build

RUN --mount=type=cache,target=/root/.cache/pip \
    pip install \
    --prefix=/install \
    build rich typer

# ---------- Common Git Stage ----------
FROM deps AS git-base

RUN --mount=type=cache,target=/var/cache/apt \
    apt-get update && \
    apt-get install -y git

# ---------- aegis link ----------
FROM git-base AS link

COPY --from=deps /install /usr/local

RUN git clone --depth=1 https://github.com/bearddaniel80-netizen/aegis-link.git

WORKDIR aegis-link/src

RUN python -m build

RUN mv dist/*.whl /tmp/

# ------------ aql -----------------
FROM git-base AS aql

COPY --from=deps /install /usr/local

WORKDIR /downloads

RUN git clone --depth=1 https://github.com/bearddaniel80-netizen/aql-public.git

WORKDIR /downloads/aql-public/src

RUN python -m build

RUN mv dist/*.whl /tmp/

WORKDIR /app

RUN cp -R /downloads/aql-public/data/* .

# ---------- aql compliance ----------
FROM git-base AS compliance

COPY --from=deps /install /usr/local

WORKDIR /downloads

RUN git clone --depth=1 https://github.com/bearddaniel80-netizen/compliance-public.git

WORKDIR /downloads/compliance-public/src

RUN python -m build

RUN mv dist/*.whl /tmp/

RUN pip install /tmp/*.whl

WORKDIR /app

RUN cp -R /downloads/compliance-public/data/* .

RUN cp /downloads/compliance-public/README.md .

# ---------- Java runtime ----------
FROM gradle:jdk21 AS runtime

RUN --mount=type=cache,target=/var/cache/apt \
    apt-get update && \
    apt-get install -y python3 python3-venv pandoc poppler-utils

RUN python3 -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

WORKDIR /app

COPY --from=link /tmp/*.whl /tmp/
COPY --from=aql /tmp/*.whl /tmp/
COPY --from=compliance /tmp/*.whl /tmp/
RUN --mount=type=cache,target=/root/.cache/pip \
    pip install /tmp/*.whl

COPY --from=aql /app .
COPY --from=compliance /app .

COPY --chown=gradle:gradle java/ ./

CMD ["gradle", "bootRun"]