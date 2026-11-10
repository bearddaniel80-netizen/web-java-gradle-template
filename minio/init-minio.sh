#!/bin/sh

set -e

ROOT_USER=$(cat /run/secrets/minio_root_user)
ROOT_PASSWORD=$(cat /run/secrets/minio_root_password)
APP_USER=$(cat /run/secrets/minio_app_user)
APP_PASSWORD=$(cat /run/secrets/minio_app_password)

echo "Connecting to MinIO..."

mc alias set minio "${MINIO_ENDPOINT}" \
  "${ROOT_USER}" \
  "${ROOT_PASSWORD}"

echo "Creating bucket..."

mc mb --ignore-existing minio/uploads

echo "Creating policy..."

mc admin policy create \
  minio \
  uploads-policy \
  /policy.json

echo "Creating application user..."

mc admin user add \
  minio \
  "${APP_USER}" \
  "${APP_PASSWORD}"

echo "Attaching policy..."

mc admin policy attach \
  minio \
  --user "${APP_USER}" \
  uploads-policy

echo "Testing application credentials..."

mc alias set app-test "${MINIO_ENDPOINT}" \
  "${APP_USER}" \
  "${APP_PASSWORD}"

echo "Testing ListBucket..."

mc ls app-test/uploads

echo "Testing PutObject..."

echo "hello" > /tmp/test.txt
mc cp /tmp/test.txt app-test/uploads/test.txt

echo "Application credentials work!"
echo "MinIO initialization complete."