# wishlist

## Health

Enpoint para verifica saúde da aplicação, conexão com banco, redis, etc.

```http request
GET /actuator/health HTTP/1.1
Host: localhost:8080
```

## Wishlist Products by Client

```http request
GET /api/wishlist/client/CLIENT_ID/products HTTP/1.1
Host: localhost:8080
```

## Check Product in Client Wish list

```http request
GET /api/wishlist/client/CLIENT_ID/product/PRODUCT_ID HTTP/1.1
Host: localhost:8080
```

## Add Product to Wish list

```http request
POST /api/wishlist HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Content-Length: 54

{
"clientId": "1234",
"productId": "1234"
}
```

## Remove Product From Wish list

```http request
DELETE /api/wishlist/client/CLIENT_ID/product/PRODUCT_ID HTTP/1.1
Host: localhost:8080
```