package mate.academy.onlinebookstore.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mate.academy.onlinebookstore.exception.DataProcessingException;
import mate.academy.onlinebookstore.model.Book;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepository {
    private final EntityManagerFactory entityManagerFactory;

    @Override
    public Book save(Book book) {
        EntityManager entityManager = null;
        EntityTransaction transaction = null;

        try {
            entityManager = entityManagerFactory.createEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(book);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
                throw new DataProcessingException("Can't insert book to DB: " + book, e);
            }
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        return book;
    }

    @Override
    public List<Book> findAll() {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return entityManager.createQuery("from Book", Book.class).getResultList();
        } catch (Exception e) {
            throw new DataProcessingException("Can't find all books", e);
        }
    }

    @Override
    public Optional<Book> findById(long id) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return Optional.ofNullable(entityManager
                    .createQuery("from Book where id = :id", Book.class)
                    .setParameter("id", id)
                    .getSingleResult());
        } catch (Exception e) {
            throw new DataProcessingException("Can't get book by id: " + id, e);
        }
    }
}
