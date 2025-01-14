package onlinebookstore.repository.book;

import java.util.List;
import lombok.RequiredArgsConstructor;
import onlinebookstore.exception.DataProcessingException;
import onlinebookstore.model.Book;
import onlinebookstore.repository.SpecificationProvider;
import onlinebookstore.repository.SpecificationProviderManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookSpecificationProviderManager implements SpecificationProviderManager<Book> {
    private final List<SpecificationProvider<Book>> bookSpecificationProviders;

    @Override
    public SpecificationProvider<Book> getSpecificationProvider(String key) {
        return bookSpecificationProviders.stream()
                .filter(provider -> provider.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new DataProcessingException("Can't find correct specification "
                        + "provider for key: " + key));
    }
}
