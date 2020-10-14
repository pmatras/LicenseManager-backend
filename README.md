# LicenseManager-backend
Engineer's Thesis project repository

## Getting started
 
1. Setting up Docker environment
    * Check values of environment variables placed in [docker-compose.yaml](./docker-compose.yaml) **environment** sections, which are passed to Docker containers and adjust its values to your needs,
    * To run database and backend services in containerized environment using docker-compose tool just navigate into project directory and run following command in your terminal
    ```
    docker-compose --project-name license-manager up
    ```
    You can also pass explicitly docker-compose config file by passing ``-f/--file`` argument followed by path to config file
    ```
    docker-compose --project-name -f docker-compose.yaml license-manager up
    ```
    To run containers in "detached" mode just add ``-d`` argument at the end of command.

If you want to run application locally, you have to define environment variables specified in [application.properties](./backend/src/main/resources/application.properties) or edit this file with direct values of config directives.