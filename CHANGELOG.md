# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [4.0.0] 2024-12-06

Breaking change in the generated feign-client

### Changed

Geo-json geometry package changes:<br>

| OLD                                                               | NEW                                                     |
|-------------------------------------------------------------------|---------------------------------------------------------|
| `nu.ndw.nls.wkdapi.client.generated.model.v1.GeometryJson`        | `nu.ndw.nls.geojson.geometry.model.GeometryJson`        |
| `nu.ndw.nls.wkdapi.client.generated.model.v1.LineStringJson`      | `nu.ndw.nls.geojson.geometry.model.LineStringJson`      |
| `nu.ndw.nls.wkdapi.client.generated.model.v1.PointJson`           | `nu.ndw.nls.geojson.geometry.model.PointJson`           |
| `nu.ndw.nls.wkdapi.client.generated.model.v1.MultiLineStringJson` | `nu.ndw.nls.geojson.geometry.model.MultiLineStringJson` |
| `nu.ndw.nls.wkdapi.client.generated.model.v1.PolygonJson`         | `nu.ndw.nls.geojson.geometry.model.PolygonJson`         |
| `nu.ndw.nls.wkdapi.client.generated.model.v1.MultiPolygonJson`    | `nu.ndw.nls.geojson.geometry.model.MultiPolygonJson`    |
