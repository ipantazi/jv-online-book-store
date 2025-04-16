update books set title = 'Test Book 101',
                 author = 'Test Author 101',
                 price = 101.00,
                 description = 'Test Description',
                 cover_image = 'http://example.com/test-cover.jpg',
                 is_deleted = 0
             where id = 101;

delete from books_categories where book_id = 101;

INSERT INTO books_categories (book_id, category_id) VALUES (101, 101)
    ON DUPLICATE KEY UPDATE book_id = book_id;
