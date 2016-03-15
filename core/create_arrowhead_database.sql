CREATE DATABASE  IF NOT EXISTS `core` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `core`;
-- MySQL dump 10.13  Distrib 5.7.9, for Win64 (x86_64)
--
-- Host: localhost    Database: core
-- ------------------------------------------------------
-- Server version	5.7.10-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `arrowhead_cloud`
--

DROP TABLE IF EXISTS `arrowhead_cloud`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `arrowhead_cloud` (
  `id` int(11) NOT NULL,
  `authentication_info` varchar(255) DEFAULT NULL,
  `cloud_name` varchar(255) DEFAULT NULL,
  `operator` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9cjou6d7x3w0pvnnb27bc4c4d` (`operator`,`cloud_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `arrowhead_cloud`
--

LOCK TABLES `arrowhead_cloud` WRITE;
/*!40000 ALTER TABLE `arrowhead_cloud` DISABLE KEYS */;
/*!40000 ALTER TABLE `arrowhead_cloud` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `arrowhead_service`
--

DROP TABLE IF EXISTS `arrowhead_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `arrowhead_service` (
  `id` int(11) NOT NULL,
  `meta_data` varchar(255) DEFAULT NULL,
  `service_definition` varchar(255) DEFAULT NULL,
  `service_group` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKow5u4aa2pf2txupvsipl8o8db` (`service_group`,`service_definition`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `arrowhead_service`
--

LOCK TABLES `arrowhead_service` WRITE;
/*!40000 ALTER TABLE `arrowhead_service` DISABLE KEYS */;
/*!40000 ALTER TABLE `arrowhead_service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `arrowhead_system`
--

DROP TABLE IF EXISTS `arrowhead_system`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `arrowhead_system` (
  `id` int(11) NOT NULL,
  `ip_address` varchar(255) DEFAULT NULL,
  `authentication_info` varchar(255) DEFAULT NULL,
  `port` varchar(255) DEFAULT NULL,
  `system_group` varchar(255) DEFAULT NULL,
  `system_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfcoywnmdu0wm2km94onm855l8` (`system_group`,`system_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `arrowhead_system`
--

LOCK TABLES `arrowhead_system` WRITE;
/*!40000 ALTER TABLE `arrowhead_system` DISABLE KEYS */;
/*!40000 ALTER TABLE `arrowhead_system` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clouds_services`
--

DROP TABLE IF EXISTS `clouds_services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `clouds_services` (
  `id` int(11) NOT NULL,
  `cloud_id` int(11) DEFAULT NULL,
  `service_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKq7oa4r9xbm30wx6dfqw62t2i5` (`cloud_id`,`service_id`),
  KEY `FKkyfqutpyeb3hlc526bh8kkla` (`service_id`),
  CONSTRAINT `FKb851ly3g76roxuvo3atnfqc7o` FOREIGN KEY (`cloud_id`) REFERENCES `arrowhead_cloud` (`id`),
  CONSTRAINT `FKkyfqutpyeb3hlc526bh8kkla` FOREIGN KEY (`service_id`) REFERENCES `arrowhead_service` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clouds_services`
--

LOCK TABLES `clouds_services` WRITE;
/*!40000 ALTER TABLE `clouds_services` DISABLE KEYS */;
/*!40000 ALTER TABLE `clouds_services` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `core_system`
--

DROP TABLE IF EXISTS `core_system`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `core_system` (
  `id` int(11) NOT NULL,
  `ip_address` varchar(255) DEFAULT NULL,
  `authentication_info` varchar(255) DEFAULT NULL,
  `port` varchar(255) DEFAULT NULL,
  `service_uri` varchar(255) DEFAULT NULL,
  `system_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKow0cderx2j9knaajiwdyrad96` (`system_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `core_system`
--

LOCK TABLES `core_system` WRITE;
/*!40000 ALTER TABLE `core_system` DISABLE KEYS */;
/*!40000 ALTER TABLE `core_system` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hibernate_sequence`
--

LOCK TABLES `hibernate_sequence` WRITE;
/*!40000 ALTER TABLE `hibernate_sequence` DISABLE KEYS */;
INSERT INTO `hibernate_sequence` VALUES (1),(1),(1),(1),(1),(1),(1),(1);
/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Table structure for table `neighborhood`
--

DROP TABLE IF EXISTS `neighborhood`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `neighborhood` (
  `id` int(11) NOT NULL,
  `ip_address` varchar(255) DEFAULT NULL,
  `authentication_info` varchar(255) DEFAULT NULL,
  `cloud_name` varchar(255) DEFAULT NULL,
  `operator` varchar(255) DEFAULT NULL,
  `port` varchar(255) DEFAULT NULL,
  `service_uri` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKsqoqh6vfetcf8jo4928a22jaj` (`operator`,`cloud_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `neighborhood`
--

LOCK TABLES `neighborhood` WRITE;
/*!40000 ALTER TABLE `neighborhood` DISABLE KEYS */;
/*!40000 ALTER TABLE `neighborhood` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `own_cloud`
--

DROP TABLE IF EXISTS `own_cloud`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `own_cloud` (
  `id` int(11) NOT NULL,
  `ip_address` varchar(255) DEFAULT NULL,
  `authentication_info` varchar(255) DEFAULT NULL,
  `cloud_name` varchar(255) DEFAULT NULL,
  `operator` varchar(255) DEFAULT NULL,
  `port` varchar(255) DEFAULT NULL,
  `service_uri` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKk8xvsabjdcypcsij7qm72bsqv` (`operator`,`cloud_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `own_cloud`
--

LOCK TABLES `own_cloud` WRITE;
/*!40000 ALTER TABLE `own_cloud` DISABLE KEYS */;
/*!40000 ALTER TABLE `own_cloud` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `systems_services`
--

DROP TABLE IF EXISTS `systems_services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `systems_services` (
  `id` int(11) NOT NULL,
  `consumer_id` int(11) DEFAULT NULL,
  `provider_id` int(11) DEFAULT NULL,
  `service_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKnl06duvgwnau3mkekw90hctba` (`consumer_id`,`provider_id`,`service_id`),
  KEY `FKgkx12ee8t9bfm28jfstp0l9c4` (`provider_id`),
  KEY `FK6cpe7vapdht1wc1afq4tgbw3w` (`service_id`),
  CONSTRAINT `FK6cpe7vapdht1wc1afq4tgbw3w` FOREIGN KEY (`service_id`) REFERENCES `arrowhead_service` (`id`),
  CONSTRAINT `FK7i8f6y01rnirewctixqqm7pvp` FOREIGN KEY (`consumer_id`) REFERENCES `arrowhead_system` (`id`),
  CONSTRAINT `FKgkx12ee8t9bfm28jfstp0l9c4` FOREIGN KEY (`provider_id`) REFERENCES `arrowhead_system` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `systems_services`
--

LOCK TABLES `systems_services` WRITE;
/*!40000 ALTER TABLE `systems_services` DISABLE KEYS */;
/*!40000 ALTER TABLE `systems_services` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-03-15 15:36:04
