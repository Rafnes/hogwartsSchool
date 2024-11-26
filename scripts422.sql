create table persons (
	id int4 primary key,
	name text not null,
	age int4 not null check (age > 0),
	car_id int4 references cars(id),
	has_driver_license boolean not null
	);

create table cars (
	id int4 primary key,
	brand text not null,
	model text not null,
	price numeric(10, 2) not null
)

alter table persons alter column car_id set not null;

insert into cars
(id, brand, model, price)
values(1, 'Toyota', 'Corolla', 12000.00);

insert into cars
(id, brand, model, price)
values(2, 'Ford', 'Focus', 15000.00);

insert into cars 
(id, brand, model, price)
values (3, 'Ford', 'F-150', 20000.00);

insert into persons (id, name, age, car_id, has_driver_license) values (1, 'Joe Wells', 19, 1, true);
insert into persons (id, name, age, car_id, has_driver_license) values (2, 'Chris Jackson', 35, 2, true);
insert into persons (id, name, age, car_id, has_driver_license) values (3, 'Helen Hurth', 31, 1, true);
insert into persons (id, name, age, car_id, has_driver_license) values (4, 'Marie Slummer', 15, 3, false);
insert into persons (id, name, age, car_id, has_driver_license) values (5, 'Fred Wells', 14, 1, false);