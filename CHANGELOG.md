### August 13th, 2018
* Our complicated custom input validation solution replaced with a clean, easy to use annotation based validation library (Hibernate validator).
* Management API refactorings, to provide ID-field based resource query, update and delete operations.
* Common resources (services, systems, clouds, devices) have their management REST endpoints in the common module now, so each module can provide their own endpoints for their database.
* Complex entity classes now use Hibernate's @OnDelete annotation to delete child entities, when a parent entity gets deleted.
* Created new common resource: ArrowheadDevice.
* Service metadata is moved back to ArrowheadService, and the port field is moved back to ArrowheadSystem (from ServiceRegistryEntry).