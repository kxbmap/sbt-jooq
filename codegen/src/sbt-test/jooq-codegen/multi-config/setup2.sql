create table company (
  id         bigint       not null  primary key auto_increment,
  name       varchar(255) not null,
  created_at timestamp    not null
);

insert into company (name, created_at) values
  ('Example Co., Ltd.', current_timestamp),
  ('Example Corp.', current_timestamp),
  ('Example Inc.', current_timestamp),
  ('Example KK.', current_timestamp);
