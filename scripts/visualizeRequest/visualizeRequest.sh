curl 'https://data.ndw.nu/api/rest/static-road-data/accessibility-map/v2/accessibility.geojson' \
  -H 'Accept: application/geo+json' \
  -H 'Content-Type: application/json' \
  -d '
  {
    "area": {
      "type": "municipality",
      "id": "GM0363"
    },
    "vehicle": {
      "type": "car",
      "width": 0,
      "height": 0,
      "weight": 0,
      "length": 0,
      "axleLoad": 0,
      "hasTrailer": false,
      "emissionClass": "euro_1",
      "fuelTypes": [
        "hydrogen"
      ]
    },
    "includeAccessibleRoadSections": true,
    "destination": {
      "latitude": 52.37297089,
      "longitude": 4.89304433
    }
  }
  ' \
  | jq > response.geojson
