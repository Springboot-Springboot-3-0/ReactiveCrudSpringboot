# API Collections

This folder contains import-ready API collections for the product endpoints.

## Postman

Import [postman/ReactiveCrudSpringboot.postman_collection.json](postman/ReactiveCrudSpringboot.postman_collection.json) into Postman.

Default variables:
- `baseUrl`: `http://localhost:8080`
- `productId`: `1`

## Bruno

Open the [bruno](bruno) folder as a Bruno collection.

Default local environment:
- `baseUrl`: `http://localhost:8080`
- `productId`: `1`

Requests included:
- List products
- Get product by id
- Create product
- Update product
- Delete product
- Validation error example
- Not found example
- Create product missing price
- Get product with invalid id
