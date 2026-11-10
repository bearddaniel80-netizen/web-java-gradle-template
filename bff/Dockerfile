# syntax=docker/dockerfile:1.7
# ---------- Java runtime ----------
FROM gradle:jdk21 AS runtime

WORKDIR /app

COPY --chown=gradle:gradle java/ ./

CMD ["gradle", "bootRun"]