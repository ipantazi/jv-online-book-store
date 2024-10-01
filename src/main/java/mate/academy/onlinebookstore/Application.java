package mate.academy.onlinebookstore;

import java.math.BigDecimal;
import mate.academy.onlinebookstore.model.Book;
import mate.academy.onlinebookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    @Autowired
    private BookService bookService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            Book book = new Book();
            book.setTitle("Macbeth");
            book.setAuthor("William Shakespeare");
            book.setIsbn("978-0091547714");
            book.setPrice(BigDecimal.valueOf(45));
            book.setDescription("The Boynton/Cook editions of four of Shakespeare's "
                    + "most popular plays...");
            book.setCoverImage("https://drive.google.com/file/d/"
                    + "1chUUpuyX34iDwmW0ZyCOX9kNJBLYbPzf/view?usp=sharing");

            bookService.save(book);

            System.out.println(bookService.findAll());
        };
    }
}
