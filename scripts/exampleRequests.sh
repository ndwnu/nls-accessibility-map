time curl -X 'POST' \
  'https://data.ndw.nu/api/rest/static-road-data/accessibility-map/v2/accessibility.geojson' \
  -H 'accept: application/geo+json' \
  -H 'Accept-Encoding: gzip' \
  -H 'Content-Type: application/json' \
  -d '{
  "destination": {
    "latitude": 52.093784,
    "longitude": 5.15289
  },
  "area": {
    "type": "municipality",
    "id": "GM0344"
  },
  "vehicle": {
    "type": "truck",
    "width": 2,
    "height": 2.5,
    "weight": 20,
    "length": 5.2,
    "axleLoad": 4,
    "hasTrailer": false,
    "emissionClass": "euro_6",
    "fuelTypes": [
      "petrol"
    ]
  },
  "exclusions": {
    "emissionZoneTypes": [
      "low_emission_zone"
    ],
    "emissionZoneIds": [
      "NDW11_63a0104e-0b70-4b01-ad72-1ec692b41c47"
    ]
  }
}'
