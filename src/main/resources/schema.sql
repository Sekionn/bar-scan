CREATE TABLE products
(
    id UUID NOT NULL
    barcode varchar(100) NOT NULL
    shelfOfOrigin INT NOT NULL
    amountCounted INT NOT NULL
    countedDate datetime NOT NULL
    PRIMARY KEY (Id)
)