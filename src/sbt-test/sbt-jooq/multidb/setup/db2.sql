create table company (
  id         bigint       not null primary key auto_increment,
  name       varchar(255) not null,
  url        varchar(255),
  created_at timestamp    not null
);

insert into company (name, url, created_at) values
  ('Lightbend', 'https://www.lightbend.com/', current_timestamp),
  ('Oracle', 'https://www.oracle.com/', current_timestamp),
  ('Google', 'https://www.google.com/', current_timestamp),
  ('Microsoft', 'https://www.microsoft.com/', current_timestamp);
