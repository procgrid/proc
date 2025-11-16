# Kafka Topic Design & Event Schemas

## Overview

This document defines the complete event-driven architecture for the ProcGrid B2B e-commerce platform using Apache Kafka for asynchronous communication between microservices.

## Kafka Configuration

```yaml
# Kafka Cluster Configuration
bootstrap.servers: localhost:9092
default.replication.factor: 3
min.insync.replicas: 2
acks: all
retries: 2147483647
max.in.flight.requests.per.connection: 1
enable.idempotence: true

# Topic Defaults
num.partitions: 3
retention.ms: 604800000  # 7 days
compression.type: snappy
```

## Topic Naming Convention

**Format**: `{domain}.{entity}.{action}.{version}`

**Examples**:
- `user.profile.created.v1`
- `order.order.confirmed.v1`
- `payment.escrow.released.v1`

---

## 🔐 USER & KYC EVENTS

### Topic: `user.profile.created.v1`

**Description**: Published when a new user (producer/buyer) successfully registers

**Partition Key**: `userId`

**Schema**:
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "User Profile Created Event",
  "type": "object",
  "required": ["eventId", "eventType", "timestamp", "sourceService", "payload"],
  "properties": {
    "eventId": {
      "type": "string",
      "description": "Unique event identifier",
      "pattern": "^[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}$"
    },
    "eventType": {
      "type": "string",
      "enum": ["user.profile.created"],
      "description": "Event type identifier"
    },
    "timestamp": {
      "type": "string",
      "format": "date-time",
      "description": "Event creation timestamp in ISO 8601 format"
    },
    "sourceService": {
      "type": "string",
      "enum": ["user-service"],
      "description": "Service that published this event"
    },
    "version": {
      "type": "string",
      "enum": ["1.0"],
      "description": "Event schema version"
    },
    "correlationId": {
      "type": "string",
      "description": "Correlation ID for request tracing"
    },
    "payload": {
      "type": "object",
      "required": ["userId", "email", "role", "profile"],
      "properties": {
        "userId": {
          "type": "string",
          "description": "Unique user identifier"
        },
        "email": {
          "type": "string",
          "format": "email",
          "description": "User email address"
        },
        "phone": {
          "type": "string",
          "description": "User phone number"
        },
        "role": {
          "type": "string",
          "enum": ["PRODUCER", "BUYER", "ADMIN"],
          "description": "User role"
        },
        "profile": {
          "type": "object",
          "properties": {
            "firstName": {"type": "string"},
            "lastName": {"type": "string"},
            "farmName": {"type": "string", "description": "For producers only"},
            "businessName": {"type": "string", "description": "For buyers only"},
            "location": {"type": "string"},
            "gstNumber": {"type": "string"}
          }
        },
        "registrationSource": {
          "type": "string",
          "enum": ["WEB", "MOBILE", "API"],
          "description": "Registration channel"
        }
      }
    }
  }
}
```

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440001",
  "eventType": "user.profile.created",
  "timestamp": "2025-11-15T10:30:00.000Z",
  "sourceService": "user-service",
  "version": "1.0",
  "correlationId": "req-12345-abcd",
  "payload": {
    "userId": "user_67890",
    "email": "farmer@example.com",
    "phone": "+919876543210",
    "role": "PRODUCER",
    "profile": {
      "firstName": "John",
      "lastName": "Doe",
      "farmName": "Green Valley Farm",
      "location": "Punjab, India",
      "gstNumber": "03AAACG2115R1Z1"
    },
    "registrationSource": "WEB"
  }
}
```

---

### Topic: `user.kyc.submitted.v1`

**Description**: Published when user submits KYC documents for verification

**Schema**:
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "KYC Submission Event",
  "type": "object",
  "required": ["eventId", "eventType", "timestamp", "sourceService", "payload"],
  "properties": {
    "eventId": {"type": "string"},
    "eventType": {"type": "string", "enum": ["user.kyc.submitted"]},
    "timestamp": {"type": "string", "format": "date-time"},
    "sourceService": {"type": "string", "enum": ["user-service"]},
    "version": {"type": "string", "enum": ["1.0"]},
    "payload": {
      "type": "object",
      "required": ["userId", "submissionId", "documents"],
      "properties": {
        "userId": {"type": "string"},
        "submissionId": {"type": "string"},
        "documents": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "documentId": {"type": "string"},
              "documentType": {
                "type": "string",
                "enum": ["IDENTITY_PROOF", "ADDRESS_PROOF", "GST_CERTIFICATE", "FARM_OWNERSHIP", "BUSINESS_LICENSE"]
              },
              "fileName": {"type": "string"},
              "fileSize": {"type": "integer"},
              "fileUrl": {"type": "string", "format": "uri"}
            }
          }
        },
        "submittedAt": {"type": "string", "format": "date-time"}
      }
    }
  }
}
```

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440002",
  "eventType": "user.kyc.submitted",
  "timestamp": "2025-11-15T11:00:00.000Z",
  "sourceService": "user-service",
  "version": "1.0",
  "payload": {
    "userId": "user_67890",
    "submissionId": "kyc_sub_001",
    "documents": [
      {
        "documentId": "doc_123",
        "documentType": "IDENTITY_PROOF",
        "fileName": "aadhaar_card.pdf",
        "fileSize": 2048576,
        "fileUrl": "https://storage.procgrid.com/kyc/doc_123.pdf"
      },
      {
        "documentId": "doc_124",
        "documentType": "FARM_OWNERSHIP",
        "fileName": "farm_certificate.pdf",
        "fileSize": 1536000,
        "fileUrl": "https://storage.procgrid.com/kyc/doc_124.pdf"
      }
    ],
    "submittedAt": "2025-11-15T11:00:00.000Z"
  }
}
```

---

### Topic: `user.kyc.verified.v1`

**Description**: Published when KYC verification is completed successfully

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440003",
  "eventType": "user.kyc.verified",
  "timestamp": "2025-11-15T12:00:00.000Z",
  "sourceService": "user-service",
  "version": "1.0",
  "payload": {
    "userId": "user_67890",
    "submissionId": "kyc_sub_001",
    "verifiedBy": "admin_001",
    "verificationLevel": "LEVEL_2",
    "verifiedDocuments": ["doc_123", "doc_124"],
    "verifiedAt": "2025-11-15T12:00:00.000Z",
    "approvalNotes": "All documents verified successfully"
  }
}
```

---

### Topic: `user.kyc.rejected.v1`

**Description**: Published when KYC verification is rejected

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440004",
  "eventType": "user.kyc.rejected",
  "timestamp": "2025-11-15T12:30:00.000Z",
  "sourceService": "user-service",
  "version": "1.0",
  "payload": {
    "userId": "user_67890",
    "submissionId": "kyc_sub_001",
    "rejectedBy": "admin_002",
    "rejectedDocuments": ["doc_123"],
    "rejectionReason": "Document image quality is poor, please resubmit",
    "rejectedAt": "2025-11-15T12:30:00.000Z",
    "resubmissionAllowed": true
  }
}
```

---

## 📦 PRODUCT & INVENTORY EVENTS

### Topic: `product.lot.created.v1`

**Description**: Published when a new product lot is created by a producer

**Schema**:
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Product Lot Created Event",
  "type": "object",
  "required": ["eventId", "eventType", "timestamp", "sourceService", "payload"],
  "properties": {
    "eventId": {"type": "string"},
    "eventType": {"type": "string", "enum": ["product.lot.created"]},
    "timestamp": {"type": "string", "format": "date-time"},
    "sourceService": {"type": "string", "enum": ["product-service"]},
    "version": {"type": "string", "enum": ["1.0"]},
    "payload": {
      "type": "object",
      "required": ["productId", "producerId", "categoryId", "productDetails"],
      "properties": {
        "productId": {"type": "string"},
        "producerId": {"type": "string"},
        "categoryId": {"type": "string"},
        "productDetails": {
          "type": "object",
          "properties": {
            "title": {"type": "string"},
            "description": {"type": "string"},
            "pricePerUnit": {"type": "number"},
            "quantity": {"type": "number"},
            "unit": {"type": "string", "enum": ["KG", "TONNE", "QUINTAL", "PIECE", "LITER"]},
            "location": {"type": "string"},
            "qualityGrade": {"type": "string", "enum": ["PREMIUM", "GRADE_A", "GRADE_B", "STANDARD"]},
            "harvestDate": {"type": "string", "format": "date"},
            "expiryDate": {"type": "string", "format": "date"},
            "certifications": {"type": "array", "items": {"type": "string"}},
            "images": {"type": "array", "items": {"type": "string"}}
          }
        }
      }
    }
  }
}
```

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440005",
  "eventType": "product.lot.created",
  "timestamp": "2025-11-15T13:00:00.000Z",
  "sourceService": "product-service",
  "version": "1.0",
  "payload": {
    "productId": "prod_12345",
    "producerId": "producer_67890",
    "categoryId": "cat_basmati_rice",
    "productDetails": {
      "title": "Premium Basmati Rice - 1121 Variety",
      "description": "Premium quality 1121 Basmati rice with long grains and aromatic fragrance",
      "pricePerUnit": 85.50,
      "quantity": 1000.0,
      "unit": "KG",
      "location": "Haryana, India",
      "qualityGrade": "PREMIUM",
      "harvestDate": "2025-10-15",
      "expiryDate": "2026-10-15",
      "certifications": ["ORGANIC", "NON_GMO"],
      "images": [
        "https://storage.procgrid.com/products/prod_12345_1.jpg",
        "https://storage.procgrid.com/products/prod_12345_2.jpg"
      ]
    }
  }
}
```

---

### Topic: `product.lot.updated.v1`

**Description**: Published when product lot information is updated

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440006",
  "eventType": "product.lot.updated",
  "timestamp": "2025-11-15T14:00:00.000Z",
  "sourceService": "product-service",
  "version": "1.0",
  "payload": {
    "productId": "prod_12345",
    "producerId": "producer_67890",
    "updatedFields": ["pricePerUnit", "quantity"],
    "oldValues": {
      "pricePerUnit": 85.50,
      "quantity": 1000.0
    },
    "newValues": {
      "pricePerUnit": 80.00,
      "quantity": 800.0
    },
    "updateReason": "Price adjustment and quantity sold"
  }
}
```

---

## 💬 QUOTE EVENTS

### Topic: `quote.request.created.v1`

**Description**: Published when a buyer creates a new quote request

**Schema**:
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Quote Request Created Event",
  "type": "object",
  "required": ["eventId", "eventType", "timestamp", "sourceService", "payload"],
  "properties": {
    "eventId": {"type": "string"},
    "eventType": {"type": "string", "enum": ["quote.request.created"]},
    "timestamp": {"type": "string", "format": "date-time"},
    "sourceService": {"type": "string", "enum": ["quote-service"]},
    "version": {"type": "string", "enum": ["1.0"]},
    "payload": {
      "type": "object",
      "required": ["quoteRequestId", "buyerId", "productId"],
      "properties": {
        "quoteRequestId": {"type": "string"},
        "buyerId": {"type": "string"},
        "productId": {"type": "string"},
        "producerId": {"type": "string"},
        "requestDetails": {
          "type": "object",
          "properties": {
            "requestedQuantity": {"type": "number"},
            "maxPrice": {"type": "number"},
            "deliveryLocation": {"type": "string"},
            "requiredByDate": {"type": "string", "format": "date"},
            "additionalRequirements": {"type": "string"}
          }
        },
        "expiresAt": {"type": "string", "format": "date-time"}
      }
    }
  }
}
```

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440007",
  "eventType": "quote.request.created",
  "timestamp": "2025-11-15T15:00:00.000Z",
  "sourceService": "quote-service",
  "version": "1.0",
  "payload": {
    "quoteRequestId": "qr_12345",
    "buyerId": "buyer_98765",
    "productId": "prod_12345",
    "producerId": "producer_67890",
    "requestDetails": {
      "requestedQuantity": 500.0,
      "maxPrice": 80.00,
      "deliveryLocation": "Delhi, India",
      "requiredByDate": "2025-11-20",
      "additionalRequirements": "Need quality certificate"
    },
    "expiresAt": "2025-11-18T15:00:00.000Z"
  }
}
```

---

### Topic: `quote.response.created.v1`

**Description**: Published when a producer submits a quote response

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440008",
  "eventType": "quote.response.created",
  "timestamp": "2025-11-15T16:00:00.000Z",
  "sourceService": "quote-service",
  "version": "1.0",
  "payload": {
    "quoteResponseId": "qres_67890",
    "quoteRequestId": "qr_12345",
    "producerId": "producer_67890",
    "buyerId": "buyer_98765",
    "responseDetails": {
      "offeredPrice": 78.00,
      "offeredQuantity": 500.0,
      "deliveryTerms": "FOB warehouse, 2 days delivery",
      "paymentTerms": "50% advance, 50% on delivery",
      "validUntil": "2025-11-17T16:00:00.000Z",
      "notes": "Premium quality guaranteed with certificates"
    }
  }
}
```

---

### Topic: `quote.accepted.v1`

**Description**: Published when a buyer accepts a quote response

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440009",
  "eventType": "quote.accepted",
  "timestamp": "2025-11-15T17:00:00.000Z",
  "sourceService": "quote-service",
  "version": "1.0",
  "payload": {
    "quoteRequestId": "qr_12345",
    "quoteResponseId": "qres_67890",
    "buyerId": "buyer_98765",
    "producerId": "producer_67890",
    "acceptedTerms": {
      "finalPrice": 78.00,
      "finalQuantity": 500.0,
      "totalAmount": 39000.00,
      "deliveryTerms": "FOB warehouse, 2 days delivery",
      "paymentTerms": "50% advance, 50% on delivery"
    },
    "acceptedAt": "2025-11-15T17:00:00.000Z"
  }
}
```

---

### Topic: `quote.rejected.v1`

**Description**: Published when a buyer rejects a quote or quote request is rejected

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440010",
  "eventType": "quote.rejected",
  "timestamp": "2025-11-15T17:30:00.000Z",
  "sourceService": "quote-service",
  "version": "1.0",
  "payload": {
    "quoteRequestId": "qr_12346",
    "quoteResponseId": "qres_67891",
    "buyerId": "buyer_98765",
    "producerId": "producer_67891",
    "rejectionReason": "Price too high for our budget",
    "rejectedBy": "BUYER",
    "rejectedAt": "2025-11-15T17:30:00.000Z"
  }
}
```

---

## 📋 ORDER EVENTS

### Topic: `order.created.v1`

**Description**: Published when a new order is created from an accepted quote

**Schema**:
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Order Created Event",
  "type": "object",
  "required": ["eventId", "eventType", "timestamp", "sourceService", "payload"],
  "properties": {
    "eventId": {"type": "string"},
    "eventType": {"type": "string", "enum": ["order.created"]},
    "timestamp": {"type": "string", "format": "date-time"},
    "sourceService": {"type": "string", "enum": ["order-service"]},
    "version": {"type": "string", "enum": ["1.0"]},
    "payload": {
      "type": "object",
      "required": ["orderId", "buyerId", "producerId", "productId"],
      "properties": {
        "orderId": {"type": "string"},
        "buyerId": {"type": "string"},
        "producerId": {"type": "string"},
        "productId": {"type": "string"},
        "quoteResponseId": {"type": "string"},
        "orderDetails": {
          "type": "object",
          "properties": {
            "quantity": {"type": "number"},
            "unitPrice": {"type": "number"},
            "totalPrice": {"type": "number"},
            "shippingCost": {"type": "number"},
            "taxAmount": {"type": "number"},
            "finalAmount": {"type": "number"},
            "deliveryAddress": {"type": "string"},
            "deliveryDate": {"type": "string", "format": "date"},
            "orderNotes": {"type": "string"}
          }
        },
        "orderStatus": {"type": "string", "enum": ["PENDING"]},
        "deliveryStatus": {"type": "string", "enum": ["NOT_STARTED"]}
      }
    }
  }
}
```

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440011",
  "eventType": "order.created",
  "timestamp": "2025-11-15T18:00:00.000Z",
  "sourceService": "order-service",
  "version": "1.0",
  "payload": {
    "orderId": "order_12345",
    "buyerId": "buyer_98765",
    "producerId": "producer_67890",
    "productId": "prod_12345",
    "quoteResponseId": "qres_67890",
    "orderDetails": {
      "quantity": 500.0,
      "unitPrice": 78.00,
      "totalPrice": 39000.00,
      "shippingCost": 2000.00,
      "taxAmount": 4140.00,
      "finalAmount": 45140.00,
      "deliveryAddress": "123 Market Street, Delhi, India - 110001",
      "deliveryDate": "2025-11-20",
      "orderNotes": "Please ensure quality certificates are included"
    },
    "orderStatus": "PENDING",
    "deliveryStatus": "NOT_STARTED"
  }
}
```

---

### Topic: `order.confirmed.v1`

**Description**: Published when an order is confirmed by the producer

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440012",
  "eventType": "order.confirmed",
  "timestamp": "2025-11-15T19:00:00.000Z",
  "sourceService": "order-service",
  "version": "1.0",
  "payload": {
    "orderId": "order_12345",
    "buyerId": "buyer_98765",
    "producerId": "producer_67890",
    "previousStatus": "PENDING",
    "newStatus": "CONFIRMED",
    "confirmedBy": "producer_67890",
    "confirmedAt": "2025-11-15T19:00:00.000Z",
    "confirmationNotes": "Order confirmed, will be shipped in 2 days"
  }
}
```

---

### Topic: `order.cancelled.v1`

**Description**: Published when an order is cancelled

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440013",
  "eventType": "order.cancelled",
  "timestamp": "2025-11-15T20:00:00.000Z",
  "sourceService": "order-service",
  "version": "1.0",
  "payload": {
    "orderId": "order_12346",
    "buyerId": "buyer_98766",
    "producerId": "producer_67891",
    "previousStatus": "CONFIRMED",
    "newStatus": "CANCELLED",
    "cancelledBy": "buyer_98766",
    "cancellationReason": "Change in business requirements",
    "cancelledAt": "2025-11-15T20:00:00.000Z",
    "refundRequired": true
  }
}
```

---

### Topic: `order.delivery.updated.v1`

**Description**: Published when delivery status is updated

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440014",
  "eventType": "order.delivery.updated",
  "timestamp": "2025-11-16T10:00:00.000Z",
  "sourceService": "order-service",
  "version": "1.0",
  "payload": {
    "orderId": "order_12345",
    "buyerId": "buyer_98765",
    "producerId": "producer_67890",
    "deliveryUpdate": {
      "previousStatus": "IN_TRANSIT",
      "newStatus": "DELIVERED",
      "trackingNumber": "TRK123456789",
      "courierName": "FastShip Express",
      "updatedAt": "2025-11-16T10:00:00.000Z",
      "deliveryNotes": "Package delivered to recipient",
      "deliveryLocation": {
        "latitude": 28.6139,
        "longitude": 77.2090
      }
    }
  }
}
```

---

## 💳 PAYMENT EVENTS

### Topic: `payment.escrow.created.v1`

**Description**: Published when an escrow account is created for an order

**Schema**:
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Escrow Created Event",
  "type": "object",
  "required": ["eventId", "eventType", "timestamp", "sourceService", "payload"],
  "properties": {
    "eventId": {"type": "string"},
    "eventType": {"type": "string", "enum": ["payment.escrow.created"]},
    "timestamp": {"type": "string", "format": "date-time"},
    "sourceService": {"type": "string", "enum": ["payment-service"]},
    "version": {"type": "string", "enum": ["1.0"]},
    "payload": {
      "type": "object",
      "required": ["escrowId", "orderId", "buyerId", "producerId"],
      "properties": {
        "escrowId": {"type": "string"},
        "orderId": {"type": "string"},
        "buyerId": {"type": "string"},
        "producerId": {"type": "string"},
        "escrowDetails": {
          "type": "object",
          "properties": {
            "amount": {"type": "number"},
            "currency": {"type": "string"},
            "paymentMethod": {"type": "string"},
            "holdDurationDays": {"type": "integer"},
            "releaseConditions": {"type": "string"},
            "releaseScheduledAt": {"type": "string", "format": "date-time"}
          }
        },
        "escrowStatus": {"type": "string", "enum": ["CREATED"]}
      }
    }
  }
}
```

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440015",
  "eventType": "payment.escrow.created",
  "timestamp": "2025-11-15T19:30:00.000Z",
  "sourceService": "payment-service",
  "version": "1.0",
  "payload": {
    "escrowId": "escrow_12345",
    "orderId": "order_12345",
    "buyerId": "buyer_98765",
    "producerId": "producer_67890",
    "escrowDetails": {
      "amount": 45140.00,
      "currency": "INR",
      "paymentMethod": "BANK_TRANSFER",
      "holdDurationDays": 7,
      "releaseConditions": "Automatic release after delivery confirmation or manual release by buyer",
      "releaseScheduledAt": "2025-11-23T19:30:00.000Z"
    },
    "escrowStatus": "CREATED"
  }
}
```

---

### Topic: `payment.escrow.released.v1`

**Description**: Published when escrow funds are released to the producer

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440016",
  "eventType": "payment.escrow.released",
  "timestamp": "2025-11-20T15:00:00.000Z",
  "sourceService": "payment-service",
  "version": "1.0",
  "payload": {
    "escrowId": "escrow_12345",
    "orderId": "order_12345",
    "buyerId": "buyer_98765",
    "producerId": "producer_67890",
    "releaseDetails": {
      "releasedAmount": 45140.00,
      "releaseType": "FULL",
      "releaseTrigger": "DELIVERY_CONFIRMED",
      "platformFee": 1354.20,
      "producerAmount": 43785.80,
      "releasedAt": "2025-11-20T15:00:00.000Z",
      "transactionId": "txn_release_001",
      "releaseNotes": "Payment released after successful delivery"
    }
  }
}
```

---

### Topic: `payment.escrow.refunded.v1`

**Description**: Published when escrow funds are refunded to the buyer

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440017",
  "eventType": "payment.escrow.refunded",
  "timestamp": "2025-11-16T12:00:00.000Z",
  "sourceService": "payment-service",
  "version": "1.0",
  "payload": {
    "escrowId": "escrow_12346",
    "orderId": "order_12346",
    "buyerId": "buyer_98766",
    "producerId": "producer_67891",
    "refundDetails": {
      "refundAmount": 25000.00,
      "refundType": "FULL",
      "refundReason": "Order cancelled by buyer",
      "refundMethod": "BANK_TRANSFER",
      "refundedAt": "2025-11-16T12:00:00.000Z",
      "transactionId": "txn_refund_001",
      "refundNotes": "Full refund processed due to order cancellation"
    }
  }
}
```

---

## 🔔 NOTIFICATION EVENTS

### Topic: `notification.send.requested.v1`

**Description**: Published when a notification needs to be sent to a user

**Sample Event**:
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440018",
  "eventType": "notification.send.requested",
  "timestamp": "2025-11-15T19:00:00.000Z",
  "sourceService": "order-service",
  "version": "1.0",
  "payload": {
    "notificationId": "notif_12345",
    "userId": "buyer_98765",
    "notificationType": "ORDER_CONFIRMED",
    "channels": ["EMAIL", "SMS", "PUSH"],
    "priority": "HIGH",
    "template": "order_confirmation",
    "templateData": {
      "orderId": "order_12345",
      "producerName": "John Doe Farm",
      "productTitle": "Premium Basmati Rice",
      "quantity": "500 KG",
      "totalAmount": "₹45,140",
      "deliveryDate": "2025-11-20"
    },
    "recipient": {
      "userId": "buyer_98765",
      "email": "buyer@agrimart.com",
      "phone": "+919876543210",
      "preferredLanguage": "en"
    }
  }
}
```

---

## 🏗️ KAFKA INFRASTRUCTURE

### Topic Configuration

```yaml
topics:
  # User & KYC Topics
  - name: user.profile.created.v1
    partitions: 3
    replication-factor: 3
    retention: 7d
    
  - name: user.kyc.submitted.v1
    partitions: 3
    replication-factor: 3
    retention: 30d
    
  - name: user.kyc.verified.v1
    partitions: 3
    replication-factor: 3
    retention: 365d
    
  - name: user.kyc.rejected.v1
    partitions: 3
    replication-factor: 3
    retention: 365d

  # Product Topics
  - name: product.lot.created.v1
    partitions: 6
    replication-factor: 3
    retention: 7d
    
  - name: product.lot.updated.v1
    partitions: 6
    replication-factor: 3
    retention: 7d

  # Quote Topics
  - name: quote.request.created.v1
    partitions: 6
    replication-factor: 3
    retention: 30d
    
  - name: quote.response.created.v1
    partitions: 6
    replication-factor: 3
    retention: 30d
    
  - name: quote.accepted.v1
    partitions: 3
    replication-factor: 3
    retention: 365d
    
  - name: quote.rejected.v1
    partitions: 3
    replication-factor: 3
    retention: 30d

  # Order Topics
  - name: order.created.v1
    partitions: 6
    replication-factor: 3
    retention: 365d
    
  - name: order.confirmed.v1
    partitions: 3
    replication-factor: 3
    retention: 365d
    
  - name: order.cancelled.v1
    partitions: 3
    replication-factor: 3
    retention: 365d
    
  - name: order.delivery.updated.v1
    partitions: 6
    replication-factor: 3
    retention: 365d

  # Payment Topics
  - name: payment.escrow.created.v1
    partitions: 3
    replication-factor: 3
    retention: 365d
    
  - name: payment.escrow.released.v1
    partitions: 3
    replication-factor: 3
    retention: 365d
    
  - name: payment.escrow.refunded.v1
    partitions: 3
    replication-factor: 3
    retention: 365d

  # Notification Topics
  - name: notification.send.requested.v1
    partitions: 6
    replication-factor: 3
    retention: 7d
```

### Consumer Groups

```yaml
consumer-groups:
  # Notification Service Consumers
  - group-id: notification-service-orders
    topics: 
      - order.created.v1
      - order.confirmed.v1
      - order.cancelled.v1
      - order.delivery.updated.v1
    
  - group-id: notification-service-payments
    topics:
      - payment.escrow.created.v1
      - payment.escrow.released.v1
      - payment.escrow.refunded.v1
  
  # Analytics Service Consumers
  - group-id: analytics-service
    topics:
      - user.profile.created.v1
      - product.lot.created.v1
      - order.created.v1
      - quote.accepted.v1
      
  # Audit Service Consumers
  - group-id: audit-service
    topics:
      - user.kyc.verified.v1
      - user.kyc.rejected.v1
      - payment.escrow.released.v1
      - payment.escrow.refunded.v1

  # Search Index Consumers
  - group-id: search-indexer
    topics:
      - product.lot.created.v1
      - product.lot.updated.v1
      - user.profile.created.v1
```

### Schema Registry Configuration

```yaml
schema-registry:
  url: http://localhost:8081
  compatibility-level: BACKWARD
  
schemas:
  - subject: user.profile.created.v1-value
    schema-type: JSON
    
  - subject: order.created.v1-value
    schema-type: JSON
    
  - subject: payment.escrow.created.v1-value
    schema-type: JSON
```

### Dead Letter Queues

```yaml
dead-letter-topics:
  - name: dlq.user.events
    source-topics:
      - user.profile.created.v1
      - user.kyc.submitted.v1
      - user.kyc.verified.v1
      - user.kyc.rejected.v1
    retention: 30d
    
  - name: dlq.order.events
    source-topics:
      - order.created.v1
      - order.confirmed.v1
      - order.cancelled.v1
    retention: 30d
    
  - name: dlq.payment.events
    source-topics:
      - payment.escrow.created.v1
      - payment.escrow.released.v1
      - payment.escrow.refunded.v1
    retention: 30d
```

## Event Processing Patterns

### 1. Saga Pattern for Order Processing
- **Order Created** → **Escrow Created** → **Inventory Reserved** → **Order Confirmed**
- Compensation events for failures at each step

### 2. Event Sourcing for Audit Trail
- All state changes captured as events
- Ability to replay events for debugging
- Complete audit trail for compliance

### 3. CQRS Integration
- Events update read models for queries
- Separate write and read concerns
- Optimized query performance

### 4. Circuit Breaker for External Services
- Payment gateway integration
- SMS/Email service integration
- File upload services

This comprehensive Kafka topic design ensures reliable, scalable, and maintainable event-driven communication across all microservices in the ProcGrid platform.