package customer.ztest_testing.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.sap.cds.Result;
import com.sap.cds.ql.Select;
import com.sap.cds.services.cds.CqnService;

import cds.gen.catalogservice.Books;
import cds.gen.catalogservice.Books_;
import cds.gen.catalogservice.CatalogService_;
import cds.gen.catalogservice.SubmitOrderContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CatalogServiceTest {

    @Autowired
    @Qualifier(CatalogService_.CDS_NAME)
    private CqnService catalogService;

    @Test
    public void discountApplied() {
        Result result = catalogService.run(Select.from(Books_.class).byId("51061ce3-ddde-4d70-a2dc-6314afbcc73e"));

        // book with title "The Raven" and a stock quantity of > 111
        Books book = result.single(Books.class);
        // 断言判断结果
        assertEquals("The Raven -- 11% discount", book.getTitle(), "Book was not discounted");
        // 打印结果
        System.out.println(book.getTitle());
    }

    @Test
    public void submitOrder() {
        SubmitOrderContext context = SubmitOrderContext.create();
        // 读取提交订单前图书库存数量
        Result result = catalogService.run(Select.from(Books_.class).byId("4a519e61-3c3a-4bd9-ab12-d7e0c5329933"));
        Books book = result.single(Books.class);
        // 提交数量2的订单
        // ID of a book known to have a stock quantity of 22
        context.setBook("4a519e61-3c3a-4bd9-ab12-d7e0c5329933");
        context.setQuantity(2);
        catalogService.emit(context);
        // 断言
        assertEquals(book.getStock() - context.getQuantity(), context.getResult().getStock(),
                "结果错误预期20实际" + context.getResult().getStock());
        // 打印结果
        System.out.println(book.getStock());
        System.out.println(context.getQuantity());
        System.out.println(book.getStock() - context.getQuantity());
        System.out.println(context.getResult().getStock());

    }
}