create table employee (
  id         bigint       not null  primary key auto_increment,
  name       varchar(255) not null,
  created_at timestamp    not null
);

insert into employee (name, created_at) values
  ('Alice', current_timestamp),
  ('Bob', current_timestamp),
  ('Carol', current_timestamp),
  ('Dave', current_timestamp);
