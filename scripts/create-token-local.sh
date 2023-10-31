#!/bin/bash

# exit on error
set -e

curl -s --location --request POST 'http://localhost:8000/auth/realms/ndw/protocol/openid-connect/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'client_id=nls-accessibility-map-api-service-account' \
  --data-urlencode 'client_secret=cfe4793c-ddf7-405a-940f-51b43800aed4' \
  --data-urlencode 'grant_type=client_credentials' \
  | jq -r '.access_token'

echo
