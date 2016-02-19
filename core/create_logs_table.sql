DROP TABLE IF EXISTS `logs`;
CREATE TABLE `logs`
   (
    `id` int NOT NULL AUTO_INCREMENT,
    `user_id` VARCHAR(20)    NOT NULL,
    `dated`   DATETIME       NOT NULL,
    `logger`  VARCHAR(256)   NOT NULL,
    `level`   VARCHAR(10)    NOT NULL,
    `message` VARCHAR(1000)  NOT NULL,
	PRIMARY KEY (`id`)
   );