create table employee
(
    id         bigint       not null primary key auto_increment,
    name       varchar(255) not null,
    created_at timestamp    not null
);
