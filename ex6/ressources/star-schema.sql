CREATE TABLE DW_Sale (
  ShopID int NOT NULL ,
  ArticleID int NOT NULL,
  SaleID int NOT NULL,
  SaleDate DATE NOT NULL,
  PRIMARY KEY (SaleID),
  Amount int NOT NULL,
);

CREATE TABLE DW_Shop (
  ShopID int NOT NULL ,
  City varchar(255) NOT NULL,
  Region varchar(255) NOT NULL,
  Country varchar(255) NOT NULL,
  Name varchar(255) NOT NULL,
  PRIMARY KEY (ShopID)
);

CREATE TABLE DW_Article (
  ArticleID int NOT NULL ,
  ProductGroup varchar(255) NOT NULL,
  ProductFamily varchar(255) NOT NULL,
  Name varchar(255) NOT NULL,
  Price double precision NOT NULL,
  PRIMARY KEY (ArticleID)
);

ALTER TABLE DW_Sale ADD CONSTRAINT ShopID_fk_1 FOREIGN KEY (ShopID) REFERENCES DW_Shop (ShopID);
ALTER TABLE DW_Sale ADD CONSTRAINT ArticleID_fk_1 FOREIGN KEY (ArticleID) REFERENCES DW_Article (ArticleID);
