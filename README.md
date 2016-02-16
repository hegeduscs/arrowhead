# Arrowhead Core System

## Usage

1. Clone repository, then import the Maven Project using Eclipse Java EE Edition

2. Create database by running `arrowhead_database_stub.sql` on your MySQL installation

  Create a new database called `arrowhead` and import the script mentioned above. This will create all the necessary tables and some dummy data for testing the framework. You can find your phpMyAdmin installation by navigating to the following address in your browser: `http://localhost/phpmyadmin`

3. Rename `hibernate.cfg.xml.example` found in `\core\src\main\resources` to `hibernate.cfg.xml`

  This way your database credentials are not shred in the repository.

4. Set MySQL server url, username and password on the following lines:
  ```xml
     <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/core</property>
     <property name="hibernate.connection.username">root</property>
     <property name="hibernate.connection.password">root</property>
  ```
  Default credentials using XAMPP and phpMyAdmin: root/\<blank\>

5. Create new Tomcat server and deploy the project

  Right click on project -> Run As -> Run On Server -> Select an existing or define a new server -> Add resources to configured column

## Requirements

* Eclipse IDE for Java EE Developers (Mars+): [download](https://www.eclipse.org/downloads/)
* MySQL Database and phpMyAdmin bundled with XAMPP (or other equivalent software) : [download](https://www.apachefriends.org/hu/download.html)
* Apache Tomcat (v7.0+): [download](http://tomcat.apache.org/)
* Java SE Runtime Environment (v7.0+): [download](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
