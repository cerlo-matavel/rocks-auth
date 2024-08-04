CREATE TABLE APP_USER (
                      id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
                         username VARCHAR(255) UNIQUE NOT NULL,
                         email VARCHAR(255) UNIQUE NOT NULL,
                         password VARCHAR(255) NOT NULL,
                         role VARCHAR(255) NOT NULL
);
