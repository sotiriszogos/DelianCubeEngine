1. First you need to setup the database.
(*) Create a mysql database with the schema of the pkdd99 database which is in file pkdd99_star_fullSchemaCreation.sql (execute it first via e.g., MySQL Workbench)
(*) The mysql workbench model is in file pkdd99.mwb

The tables are of two kinds:
Dimension tables: account, date, status, payment_reason
Fact tables: loan, orders

2. Once the database setup is done, you need to add to your the user 
   CinecubesUser 
  with password 
    Cinecubes
(or update the client.java to work with your own users)


3. The folder PerTable_DataDump contains csv files with the data for all tables.
       Load the data by importing the data in any of the two following ways:
a) Edit the path for the upload folder with the data files (*.csv) in the script loadData.sql and run it
b) First load the *dimdata.sql for the dimension tables and then the *factdata.sql for the fact tables


4. To work with the dataset, one needs to define a connection, hierarchies and cubes.
Given in the directory, ready for use: 
Connection info (dbc.ini)
For the cube Loans: loans.ini
For the cube Orders: orders.ini

