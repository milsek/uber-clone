INSERT INTO role (name)
VALUES ('ROLE_USER');
INSERT INTO role (name)
VALUES ('ROLE_ADMIN');
INSERT INTO role (name)
VALUES ('ROLE_PASSENGER');
INSERT INTO role (name)
VALUES ('ROLE_DRIVER');

INSERT INTO vehicle_type (name, price, seats)
VALUES ('COUPE', 100, 4);
INSERT INTO vehicle_type (name, price, seats)
VALUES ('MINIVAN', 150, 6);
INSERT INTO vehicle_type (name, price, seats)
VALUES ('STATION', 125, 4);

/*
INSERT INTO passenger
    (username, email, password, name, surname, phone_number,
     city, profile_picture, authentication_provider,
     account_status, payment_details, distance_travelled,
     rides_completed, token_balance)
VALUES
    ('passenger@noemail.com', 'passenger@noemail.com', 'cascaded',
     'Albert', 'Einstein', '+13515543534', 'Bern', '/default.png',
     'LOCAL', 0, '', 5.0, 1, 1000);
INSERT INTO user_role(user_id, role_id) VALUES (1, 3);
-- DRIVER WITH NO CURRENT RIDE
INSERT INTO vehicle
    (baby_seat, pets_allowed, make, model, colour,
     license_plate_number, ride_active, current_lat,
     current_lng, next_lat, next_lng, coordinates_changed_at,
     expected_trip_time, vehicle_type_id)
VALUES
    (false, true, 'Volkswagen',
     'Passat', 'Gray', 'NN1111TX',
     false, 45.241805, 19.798567,
     45.241805, 19.798567, '2022-12-31 23.59.59',
     0, 1);
INSERT INTO driver
(username, email, password, name, surname, phone_number,
 city, profile_picture, authentication_provider,
 account_status, active, distance_travelled, rides_completed,
 total_rating_sum, number_of_reviews, active_minutes_today,
 last_set_active, vehicle_id, current_ride_id, next_ride_id)
VALUES
    ('driver1@noemail.com', 'driver1@noemail.com', 'cascaded',
     'Vincent', 'Van Gogh', '+11111111111', 'Novi Sad', '/default.png',
     'LOCAL', 0, true, 0, 0, 0, 0, 0, '2022-12-31 23.59.59',
     1, null, null);
-- DRIVER WITH A CURRENT RIDE
INSERT INTO vehicle
(baby_seat, pets_allowed, make, model, colour,
 license_plate_number, ride_active, current_lat,
 current_lng, next_lat, next_lng, coordinates_changed_at,
 expected_trip_time, vehicle_type_id)
VALUES
    (true, true, 'Peugeot',
     '308', 'White', 'NN2222TX',
     false, 45.241805, 19.798567,
     45.241805, 19.798567, '2022-12-31 23.59.59',
     0, 1);
INSERT INTO driver
(username, email, password, name, surname, phone_number,
 city, profile_picture, authentication_provider,
 account_status, active, distance_travelled, rides_completed,
 total_rating_sum, number_of_reviews, active_minutes_today,
 last_set_active, vehicle_id, current_ride_id, next_ride_id)
VALUES
    ('driver2@noemail.com', 'driver2@noemail.com', 'cascaded',
     'Albrecht', 'Durer', '+22222222222', 'Novi Sad', '/default.png',
     'LOCAL', 0, true, 4.5, 1, 5, 1, 0, '2022-12-31 23.59.59',
     1, null, null);
INSERT INTO route () VALUES ();
INSERT INTO route_coordinates (lat, lng, route_id)
VALUES (45.241805, 19.798567, 1);
INSERT INTO route_coordinates (lat, lng, route_id)
VALUES (45.245749, 19.851122, 1);
INSERT INTO route_waypoints (lat, lng, route_id)
VALUES (45.241805, 19.798567, 1);
INSERT INTO route_waypoints (lat, lng, route_id)
VALUES (45.245749, 19.851122, 1);
INSERT INTO ride
    (distance, expected_time, driver_rejection_reason, status,
     start_address, destination_address, passengers_confirmed,
     start_time, end_time, created_at, driver_inconsistency_reported,
     price, vehicle_type, baby_seat_requested, pet_friendly_requested,
     delay_in_minutes, driver_username, route_id)
VALUES
    (4.5, 350, null, 1, 'Example start address 1, Novi Sad',
    'Example destination address 1, Novi Sad',
    true, '2022-01-29 23.54.29', '2022-12-29 23.58.59',
    '2022-12-29 23.51.59', false, 510,
    'COUPE', false, true, 0, 'driver2@noemail.com', 1);
UPDATE driver
SET current_ride_id = 1
WHERE username = 'driver2@noemail.com';
-- Another ride
INSERT INTO route () VALUES ();
INSERT INTO route_coordinates (lat, lng, route_id)
VALUES (45.241805, 19.798567, 2);
INSERT INTO route_coordinates (lat, lng, route_id)
VALUES (45.245749, 19.851122, 2);
INSERT INTO route_waypoints (lat, lng, route_id)
VALUES (45.241805, 19.798567, 2);
INSERT INTO route_waypoints (lat, lng, route_id)
VALUES (45.245749, 19.851122, 2);
INSERT INTO ride
(distance, expected_time, driver_rejection_reason, status,
 start_address, destination_address, passengers_confirmed,
 start_time, end_time, created_at, driver_inconsistency_reported,
 price, vehicle_type, baby_seat_requested, pet_friendly_requested,
 delay_in_minutes, driver_username, route_id)
VALUES
    (9.0, 1000, null, 7, 'Example start address 2, Novi Sad',
     'Example destination address 2, Novi Sad',
     true, '2023-01-28 23.45.29', '2022-12-28 23.58.59',
     '2022-01-28 23.44.59', false, 900,
     'COUPE', false, true, 0, 'driver2@noemail.com', 2);
*/
