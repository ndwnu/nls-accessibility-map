#!/bin/bash

# Exit whenever a command in the script fails
set -e

curl -v --location --request POST 'http://localhost:15672/api/exchanges/%2f/eventbus/publish' \
--header 'Authorization: Basic Z3Vlc3Q6Z3Vlc3Q=' \
--header 'Content-Type: text/plain' \
--data-raw '{
  "properties": {},
  "routing_key": "ndw.nls.nwb.imported",
  "payload": "{\"type\": \"NWB-imported\", \"timestamp\": \"2024-08-15T10:39:00.779302Z\", \"message_id\": \"256aa6d8-07c0-11eb-8f23-0242ac180004\", \"subject\": {\"type\": \"nwb-version\", \"container\": \"nwb\", \"name\": \"somefile.ext\", \"version\": \"18140\", \"revision\": \"2024-07-01T08:15:25Z\"}}",
  "payload_encoding": "string"
}'
