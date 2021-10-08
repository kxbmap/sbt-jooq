create table "author"
(
    "id"            number(7)    not null primary key,
    "first_name"    varchar2(50),
    "last_name"     varchar2(50) not null,
    "date_of_birth" date,
    "year_of_birth" number(7),
    "distinguished" number(1)
);
