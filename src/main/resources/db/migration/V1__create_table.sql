CREATE TABLE `book_status` (
  status VARCHAR(20) PRIMARY KEY
);

CREATE TABLE `books` (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title TEXT NOT NULL,
  price INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  FOREIGN KEY (status) REFERENCES book_status (status)
);

CREATE TABLE `authors` (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  birth_date DATE NOT NULL,
  UNIQUE KEY `uk_authors_name` (`name`)
);

CREATE TABLE `book_author_relations` (
  book_id INT NOT NULL,
  author_id INT NOT NULL,
  PRIMARY KEY (book_id, author_id),
  FOREIGN KEY (book_id) REFERENCES books (id),
  FOREIGN KEY (author_id) REFERENCES authors (id)
);

INSERT INTO book_status (status) VALUES ('PUBLISHED');
INSERT INTO book_status (status) VALUES ('UNPUBLISHED');