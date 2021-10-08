create table "book_to_book_store"
(
    "name"    varchar2(400) not null,
    "book_id" integer       not null,
    "stock"   integer,

    primary key ("name", "book_id"),
    constraint "fk_b2bs_book_store" foreign key ("name") references "book_store" ("name") on delete cascade,
    constraint "fk_b2bs_book" foreign key ("book_id") references "book" ("id") on delete cascade
);
