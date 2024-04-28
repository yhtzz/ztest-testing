package customer.ztest_testing.handlers;

import static cds.gen.catalogservice.CatalogService_.BOOKS;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.sap.cds.Result;
import com.sap.cds.Struct;
import com.sap.cds.ql.Insert;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.Update;
import com.sap.cds.ql.cqn.CqnAnalyzer;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.reflect.CdsModel;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.cds.CdsReadEventContext;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.messages.Messages;
import com.sap.cds.services.persistence.PersistenceService;
import com.sap.cds.services.request.FeatureTogglesInfo;

import cds.gen.catalogservice.Books;
import cds.gen.catalogservice.Books_;
import cds.gen.catalogservice.CatalogService_;

import cds.gen.catalogservice.SubmitOrderContext;
import customer.ztest_testing.MessageKeys;

/**
 * Custom business logic for the "Catalog Service" (see cat-service.cds)
 *
 * Handles Reading of Books
 *
 * Adds Discount Message to the Book Title if too much stock is available
 *
 * Provides adding book reviews
 */
@Component
@ServiceName(CatalogService_.CDS_NAME)
class CatalogServiceHandler implements EventHandler {
	private final PersistenceService db;

	public CatalogServiceHandler(PersistenceService db) {
		this.db = db;
	}

	@On
	public void onSubmitOrder(SubmitOrderContext context) {
		Integer quantity = context.getQuantity();
		String bookId = context.getBook();

		Optional<Books> book = db.run(Select.from(BOOKS).columns(Books_::stock).byId(bookId)).first(Books.class);

		book.orElseThrow(() -> new ServiceException(ErrorStatuses.NOT_FOUND, MessageKeys.BOOK_MISSING)
				.messageTarget(Books_.class, b -> b.ID()));

		int stock = book.map(Books::getStock).get();

		if (stock >= quantity) {
			db.run(Update.entity(BOOKS).byId(bookId).data(Books.STOCK, stock -= quantity));
			SubmitOrderContext.ReturnType result = SubmitOrderContext.ReturnType.create();
			result.setStock(stock);
			context.setResult(result);
		} else {
			throw new ServiceException(ErrorStatuses.CONFLICT, MessageKeys.ORDER_EXCEEDS_STOCK, quantity);
		}
	}

	@After(event = CqnService.EVENT_READ)
	public void discountBooks(Stream<Books> stream) {
		stream.filter(b -> b.getTitle() != null).forEach(b -> {
			loadStockIfNotSet(b);
			discountBooksWithMoreThan111Stock(b);
		});
	}

	private void discountBooksWithMoreThan111Stock(Books b) {
		if (b.getStock() != null && b.getStock() > 111) {
			b.setTitle(String.format("%s -- 11%% discount", b.getTitle()));
		}
	}

	private void loadStockIfNotSet(Books b) {
		if (b.getId() != null && b.getStock() == null) {
			b.setStock(
					db.run(Select.from(BOOKS).byId(b.getId()).columns(Books_::stock)).single(Books.class).getStock());
		}
	}

}
