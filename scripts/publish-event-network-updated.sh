#!/bin/bash
curl --location --request POST 'http://localhost:15672/api/exchanges/%2f/eventbus/publish' \
  --header 'Authorization: Basic Z3Vlc3Q6Z3Vlc3Q=' \
  --header 'Content-Type: text/plain' \
  --data-raw '{
  "properties": {},
  "routing_key": "ndw.nls.accessibility.routing.network.updated",
  "payload": "{\"type\": \"accessibility-routing-network-updated\", \"timestamp\": \"2024-01-01T10:39:00.779300\", \"message_id\": \"256aa6d8-07c0-11eb-8f23-0242ac180002\", \"subject\": {\"type\": \"accessibility-routing-network\", \"nwbVersion\": \"20240101\", \"timestamp\": \"2024-01-01T10:39:00.779300\"}}",
  "payload_encoding": "string"
}'