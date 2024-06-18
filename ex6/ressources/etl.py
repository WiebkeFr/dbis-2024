import psycopg2
import pandas as pd
from fuzzywuzzy import fuzz
from fuzzywuzzy import process
import time
from datetime import datetime


CSV_FILE_PATH = "sales.csv"

"""
Notes:
removed line 35906, due to it having two dates (?)
06.04.2019;12.03.2019;Superstore Dresden;AEG �ko Lavatherm 59850 Sensidry;3;2997,00

No encoding worked correctly for the umlaute, so i think maybe someone previously saved iso-8859-1 as utf-8.
All umlaute have the same encoding value.
"""


def extract_csv(csv_file_path, encoding="utf-8"):
    print("Extract CSV")
    data = pd.read_csv(csv_file_path, sep=";", encoding=encoding)
    return data


def extract_transform_sql(read_connection):
    print("Extract SQL")
    cursor = read_connection.cursor()

    shop_query = """
                SELECT s.ShopID, ci.Name as City, r.Name as Region, co.Name as Country, s.Name
                FROM Shop s
                JOIN City ci ON s.CityID = ci.CityID
                JOIN Region r ON ci.RegionID = r.RegionID
                JOIN Country co ON r.CountryID = co.CountryID;
                """
    cursor.execute(shop_query)
    store_data = cursor.fetchall()
    store_df = pd.DataFrame(store_data, columns=['ShopID', 'City', 'Region', 'Country', 'Name'])
    store_df.drop(columns=['ShopID'], inplace=True)

    article_query = """
                    SELECT a.ArticleID, pg.Name as ProductGroup, pf.Name as ProductFamily, a.Name, a.Price
                    FROM Article a
                    JOIN ProductGroup pg ON a.ProductGroupID = pg.ProductGroupID
                    JOIN ProductFamily pf ON pg.ProductFamilyID = pf.ProductFamilyID;
                    """
    cursor.execute(article_query)
    article_data = cursor.fetchall()
    article_df = pd.DataFrame(article_data, columns=['ArticleID', 'ProductGroup', 'ProductFamily', 'Name', 'Price'])
    article_df.drop(columns=['ArticleID'], inplace=True)
    cursor.close()
    return article_df, store_df


def load_articles(connection, article_df):
    cursor = connection.cursor()
    insert_sql = '''
    INSERT INTO DW_Article (ProductGroup, ProductFamily, Name, Price) 
    VALUES (%s, %s, %s, %s)
    '''
    data_touples = [(row['ProductGroup'], row['ProductFamily'], row['Name'], row['Price']) for index, row in
                    article_df.iterrows()]
    cursor.executemany(insert_sql, data_touples)
    cursor.close()
    connection.commit()


def load_stores(connection, store_df):
    cursor = connection.cursor()
    insert_sql = '''
    INSERT INTO DW_Shop (City, Region, Country, Name) 
    VALUES (%s, %s, %s, %s)
    '''
    data_touples = [(row['City'], row['Region'], row['Country'], row['Name']) for index, row in store_df.iterrows()]

    cursor.executemany(insert_sql, data_touples)
    cursor.close()
    connection.commit()


def handle_missing_id(mapping, missing):
    # find 3 closest matches for missing value in mapping keys
    options = mapping.keys()
    closest_matches = process.extract(missing, options, limit=3, scorer=fuzz.partial_ratio)
    print(f"Missing value: {missing}")
    print("Closest matches:")
    for i, match in enumerate(closest_matches):
        print(f"{i}: {match[0]}")
    print("3: None of the above")
    selection_valid = False
    selection = -1
    while not selection_valid:
        time.sleep(2)
        selection = input()
        try:
            selection = int(selection)
            assert (selection in range(4))
        except:
            print("Invalid selection, please try again")
            continue
        selection_valid = True
    return mapping[closest_matches[selection][0]] if selection != 3 else None


def convert_date_format(date_str):
    dt = datetime.strptime(date_str, '%d.%m.%Y')
    return dt.strftime('%Y-%m-%d')


def transform_load_sales(connection, sales):
    cursor = connection.cursor()
    cursor.execute("SELECT Name, ShopID FROM DW_Shop")
    store_names = cursor.fetchall()
    store_to_id = {name: id for name, id in store_names}
    cursor.execute("SELECT Name, ArticleId FROM DW_Article")
    article_names = cursor.fetchall()
    article_to_id = {name: id for name, id in article_names}
    data_touples = []
    for _, sale in sales.iterrows():
        if sale["Article"] not in article_to_id:
            article_id = handle_missing_id(article_to_id, sale["Article"])
            if article_id is None:
                print(f"Article {sale['Article']} not found, sale will not be added")
                continue
            article_to_id[sale["Article"]] = article_id
        if sale["Shop"] not in store_to_id:
            store_id = handle_missing_id(store_to_id, sale["Shop"])
            if store_id is None:
                print(f"Store {sale['Shop']} not found, sale will not be added")
                continue
            store_to_id[sale["Shop"]] = store_id
        data_touples.append((convert_date_format(sale["Date"]), store_to_id[sale["Shop"]],
                             article_to_id[sale["Article"]], sale["Sold"], float(sale["Revenue"].replace(",", "."))))
    insert_sql = '''
    INSERT INTO DW_Sale (SaleDate, ShopID, ArticleID, Amount, Revenue)
    VALUES (%s, %s, %s, %s, %s)
    '''

    cursor.executemany(insert_sql, data_touples)
    cursor.close()
    connection.commit()


if __name__ == '__main__':
    connection = psycopg2.connect(
        database="postgres",
        user="admin",
        password="password",
        host="localhost",
        port="5432"
    )
    sales_df = extract_csv(CSV_FILE_PATH)
    article_df, store_df = extract_transform_sql(connection)
    load_articles(connection, article_df)
    load_stores(connection, store_df)
    transform_load_sales(connection, sales_df)
    connection.close()
