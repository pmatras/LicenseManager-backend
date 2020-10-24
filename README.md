# LicenseManager-backend
Engineer's Thesis project repository

## Getting started

1. Setting up SSL certificate for backend
    * You can use either provided PKCS12 certificate placed here [license-manager.p12](/backend/src/main/resources/keystore/license-manager.p12) or
    generate your own SSL certificate using Java keystore tool using following command and providing required values
    ```
    keytool -genkeypair -alias license-manager -keyalg RSA -keysize 4096 -storetype PKCS12 -keystore license-manager.p12 -validity 365
    ```
    Place generated certificate in [keystore](/backend/src/main/resources/keystore) directory
    * If you decided to use your own certificate please check if values of environment variables corresponding to SSL certificate placed in [docker-compose.yaml](./docker-compose.yaml) are correct
 
2. Setting up Docker environment
    * Check values of environment variables placed in [docker-compose.yaml](./docker-compose.yaml) **environment** sections, which are passed to Docker containers and adjust its values to your needs,
    * To run database and backend services in containerized environment using docker-compose tool just navigate into project directory and run following command in your terminal
    ```
    docker-compose --project-name license-manager up
    ```
    You can also pass explicitly docker-compose config file by passing ``-f/--file`` argument followed by path to config file
    ```
    docker-compose --project-name license-manager -f docker-compose.yaml up
    ```
    To run containers in "detached" mode just add ``-d`` argument at the end of command.
     ```
    docker-compose --project-name license-manager -f docker-compose.yaml up -d
    ```

    To force containers rebuild use ``--build argument``:
    ```
    docker-compose --project-name license-manager -f docker-compose.yaml up --build
    ```
    To stop all containers just run command:
    ```
    docker-compose -f docker-compose.yaml down
    ```
    * **IMPORTANT NOTE** - docker-compose will also start container running mailhog image which is SMTP mail server for testing purposes. If you don't want to use mailhog and instead 
    delivery e-mails from license-manager to recipients using real SMTP servers just delete mailhog service from [docker-compose.yaml](./docker-compose.yaml) file and edit
    mail properties defined in [application.properties](./backend/src/main/resources/application.properties) to satisfy your requirements

If you want to run application locally, you have to define environment variables specified in [application.properties](./backend/src/main/resources/application.properties) or edit this file with direct values of config directives.
