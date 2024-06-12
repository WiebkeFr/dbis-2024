
import psycopg2

# Establish a connection to the PostgreSQL database
connection = psycopg2.connect(
    database="your_database",
    user="your_username",
    password="your_password",
    host="your_host",
    port="your_port"  # default port is 5432
)

# Create a cursor object
cursor = connection.cursor()

# Define a SQL query with placeholders for parameters
sql_query = "INSERT INTO table_name (column1, column2) VALUES (%s, %s)"

# Define a list of parameter tuples for multiple rows
data = [
    (value1_row1, value2_row1),
    (value1_row2, value2_row2),
]

# Execute the batch insert using executemany()
cursor.executemany(sql_query, data)

# Commit the transaction
connection.commit()

# Close the cursor and connection
cursor.close()
connection.close()

def extract_csv():
    print("Extract CSV")

def extract_sql():
    print("Extract SQL")