create table "book"
(
    "id"           number(7)     not null primary key,
    "author_id"    number(7)     not null,
    "title"        varchar2(400) not null,
    "published_in" number(7)     not null,
    "language_id"  number(7)     not null,

    constraint "fk_book_author" foreign key ("author_id") references "author" ("id"),
    constraint "fk_book_language" foreign key ("language_id") references "language" ("id")
);
