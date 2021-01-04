# LicenseManager-backend ![GitHub Workflow Status](https://img.shields.io/github/workflow/status/birdman98/LicenseManager-backend/Build%20Spring%20application) ![GitHub](https://img.shields.io/github/license/birdman98/LicenseManager-backend) ![GitHub release (latest by date)](https://img.shields.io/github/v/release/birdman98/LicenseManager-backend) ![GitHub all releases](https://img.shields.io/github/downloads/birdman98/LicenseManager-backend/total) ![Lines of code](https://img.shields.io/tokei/lines/github/birdman98/LicenseManager-backend) ![GitHub language count](https://img.shields.io/github/languages/count/birdman98/LicenseManager-backend) ![GitHub top language](https://img.shields.io/github/languages/top/birdman98/LicenseManager-backend)

Engineer's Thesis project repository

## Getting started

1. Setting up SSL certificate for backend

   - You can use either provided PKCS12 certificate placed here [license-manager.p12](/backend/src/main/resources/keystore/license-manager.p12) or
     generate your own SSL certificate using Java keystore tool using following command and providing required values

   ```
   keytool -genkeypair -alias license-manager -keyalg RSA -keysize 4096 -storetype PKCS12 -keystore license-manager.p12 -validity 365
   ```

   Place generated certificate in [keystore](/backend/src/main/resources/keystore) directory

   - If you decided to use your own certificate please check if values of environment variables corresponding to SSL certificate placed in [docker-compose.yaml](./docker-compose.yaml) are correct

2. Setting up Docker environment

   - Check values of environment variables placed in [docker-compose.yaml](./docker-compose.yaml) **environment** sections, which are passed to Docker containers and adjust its values to your needs,
   - To run database and backend services in containerized environment using docker-compose tool just navigate into project directory and run following command in your terminal

   ```
   docker-compose --project-name license-manager up
   ```

   You can also pass explicitly docker-compose config file by passing `-f/--file` argument followed by path to config file

   ```
   docker-compose --project-name license-manager -f docker-compose.yaml up
   ```

   To run containers in "detached" mode just add `-d` argument at the end of command.

   ```
   docker-compose --project-name license-manager -f docker-compose.yaml up -d
   ```

   To force containers rebuild use `--build argument`:

   ```
   docker-compose --project-name license-manager -f docker-compose.yaml up --build
   ```

   To stop all containers just run command:

   ```
   docker-compose -f docker-compose.yaml down
   ```

   **IMPORTANT NOTES**

   - docker-compose will also start container running mailhog image which is SMTP mail server for testing purposes. If you don't want to use mailhog and instead
     delivery e-mails from license-manager to recipients using real SMTP servers just delete mailhog service from [docker-compose.yaml](./docker-compose.yaml) file and edit
     mail properties defined in [application.properties](./backend/src/main/resources/application.properties) to satisfy your requirements
   - alongside with crucial backend services containers, there will be also created reverse proxy container running Nginx reverse proxy, which forwards HTTP requests on port 80 to HTTPS port 9800 in license-manager server. It can be used e.g. for testing purposes in development environment when some client refuses connections to server running HTTPS with self signed certificate. In my case I found it usefull when developing mobile application, because Android OS refuses connecting to self signed certificate HTTPS and at the moment, there is no working solution for React Native Expo managed apps to bypass this constraint. Whether you aren't going to use this reverse proxy container, feel free to delete it from [docker-compose.yaml](./docker-compose.yaml) file.

If you want to run application locally, you have to define environment variables specified in [application.properties](./backend/src/main/resources/application.properties) or edit this file with direct values of config directives.

3. Configuring CORS in Spring server - to configure domains allowed by CORS change `cors.clients.urls` directive in [application.properties](./backend/src/main/resources/application.properties), for example:

```
cors.clients.urls=https://localhost:9804, https://localhost:9805
```

If directive is not specified or commented the default value is '\*' - allows all origins.

Note that frontend application for License Manager is CSR (Client-Side Rendered), so you have to make sure that client's browser CORS preflight request won't be rejected by server by specifying
proper allowed domains or by allowing all domains.

4. You have to note, that CSRF is enabled in Spring Security, excepts endpoints responsible for registration, account activation and login - in response to this endpoints you will receive cookie with **XSRF-TOKEN** value. To make further requests to secured enpoints you need to get this XSRF token value from cookie and add to every request
   header with name `X-XSRF-TOKEN` and value which you previously got in cookie.

5. You can view API documentation using Swagger UI OpenAPI documentation at `/swagger-ui/index.html`.
