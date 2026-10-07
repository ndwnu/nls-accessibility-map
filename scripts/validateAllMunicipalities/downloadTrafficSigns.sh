curl 'https://data.ndw.nu/api/rest/static-road-data/traffic-signs/v4/current-state?status=PLACED&rvvCode=C1&rvvCode=C10&rvvCode=C11&rvvCode=C12&rvvCode=C17&rvvCode=C18&rvvCode=C19&rvvCode=C20&rvvCode=C21&rvvCode=C22&rvvCode=C22a&rvvCode=C22c&rvvCode=C6&rvvCode=C7&rvvCode=C7a&rvvCode=C7b&rvvCode=C7c&rvvCode=C8&rvvCode=C9'  \
  | jq > trafficSigns.geojson


