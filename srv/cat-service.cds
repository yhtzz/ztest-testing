using {my.bookshop as my} from '../db/index';

@path : 'browse'
service CatalogService @(requires: 'any') {
    @readonly
    entity Books       as projection on my.Books excluding {
        createdBy,
        modifiedBy
    };

    @readonly
    entity Authors     as projection on my.Authors;

    action submitOrder(book : Books : ID, quantity : Integer) returns {
        stock : Integer
    };

}
