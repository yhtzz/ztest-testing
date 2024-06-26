package customer.ztest_testing.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import com.sap.cds.services.persistence.PersistenceService;
import cds.gen.catalogservice.Books;

@ExtendWith(MockitoExtension.class)
public class CatalogServiceHandlerTest {
    @Mock
    private PersistenceService db;

    @Test
    void testDiscountBooks() {
        Books book1 = Books.create();
        book1.setTitle("Book 1");
        book1.setStock(10);

        Books book2 = Books.create();
        book2.setTitle("Book 2");
        book2.setStock(200);

        CatalogServiceHandler handler = new CatalogServiceHandler(db);
        handler.discountBooks(Stream.of(book1, book2));
        // 断言判断结果
        assertEquals("Book 1", book1.getTitle(), "Book 1 was discounted");
        assertEquals("Book 2 -- 11% discount", book2.getTitle(), "Book 2 was not discounted");
        // 打印结果
        System.out.println(book1);
        System.out.println(book2);
    }

    @Test
    void testOnSubmitOrder() {

    }
}
