# API Collections

This folder contains import-ready API collections for the product endpoints.

## Postman

Import [postman/ReactiveCrudSpringboot.postman_collection.json](postman/ReactiveCrudSpringboot.postman_collection.json) into Postman.

Default variables:
- `baseUrl`: `http://localhost:8080`
- `productId`: `1`
- `searchTerm`: `laptop`
- `minPrice`: `100`
- `maxPrice`: `1000`

## Bruno

Open the [bruno](bruno) folder as a Bruno collection.

Default local environment:
- `baseUrl`: `http://localhost:8080`
- `productId`: `1`
- `searchTerm`: `laptop`
- `minPrice`: `100`
- `maxPrice`: `1000`

Requests included:
- List products
- Get product by id
- Create product
- Update product
- Delete product
- Validation error example
- Not found example
- Search products by name
- Filter products by price range
