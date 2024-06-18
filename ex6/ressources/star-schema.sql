CREATE TABLE DW_Sale (
  ShopID int NOT NULL,
  ArticleID int NOT NULL,
  SaleID SERIAL PRIMARY KEY,
  SaleDate DATE NOT NULL,
  Amount int NOT NULL,
  Revenue double precision NOT NULL
);

CREATE TABLE DW_Shop (
  ShopID SERIAL PRIMARY KEY,
  City varchar(255) NOT NULL,
  Region varchar(255) NOT NULL,
  Country varchar(255) NOT NULL,
  Name varchar(255) NOT NULL UNIQUE
);

CREATE TABLE DW_Article (
  ArticleID SERIAL PRIMARY KEY,
  ProductGroup varchar(255) NOT NULL,
  ProductFamily varchar(255) NOT NULL,
  Name varchar(255) NOT NULL UNIQUE,
  Price double precision NOT NULL
);

ALTER TABLE DW_Sale ADD CONSTRAINT ShopID_fk_1 FOREIGN KEY (ShopID) REFERENCES DW_Shop (ShopID);
ALTER TABLE DW_Sale ADD CONSTRAINT ArticleID_fk_1 FOREIGN KEY (ArticleID) REFERENCES DW_Article (ArticleID);

commit;
