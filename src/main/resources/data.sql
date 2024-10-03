--password is 'password' in bcrypt encoding
insert into users (email, password)
values ('orcun@example.com', '$2a$10$8fwn0LUKql6wTzJHO2QoQ.Nd.59eIyFwaucgBJoiZ/T5SqrqNmyBm');

insert into roles (authority) values ('ROLE_ADMIN');
insert into roles (authority) values ('ROLE_USER');

insert into user_role (user_id, role_id) values (1, 1);
insert into user_role (user_id, role_id) values (1, 2);
