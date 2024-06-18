import psycopg2
import pandas as pd
import time
from IPython.display import clear_output

connection = psycopg2.connect(
    database="postgres",
    user="admin",
    password="password",
    host="localhost",
    port="5432"
)

geo_choices = {
    "shop": {"table": "DW_Shop", "column": "Name"},
    "city": {"table": "DW_Shop", "column": "City"},
    "region": {"table": "DW_Shop", "column": "Region"},
    "country": {"table": "DW_Shop", "column": "Country"}
}
product_choices = {
    "product": {"table": "DW_Article", "column": "Name"},
    "productGroup": {"table": "DW_Article", "column": "ProductGroup"},
    "productFamily": {"table": "DW_Article", "column": "ProductFamily"},
    "productCategory": {"table": "DW_Article", "column": "ProductCategory"}
}
time_choices = {
    "date": {"table": "DW_Sale", "column": "SaleDate"},
    "day": {"table": "DW_Sale", "column": "SaleDate"},
    "dow": {"table": "DW_Sale", "column": "SaleDate"},
    "month": {"table": "DW_Sale", "column": "SaleDate"},
    "quarter": {"table": "DW_Sale", "column": "SaleDate"},
    "year": {"table": "DW_Sale", "column": "SaleDate"}
}

def time_choice_to_sql(time_choice):
    if time_choice == "date":
        return f"{time_choices[time_choice]['column']}"
    return f"extract({time_choice} from {time_choices[time_choice]['column']})"


def analysis(geo_choice, product_choice, time_choice):
    cursor = connection.cursor()
    query = f"""
    SELECT sh.{geo_choices[geo_choice]["column"]}, ar.{product_choices[product_choice]["column"]}, {time_choice_to_sql(time_choice)} as {time_choice}, SUM(Revenue)
    FROM DW_Sale sa
    JOIN DW_Shop sh ON sa.ShopID = sh.ShopID
    JOIN DW_Article ar ON sa.ArticleID = ar.ArticleID
    GROUP BY sh.{geo_choices[geo_choice]["column"]}, ar.{product_choices[product_choice]["column"]}, {time_choice}
    ORDER BY sh.{geo_choices[geo_choice]["column"]}, ar.{product_choices[product_choice]["column"]}, {time_choice};
    """
    cursor.execute(query)
    data = cursor.fetchall()
    return data

def make_choice(choices):
    print("Please make a choice:")
    for i, choice in enumerate(choices):
        print(f"{i + 1}: {choice}")
    time.sleep(1)
    while True:
        try:
            choice = int(input())
            if choice not in range(1, len(choices) + 1):
                raise ValueError
            break
        except ValueError:
            print("Please enter a valid choice")
    clear_output()
    return choices[choice - 1]


def analasys_interface():
    geo_choice = make_choice(list(geo_choices.keys()))
    product_choice = make_choice(list(product_choices.keys()))
    time_choice = make_choice(list(time_choices.keys()))
    data = analysis(geo_choice, product_choice, time_choice)
    df = pd.DataFrame(data, columns=[geo_choice, product_choice, time_choice, "Revenue"])
    pivot_df = df.pivot_table(index=[geo_choice, product_choice], columns=time_choice, values="Revenue")
    print(pivot_df)
    return df, pivot_df

if __name__ == "__main__":
    while True:
        df, pivot_df = analasys_interface()
        print("Would you like to save the data?")
        choice = input("y/n: ")
        if choice == "y":
            filename = input("Please enter a filename: ")
            pivot_df.to_csv(f"{filename.strip()}.csv")
            print("Data saved")
        print("Would you like to perform another analysis?")
        choice = input("y/n: ")
        if choice == "n":
            break
        clear_output()
