# Arrowhead Core System

## Usage

1. Clone repository, then import the Maven Project using Eclipse Java EE Edition

2. Create database by running `arrowhead_database_stub.sql` on your MySQL installation

3. Rename `hibernate.cfg.xml.example` found in `\core\src\main\resources` to `hibernate.cfg.xml`

4. Set MySQL server url, username and password on the following lines:
  ```xml
     <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/core</property>
     <property name="hibernate.connection.username">root</property>
     <property name="hibernate.connection.password">root</property>
  ```

5. Create new Tomcat server and deploy the project
