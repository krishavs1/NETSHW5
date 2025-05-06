-- users table
CREATE TABLE users (
  id         SERIAL PRIMARY KEY,
  email      VARCHAR(200) UNIQUE NOT NULL,
  password   VARCHAR(60)  NOT NULL,
  username   VARCHAR(100) UNIQUE NOT NULL,
  school     TEXT,
  major      TEXT,
  grad_year  INT,
  interests  TEXT,
  bio        TEXT,
  registered BOOLEAN DEFAULT TRUE
);

-- resume piece tables
CREATE TABLE experiences (
  id      SERIAL PRIMARY KEY,
  user_id INT REFERENCES users(id) ON DELETE CASCADE,
  entry   TEXT
);
CREATE TABLE skills (
  id      SERIAL PRIMARY KEY,
  user_id INT REFERENCES users(id) ON DELETE CASCADE,
  skill   TEXT
);
CREATE TABLE hobbies (
  id      SERIAL PRIMARY KEY,
  user_id INT REFERENCES users(id) ON DELETE CASCADE,
  hobby   TEXT
);

-- friendships
CREATE TABLE friendships (
  user_id   INT REFERENCES users(id) ON DELETE CASCADE,
  friend_id INT REFERENCES users(id) ON DELETE CASCADE,
  PRIMARY KEY(user_id, friend_id)
);
