import pypyodbc

connection = pypyodbc.connect('Driver={SQL Server};'
                 'Server=map-it.database.windows.net',
                 'Database=map-it',
                 'user=segej87;password=J0nathan5!')

connection.close()