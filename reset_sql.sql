DROP DATABASE IF EXISTS bank_db;


CREATE DATABASE IF NOT EXISTS bank_db;

USE bank_db;


CREATE TABLE IF NOT EXISTS customers (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(30) NOT NULL,
    surname VARCHAR(30) NOT NULL,
    phone VARCHAR(30) UNIQUE,
    email VARCHAR(50),
    PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS accounts (
    accountNumber BIGINT UNSIGNED NOT NULL,
    customerID BIGINT UNSIGNED NOT NULL,
    type ENUM ( "current", "savings", "card" ),
    currency ENUM ( "AMD", "RUB", "USD" ) NOT NULL,
    balance DECIMAL(20, 5) UNSIGNED NOT NULL,
    PRIMARY KEY (accountNumber),
    FOREIGN KEY (customerID) REFERENCES customers(id)
);


CREATE TABLE IF NOT EXISTS cards (
    cardNumber BIGINT UNSIGNED NOT NULL,
    accountNumber BIGINT UNSIGNED NOT NULL,
    type ENUM ( "debit", "credit" ) NOT NULL,
    pin CHAR(6) NOT NULL,
    pinHash CHAR(88) NOT NULL,
    pinSalt CHAR(88) NOT NULL,
    PRIMARY KEY (cardNumber),
    FOREIGN KEY (accountNumber) REFERENCES accounts(accountNumber)
);


CREATE TABLE IF NOT EXISTS atms (
    id VARCHAR(100) NOT NULL,
    address VARCHAR(100) NOT NULL,
    currency ENUM ( "AMD", "RUB", "USD" ) NOT NULL,
    PRIMARY KEY (id)
);


INSERT INTO customers (
    name,
    surname,
    phone,
    email
) VALUES (
    "Artak",
    "Kirakosyan",
    "+37499099448",
    "abc90cba@mail.ru"
);


INSERT INTO customers (
    name,
    surname,
    phone,
    email
) VALUES (
    "Tatevik",
    "Khachaturyan",
    "+37411111111",
    "cccccccc@mail.ru"
);


INSERT INTO customers (
    name,
    surname,
    phone,
    email
) VALUES (
    "Eduard",
    "Matveev",
    "+37455555555",
    "qqqqqqqq@mail.ru"
);


INSERT INTO accounts (
    accountNumber,
    customerID,
    type,
    currency,
    balance
) VALUES (
    1111111111111111,
    1,
    "current",
    "AMD",
    560000
);


INSERT INTO accounts (
    accountNumber,
    customerID,
    type,
    currency,
    balance
) VALUES (
    2222222222222222,
    1,
    "savings",
    "AMD",
    34500000
);


INSERT INTO accounts (
    accountNumber,
    customerID,
    type,
    currency,
    balance
) VALUES (
    3333333333333333,
    1,
    "card",
    "AMD",
    120000
);


INSERT INTO accounts (
    accountNumber,
    customerID,
    type,
    currency,
    balance
) VALUES (
    4444444444444444,
    2,
    "current",
    "AMD",
    23000
);


INSERT INTO accounts (
    accountNumber,
    customerID,
    type,
    currency,
    balance
) VALUES (
    5555555555555555,
    3,
    "current",
    "AMD",
    200000
);


INSERT INTO accounts (
    accountNumber,
    customerID,
    type,
    currency,
    balance
) VALUES (
    6666666666666666,
    3,
    "card",
    "AMD",
    120000
);


INSERT INTO accounts (
    accountNumber,
    customerID,
    type,
    currency,
    balance
) VALUES (
    6666666666666666,
    3,
    "savings",
    "AMD",
    39837458937
);


INSERT INTO cards (
    cardNumber,
    accountNumber,
    type,
    pin,
    pinSalt,
    pinHash
) VALUES (
    9999999999999999,
    1111111111111111,
    "debit",
    "9999",
    "cHYgtlmyZKMnXzH1047InzpI/IrKzLDMAmboKTyI9NqG7ywvvaClsghXuCbafrm2nT3olp/4M+PZTQwOp0RIgA==",
    "FHqsslSuHBJwhI5Hv4TNM9tKfQIcJIX74Y9POBdCT8F84fZcdTnR+jRe+zGfySKIUIr99Lq/R8ejQttN3Ulm0A=="
);


INSERT INTO cards (
    cardNumber,
    accountNumber,
    type,
    pin,
    pinSalt,
    pinHash
) VALUES (
    8888888888888888,
    4444444444444444,
    "debit",
    "8888",
    "TdGQgZZufHcvlnktu/kLWqs9DOWH9D0Sltraveb9D5dGYENerquuJoheIQHTjAqtccZnhPXlN50zyp7mZNMctQ==",
    "m9hFPif/jnD80U0AIxdnTWiHullat3kF5FiDN+wEY8UKKOijk+R370/7XFDv7unVsOy/91sgVHRhZk+N+9Ld7g=="
);


INSERT INTO cards (
    cardNumber,
    accountNumber,
    type,
    pin,
    pinSalt,
    pinHash
) VALUES (
    7777777777777777,
    6666666666666666,
    "debit",
    "7777",
    "EgF5i6fQrD5bmDjvmxRlRx8jt4bSQGvtWt3wy2KNnIF0EO5vDwULvFZm6E+zbKnkEZc0mckzA4JHOZ9UKJcf7Q==",
    "lYuPrG0mufx3gW7LQVIOojw+otqepuSAqQRbX5UpV+g70k87nRCU+EwplBrF0I7yjV8anNuRpd6o0zI4hD+UUA=="
);


INSERT INTO atms (
    id,
    address,
    currency
) VALUES (
    "HAT_INECOBANK_ATM_021",
    "Komitas 36/7",
    "AMD"
);


SELECT * FROM customers;
SELECT * FROM accounts;
SELECT * FROM cards;
SELECT * FROM atms;


