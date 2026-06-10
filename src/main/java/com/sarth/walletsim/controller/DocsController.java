package com.sarth.walletsim.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DocsController {

    @GetMapping(value = "/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> apiDocs() {
        return ResponseEntity.ok("""
                {
                  "openapi": "3.0.3",
                  "info": {
                    "title": "Spring Boot Wallet Simulator API",
                    "version": "1.0.0",
                    "description": "API contract for JWT authentication, KYC, wallet funding, transfers, transaction history, and refund simulation."
                  },
                  "servers": [
                    {
                      "url": "http://localhost:8080",
                      "description": "Local development server"
                    }
                  ],
                  "paths": {
                    "/api/v1/auth/register": {
                      "post": {
                        "summary": "Register a user account and return a JWT",
                        "requestBody": {
                          "required": true,
                          "content": {
                            "application/json": {
                              "schema": { "$ref": "#/components/schemas/RegisterRequest" }
                            }
                          }
                        },
                        "responses": {
                          "200": { "description": "Account created" },
                          "400": { "description": "Invalid registration request" }
                        }
                      }
                    },
                    "/api/v1/auth/login": {
                      "post": {
                        "summary": "Authenticate a user and return a JWT",
                        "requestBody": {
                          "required": true,
                          "content": {
                            "application/json": {
                              "schema": { "$ref": "#/components/schemas/AuthRequest" }
                            }
                          }
                        },
                        "responses": {
                          "200": { "description": "Signed in" },
                          "401": { "description": "Invalid credentials" }
                        }
                      }
                    },
                    "/api/v1/wallet/kyc/upload": {
                      "post": {
                        "summary": "Submit KYC details and a document",
                        "security": [{ "bearerAuth": [] }],
                        "requestBody": {
                          "required": true,
                          "content": {
                            "multipart/form-data": {
                              "schema": {
                                "type": "object",
                                "required": ["userEmail", "documentType", "documentNumber", "mobileNumber", "document"],
                                "properties": {
                                  "userEmail": { "type": "string", "format": "email" },
                                  "documentType": { "type": "string", "enum": ["PASSPORT", "DRIVERS_LICENSE", "NATIONAL_ID", "VOTER_ID"] },
                                  "documentNumber": { "type": "string" },
                                  "mobileNumber": { "type": "string", "pattern": "^\\\\d{10}$" },
                                  "document": { "type": "string", "format": "binary" }
                                }
                              }
                            }
                          }
                        },
                        "responses": {
                          "200": { "description": "KYC submitted for review" },
                          "400": { "description": "Invalid KYC request" }
                        }
                      }
                    },
                    "/api/v1/admin/kyc/{kycId}/approve": {
                      "post": {
                        "summary": "Approve a KYC submission and activate a wallet",
                        "security": [{ "bearerAuth": [] }],
                        "parameters": [
                          {
                            "name": "kycId",
                            "in": "path",
                            "required": true,
                            "schema": { "type": "integer", "format": "int64" }
                          }
                        ],
                        "responses": {
                          "200": { "description": "Wallet activated" },
                          "400": { "description": "KYC cannot be approved" }
                        }
                      }
                    },
                    "/api/v1/wallet/fund": {
                      "post": {
                        "summary": "Add simulated bank funds to a wallet",
                        "security": [{ "bearerAuth": [] }],
                        "requestBody": {
                          "required": true,
                          "content": {
                            "application/json": {
                              "schema": { "$ref": "#/components/schemas/FundingRequest" }
                            }
                          }
                        },
                        "responses": {
                          "200": { "description": "Wallet funded" },
                          "404": { "description": "Wallet not found" }
                        }
                      }
                    },
                    "/api/v1/wallet/transfer": {
                      "post": {
                        "summary": "Transfer funds between wallets",
                        "security": [{ "bearerAuth": [] }],
                        "requestBody": {
                          "required": true,
                          "content": {
                            "application/json": {
                              "schema": { "$ref": "#/components/schemas/TransferRequest" }
                            }
                          }
                        },
                        "responses": {
                          "200": { "description": "Transfer completed" },
                          "400": { "description": "Invalid transfer or insufficient balance" }
                        }
                      }
                    },
                    "/api/v1/wallet/simulate-failure": {
                      "post": {
                        "summary": "Simulate receiver-bank failure and refund compensation",
                        "security": [{ "bearerAuth": [] }],
                        "requestBody": {
                          "required": true,
                          "content": {
                            "application/json": {
                              "schema": { "$ref": "#/components/schemas/TransferRequest" }
                            }
                          }
                        },
                        "responses": {
                          "200": { "description": "Failure simulation completed" }
                        }
                      }
                    },
                    "/api/v1/wallet/{upiId}": {
                      "get": {
                        "summary": "Fetch wallet balance and profile summary",
                        "security": [{ "bearerAuth": [] }],
                        "parameters": [
                          {
                            "name": "upiId",
                            "in": "path",
                            "required": true,
                            "schema": { "type": "string", "example": "9876543210@upi" }
                          }
                        ],
                        "responses": {
                          "200": { "description": "Wallet found" },
                          "404": { "description": "Wallet not found" }
                        }
                      }
                    },
                    "/api/v1/wallet/{upiId}/transactions": {
                      "get": {
                        "summary": "Fetch recent wallet transactions",
                        "security": [{ "bearerAuth": [] }],
                        "parameters": [
                          {
                            "name": "upiId",
                            "in": "path",
                            "required": true,
                            "schema": { "type": "string", "example": "9876543210@upi" }
                          }
                        ],
                        "responses": {
                          "200": { "description": "Recent wallet transactions" },
                          "404": { "description": "Wallet not found" }
                        }
                      }
                    }
                  },
                  "components": {
                    "securitySchemes": {
                      "bearerAuth": {
                        "type": "http",
                        "scheme": "bearer",
                        "bearerFormat": "JWT"
                      }
                    },
                    "schemas": {
                      "RegisterRequest": {
                        "type": "object",
                        "required": ["fullName", "email", "password"],
                        "properties": {
                          "fullName": { "type": "string", "example": "Demo User" },
                          "email": { "type": "string", "format": "email", "example": "user@wallet.dev" },
                          "password": { "type": "string", "format": "password", "example": "User@123" }
                        }
                      },
                      "AuthRequest": {
                        "type": "object",
                        "required": ["email", "password"],
                        "properties": {
                          "email": { "type": "string", "format": "email", "example": "admin@wallet.dev" },
                          "password": { "type": "string", "format": "password", "example": "Admin@123" }
                        }
                      },
                      "FundingRequest": {
                        "type": "object",
                        "required": ["upiId", "amount", "bankReferenceId"],
                        "properties": {
                          "upiId": { "type": "string", "example": "9876543210@upi" },
                          "amount": { "type": "number", "minimum": 0.01, "example": 500.00 },
                          "bankReferenceId": { "type": "string", "example": "BANK-REF-1001" }
                        }
                      },
                      "TransferRequest": {
                        "type": "object",
                        "required": ["senderUpiId", "receiverUpiId", "amount"],
                        "properties": {
                          "senderUpiId": { "type": "string", "example": "9876543210@upi" },
                          "receiverUpiId": { "type": "string", "example": "9123456780@upi" },
                          "amount": { "type": "number", "minimum": 0.01, "example": 125.50 }
                        }
                      }
                    }
                  }
                }
                """);
    }
}
