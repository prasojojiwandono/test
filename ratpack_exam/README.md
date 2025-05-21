
# Company XYZ Online Store API

This project provides a set of RESTful APIs for Company XYZ's Online Store using the Java Ratpack framework and SQLite database.

## Getting Started

### Prerequisites

- Java 11+
- Gradle

### Running the Application

```bash
# Run the application
gradlew run
```

The server will start on port 8080.

## API Documentation

### Customer APIs

#### Get Available Items

Returns all available items with their current prices (including promotions if applicable).

- **URL**: `/api/v1/customer/items`
- **Method**: `GET`
- **Response**: 
  ```json
  [
    {
      "id": 1,
      "name": "T-Shirt",
      "color": "Blue",
      "regularPrice": 25.99,
      "currentPrice": 20.79,
      "stock": 100,
      "hasPromotion": true,
      "discountPercentage": 20.0
    }
  ]
  ```

#### Purchase an Item

Allows a customer to purchase an item. If a promotion ID is provided and valid, the promotion will be applied.

- **URL**: `/api/v1/customer/purchase`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "itemId": 1,
    "customerIdCard": "ID12345678",
    "customerName": "John Doe",
    "customerPhone": "+1234567890",
    "quantity": 2,
    "promotionId": 1
  }
  ```
- **Response**: 201 Created with purchase details

### Admin APIs (Protected with Basic Authentication)

Default credentials:
- Username: `admin`
- Password: `admin123`

#### Items Management

##### Get All Items

- **URL**: `/api/v1/admin/items`
- **Method**: `GET`
- **Auth**: Basic Auth
- **Response**: List of all items with stock information

##### Get Item by ID

- **URL**: `/api/v1/admin/items/{id}`
- **Method**: `GET`
- **Auth**: Basic Auth

##### Create Item

- **URL**: `/api/v1/admin/items`
- **Method**: `POST`
- **Auth**: Basic Auth
- **Request Body**:
  ```json
  {
    "name": "T-Shirt",
    "color": "Blue",
    "price": 25.99,
    "stock": 100
  }
  ```

##### Update Item

- **URL**: `/api/v1/admin/items/{id}`
- **Method**: `PUT`
- **Auth**: Basic Auth
- **Request Body**: Same as Create Item

##### Delete Item

- **URL**: `/api/v1/admin/items/{id}`
- **Method**: `DELETE`
- **Auth**: Basic Auth

#### Promotions Management

##### Get All Promotions

- **URL**: `/api/v1/admin/promotions`
- **Method**: `GET`
- **Auth**: Basic Auth

##### Get Promotion by ID

- **URL**: `/api/v1/admin/promotions/{id}`
- **Method**: `GET`
- **Auth**: Basic Auth

##### Create Promotion

- **URL**: `/api/v1/admin/promotions`
- **Method**: `POST`
- **Auth**: Basic Auth
- **Request Body**:
  ```json
  {
    "itemId": 1,
    "discountPercentage": 20.0,
    "quota": 50,
    "limitPerCustomer": 2,
    "enabled": true
  }
  ```

##### Update Promotion

- **URL**: `/api/v1/admin/promotions/{id}`
- **Method**: `PUT`
- **Auth**: Basic Auth
- **Request Body**: Same as Create Promotion

##### Delete Promotion

- **URL**: `/api/v1/admin/promotions/{id}`
- **Method**: `DELETE`
- **Auth**: Basic Auth

#### Customer Data Access

##### Get Customers for an Item

Returns all customer information for a specific item.

- **URL**: `/api/v1/admin/customers/items/{itemId}/customers`
- **Method**: `GET`
- **Auth**: Basic Auth

## Business Logic Rules

1. Items cannot be deleted if they have been sold
2. Item stock cannot be reduced below the number of sold items
3. Promotions cannot be deleted if they have been used by customers
4. Promotion quota cannot be reduced below the number of used quota
5. Customer cannot purchase items without available stock
6. Customer cannot purchase items with promotions that are disabled or have insufficient quota
7. Customer purchases cannot be canceled once submitted

## Data Security

- Customer sensitive data (ID card, name, phone) is encrypted in the database using AES encryption
- Admin endpoints are protected with Basic Authentication

## Database Schema

### Items Table
- id (PRIMARY KEY)
- name
- color
- price
- stock

### Promotions Table
- id (PRIMARY KEY)
- item_id (FOREIGN KEY)
- discount_percentage
- quota
- limit_per_customer
- enabled
- used_quota

### Purchases Table
- id (PRIMARY KEY)
- item_id (FOREIGN KEY)
- customer_id_card (ENCRYPTED)
- customer_name (ENCRYPTED)
- customer_phone (ENCRYPTED)
- quantity
- price_paid
- promotion_id (FOREIGN KEY)
- purchase_date
