metadata-parser
===============

Main objective of this service is to retrieve and parse metadata associated with data object.
If inside archive is supported file type - it would be parsed.

currently supported file types are:
* csv
supported archive file types are:
* zip
* gz

Collected metadata associated with file, and returned  are:
* **size** file size
* **sourceUri**
* **targetUri**
* **format** file format
* **recordCount** number of records
* **title** file name
* **category** associated with file by uploader
* **orgUUID** uploader organisation identifier
* **isPublic** if file is public it would be displayed

Parsed data are then send to service responsible for saving them. Receiver can be configure in
application.yml (which is taken from cloud foundry environment variables). Default address for
sending data is: http://localhost:5000 and can be also configured in application.yml.

### Required services

* **datacatalog** - holds URL of data-catalog microservice and saves all metadata
* **user-management** - holds user information

For running locally all services can be configured in application.yml.


### Required libraries

Following libraries are necessary to successfully build metadata-parser:

* **cf-client** - separate library to communicate with cloud foundry layer.

### Configuration

 * **HDFS_USER** - obligatory, set to 'cf'

### Build

It's spring boot application build by maven. To run application type:

```
mvn verify
```

### Running Locally
To run the service locally or in Cloud Foundry, the following environment variables need to be defined:
* `VCAP_SERVICES_SSO_CREDENTIALS_TOKENKEY` - an UAA endpoint for verifying token signatures;
* `DOWNLOADS_DIR` - a folder for downloaded content;

To run the service:
* Run command: `DOWNLOADS_DIR=/tmp mvn spring-boot:run`
* Checkout documentation at http://localhost:8900/sdoc.jsp

### Deployment in a Kerberos-enabled environment 
* `mvn verify`
* Set `SPRING_PROFILES_ACTIVE: "secure"` in manifest.yml
* `cf push`
