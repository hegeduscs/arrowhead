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
-- Table structure for table `arrowheadcloud`
--

DROP TABLE IF EXISTS `arrowheadcloud`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `arrowheadcloud` (
  `id` int(11) NOT NULL,
  `authenticationInfo` varchar(255) DEFAULT NULL,
  `cloudName` varchar(255) DEFAULT NULL,
  `operator` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK3u1wgfsf3ayneoxt5264me60p` (`operator`,`cloudName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `arrowheadcloud`
--

LOCK TABLES `arrowheadcloud` WRITE;
/*!40000 ALTER TABLE `arrowheadcloud` DISABLE KEYS */;
/*!40000 ALTER TABLE `arrowheadcloud` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `arrowheadservice`
--

DROP TABLE IF EXISTS `arrowheadservice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `arrowheadservice` (
  `id` int(11) NOT NULL,
  `metaData` varchar(255) DEFAULT NULL,
  `serviceDefinition` varchar(255) DEFAULT NULL,
  `serviceGroup` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `arrowheadservice`
--

LOCK TABLES `arrowheadservice` WRITE;
/*!40000 ALTER TABLE `arrowheadservice` DISABLE KEYS */;
/*!40000 ALTER TABLE `arrowheadservice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `arrowheadservice_interfaces`
--

DROP TABLE IF EXISTS `arrowheadservice_interfaces`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `arrowheadservice_interfaces` (
  `ArrowheadService_id` int(11) NOT NULL,
  `interfaces` varchar(255) DEFAULT NULL,
  KEY `FKq6tn1xhrykgicexd7yryvjt1q` (`ArrowheadService_id`),
  CONSTRAINT `FKq6tn1xhrykgicexd7yryvjt1q` FOREIGN KEY (`ArrowheadService_id`) REFERENCES `arrowheadservice` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `arrowheadservice_interfaces`
--

LOCK TABLES `arrowheadservice_interfaces` WRITE;
/*!40000 ALTER TABLE `arrowheadservice_interfaces` DISABLE KEYS */;
/*!40000 ALTER TABLE `arrowheadservice_interfaces` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `arrowheadsystem`
--

DROP TABLE IF EXISTS `arrowheadsystem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `arrowheadsystem` (
  `id` int(11) NOT NULL,
  `IPAddress` varchar(255) DEFAULT NULL,
  `authenticationInfo` varchar(255) DEFAULT NULL,
  `port` varchar(255) DEFAULT NULL,
  `systemGroup` varchar(255) DEFAULT NULL,
  `systemName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `arrowheadsystem`
--

LOCK TABLES `arrowheadsystem` WRITE;
/*!40000 ALTER TABLE `arrowheadsystem` DISABLE KEYS */;
/*!40000 ALTER TABLE `arrowheadsystem` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clouds_services`
--

DROP TABLE IF EXISTS `clouds_services`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `clouds_services` (
  `ArrowheadCloud_id` int(11) NOT NULL,
  `serviceList_id` int(11) NOT NULL,
  KEY `FK7wcmwdn086ghlsrgde1ftq8kb` (`serviceList_id`),
  KEY `FKok587e699898ym434x0vsjqby` (`ArrowheadCloud_id`),
  CONSTRAINT `FK7wcmwdn086ghlsrgde1ftq8kb` FOREIGN KEY (`serviceList_id`) REFERENCES `arrowheadservice` (`id`),
  CONSTRAINT `FKok587e699898ym434x0vsjqby` FOREIGN KEY (`ArrowheadCloud_id`) REFERENCES `arrowheadcloud` (`id`)
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
-- Table structure for table `coresystem`
--

DROP TABLE IF EXISTS `coresystem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `coresystem` (
  `id` int(11) NOT NULL,
  `IPAddress` varchar(255) DEFAULT NULL,
  `authenticationInfo` varchar(255) DEFAULT NULL,
  `port` varchar(255) DEFAULT NULL,
  `serviceURI` varchar(255) DEFAULT NULL,
  `systemName` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqn7hgomoef12tl4um40rvw4fv` (`systemName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coresystem`
--

LOCK TABLES `coresystem` WRITE;
/*!40000 ALTER TABLE `coresystem` DISABLE KEYS */;
INSERT INTO `coresystem` VALUES (5,'http://localhost:','Public key of Orchestration System','8080','/core/orchestration','orchestration'),(6,'http://localhost:','Public key of Service Registry System','8080','/core/serviceregistry','serviceregistry'),(7,'http://localhost:','Public key of Authorization System','8080','/core/authorization','authorization'),(8,'http://localhost:','Public key of Gatekeeper System','8080','/core/gatekeeper','gatekeeper');
/*!40000 ALTER TABLE `coresystem` ENABLE KEYS */;
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
INSERT INTO `hibernate_sequence` VALUES (9),(9),(9),(9),(9);
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
  `IPAddress` varchar(255) DEFAULT NULL,
  `authenticationInfo` varchar(255) DEFAULT NULL,
  `cloudName` varchar(255) DEFAULT NULL,
  `operator` varchar(255) DEFAULT NULL,
  `port` varchar(255) DEFAULT NULL,
  `serviceURI` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7s8g58nkllsi9uw2ijka6kyk0` (`operator`,`cloudName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `neighborhood`
--

LOCK TABLES `neighborhood` WRITE;
/*!40000 ALTER TABLE `neighborhood` DISABLE KEYS */;
/*!40000 ALTER TABLE `neighborhood` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-02-08 18:10:21
