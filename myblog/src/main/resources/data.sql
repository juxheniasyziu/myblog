INSERT IGNORE INTO authorities VALUES (1,'ROLE_ADMIN');
INSERT IGNORE INTO authorities VALUES (2,'ROLE_EDITOR');
INSERT IGNORE INTO authorities VALUES (3,'ROLE_READER');


INSERT IGNORE INTO user_authorities (`user_id`, `authority_id`) VALUES ('1', '1');
INSERT IGNORE INTO user_authorities (`user_id`, `authority_id`) VALUES ('1', '2');
INSERT IGNORE INTO user_authorities (`user_id`, `authority_id`) VALUES ('2', '2');
INSERT IGNORE INTO user_authorities (`user_id`, `authority_id`) VALUES ('1', '3');
