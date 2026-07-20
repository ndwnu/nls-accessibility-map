
Fixes:
 - removed "format": "uri" from spec or else null parameters are not allowed. The API spec uses format: uri for the bbox-crs, crs, and 
   Accept-Crs parameters, causing the OpenAPI generator to produce java.net.URI types. Feign has special handling for URI-typed parameters 
   when they're null, it throws "URI parameter N was null" instead of simply omitting them from the request.
 - Point now only has a type and coordinates

