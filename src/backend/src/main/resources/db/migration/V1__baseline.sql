CREATE TABLE Category (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Name NVARCHAR(255) NOT NULL,
    Description NVARCHAR(MAX),
    ThumbnailUrl NVARCHAR(500),
    ParentCategoryId INT NULL,
    CONSTRAINT FK_Category_Parent FOREIGN KEY (ParentCategoryId) REFERENCES Category(Id)
);

CREATE TABLE Product (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Name NVARCHAR(255) NOT NULL,
    Description NVARCHAR(MAX),
    Price DECIMAL(18,2) NOT NULL,
    CategoryId INT NOT NULL,
    ImageUrl NVARCHAR(500),
    ThumbnailUrl NVARCHAR(500),
    IsActive BIT NOT NULL DEFAULT 1,
    CreatedDate DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    CONSTRAINT FK_Product_Category FOREIGN KEY (CategoryId) REFERENCES Category(Id)
);

CREATE TABLE ProductImage (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    ProductId INT NOT NULL,
    ImageUrl NVARCHAR(500) NOT NULL,
    IsPrimary BIT NOT NULL DEFAULT 0,
    DisplayOrder INT NOT NULL DEFAULT 0,
    CONSTRAINT FK_ProductImage_Product FOREIGN KEY (ProductId) REFERENCES Product(Id) ON DELETE CASCADE
);

CREATE TABLE ProductVariant (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    ProductId INT NOT NULL,
    VariantName NVARCHAR(255) NOT NULL,
    VariantValue NVARCHAR(255) NOT NULL,
    PriceAdjustment DECIMAL(18,2) NOT NULL DEFAULT 0,
    StockQuantity INT NOT NULL DEFAULT 0,
    CONSTRAINT FK_ProductVariant_Product FOREIGN KEY (ProductId) REFERENCES Product(Id) ON DELETE CASCADE
);

CREATE TABLE Customer (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    IdentityUserId NVARCHAR(255),
    Email NVARCHAR(255) NOT NULL,
    FirstName NVARCHAR(255) NOT NULL,
    LastName NVARCHAR(255) NOT NULL,
    PhoneNumber NVARCHAR(50),
    CreatedDate DATETIME2 NOT NULL DEFAULT GETUTCDATE()
);

CREATE TABLE Address (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    CustomerId INT NOT NULL,
    AddressType NVARCHAR(50) NOT NULL,
    StreetAddress NVARCHAR(500) NOT NULL,
    City NVARCHAR(255) NOT NULL,
    StateProvince NVARCHAR(255),
    PostalCode NVARCHAR(50),
    Country NVARCHAR(255) NOT NULL,
    IsDefault BIT NOT NULL DEFAULT 0,
    CONSTRAINT FK_Address_Customer FOREIGN KEY (CustomerId) REFERENCES Customer(Id) ON DELETE CASCADE
);

CREATE TABLE CartItem (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    SessionId NVARCHAR(255),
    ProductId INT NOT NULL,
    VariantId INT NULL,
    CustomerId INT NULL,
    Quantity INT NOT NULL DEFAULT 1,
    UnitPrice DECIMAL(18,2) NOT NULL DEFAULT 0,
    CreatedDate DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    CONSTRAINT FK_CartItem_Product FOREIGN KEY (ProductId) REFERENCES Product(Id),
    CONSTRAINT FK_CartItem_Variant FOREIGN KEY (VariantId) REFERENCES ProductVariant(Id),
    CONSTRAINT FK_CartItem_Customer FOREIGN KEY (CustomerId) REFERENCES Customer(Id)
);

CREATE TABLE [Order] (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    CustomerId INT NOT NULL,
    OrderDate DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    OrderStatus NVARCHAR(50) NOT NULL DEFAULT 'Paid',
    OrderNumber NVARCHAR(50) NOT NULL,
    SubTotal DECIMAL(18,2) NOT NULL DEFAULT 0,
    ShippingTotal DECIMAL(18,2) NOT NULL DEFAULT 0,
    TaxTotal DECIMAL(18,2) NOT NULL DEFAULT 0,
    GrandTotal DECIMAL(18,2) NOT NULL DEFAULT 0,
    ShippingAddressId INT NULL,
    BillingAddressId INT NULL,
    IdempotencyKey NVARCHAR(255) NULL,
    CONSTRAINT FK_Order_Customer FOREIGN KEY (CustomerId) REFERENCES Customer(Id),
    CONSTRAINT FK_Order_ShippingAddress FOREIGN KEY (ShippingAddressId) REFERENCES Address(Id),
    CONSTRAINT FK_Order_BillingAddress FOREIGN KEY (BillingAddressId) REFERENCES Address(Id)
);

CREATE TABLE OrderItem (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    OrderId INT NOT NULL,
    ProductId INT NOT NULL,
    ProductName NVARCHAR(255) NOT NULL,
    Quantity INT NOT NULL DEFAULT 1,
    UnitPrice DECIMAL(18,2) NOT NULL DEFAULT 0,
    LineTotal DECIMAL(18,2) NOT NULL DEFAULT 0,
    CONSTRAINT FK_OrderItem_Order FOREIGN KEY (OrderId) REFERENCES [Order](Id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IX_Order_IdempotencyKey ON [Order](IdempotencyKey) WHERE IdempotencyKey IS NOT NULL;
