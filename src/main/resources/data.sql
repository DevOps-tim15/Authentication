CREATE TABLE IF NOT EXISTS authorities
(
`id` INTEGER AUTO_INCREMENT,
`user_type` VARCHAR(255),
PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_t
(
`id` INTEGER AUTO_INCREMENT,
`first_name` VARCHAR(255),
`last_name` VARCHAR(255),
`email` VARCHAR(255),
`username` VARCHAR(255),
`password` VARCHAR(255),
`phone` VARCHAR(255),
`website_url` VARCHAR(255),
`sex` VARCHAR(255),
`birth_date` VARCHAR(255),
`biography` VARCHAR(255),
`verified` BIT(1),
`canBeTagged` BIT(1),
`isPrivate` BIT(1),
PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_authority
(
`user_id` INTEGER,
`authority_id` INTEGER,
FOREIGN KEY (user_id) REFERENCES user_t(id),
FOREIGN KEY (authority_id) REFERENCES authorities(id)
);

CREATE TABLE IF NOT EXISTS verification_tokens
(
`id` INTEGER AUTO_INCREMENT,
`token` VARCHAR(255),
`user_id` INTEGER,
PRIMARY KEY (id),
FOREIGN KEY (user_id) REFERENCES user_t(id)
);


INSERT INTO `authorities` (`id`,`user_type`) VALUES ('1','ROLE_REGISTERED_USER');
INSERT INTO `authorities` (`id`,`user_type`) VALUES ('2','ROLE_AGENT');
INSERT INTO `authorities` (`id`,`user_type`) VALUES ('3','ROLE_ADMIN');

INSERT INTO `user_t` (`id`,`first_name`, `last_name`, `email`, `username`, `password`, `phone`, `website_url`, `sex`, `birth_date`, `biography`, `verified`, `canBeTagged`, `isPrivate`) 
VALUES ('5','Jova', 'Jovic', 'jova.jovic@gmail.com', 'jova', '$2a$12$ix4Ep6eG2ajt5yjWpAlRHusH1srR8GXdh0FvrgRWnVv2hZVRWEhoC', '0601234567', 'somesite.com', 'male', '01.01.01.', 'bio', 1, 1, 0);

INSERT INTO user_authority (user_id, authority_id) VALUES (5, 1);

INSERT INTO verification_tokens (`id`, `token`, `user_id`) VALUES (5, 'joca-token', 5);
