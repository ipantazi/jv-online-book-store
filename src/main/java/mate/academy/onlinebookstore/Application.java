package mate.academy.onlinebookstore;

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
            Book macbeth = new Book();
            macbeth.setTitle("Macbeth");
            macbeth.setAuthor("William Shakespeare");
            macbeth.setIsbn("978-0091547714");

            bookService.save(macbeth);

            System.out.println(bookService.findAll());
        };
    }
}
