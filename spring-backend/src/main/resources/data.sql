insert into admin values ('admin','LOCAL', 'city','mail','name','$2a$10$w/bdL7W/IvnbuWfNqrd0SOS8b27MVC9kxVjrtVWA0rXZ/RPJsN8CK','num','pic','surname');

INSERT INTO role (name)
VALUES ('ROLE_USER');
INSERT INTO role (name)
VALUES ('ROLE_ADMIN');
INSERT INTO role (name)
VALUES ('ROLE_PASSENGER');
INSERT INTO role (name)
VALUES ('ROLE_DRIVER');

INSERT INTO user_role (user_id, role_id)
VALUES ('test', 3);

INSERT INTO user_role (user_id, role_id)
VALUES ('admin', 2);

INSERT INTO vehicle_type (name, price, seats)
VALUES ('COUPE', 100, 4);

INSERT INTO vehicle_type (name, price, seats)
VALUES ('MINIVAN', 150, 6);

INSERT INTO vehicle_type (name, price, seats)
VALUES ('STATION', 125, 4);


insert into route values (1);
insert into route values (2);
insert into route values  (3);
insert into route values  (4);
insert into route values  (5);
