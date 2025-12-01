<!--
SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>

SPDX-License-Identifier: CC0-1.0
-->

![Logo of the project](https://raw.githubusercontent.com/idm-suedtirol/odh-alpinebits/master/alpine_bits_rgb.png)

# Open Data Hub - AlpineBits server

[![REUSE Compliance](https://github.com/noi-techpark/odh-alpinebits-server/actions/workflows/reuse.yml/badge.svg)](https://github.com/noi-techpark/odh-docs/wiki/REUSE#badges)

This repository provides an [AlpineBits](https://www.alpinebits.org/) server implementation that uses the [Open Data Hub (ODH)](https://opendatahub.com/) as backend.

- AlpineBits is an open standard to exchange touristic data.
- Open Data Hub is a platform that integrates open source projects into a practical solution.

The server implementation in this repository depends on the [AlpineBits library](https://github.com/noi-techpark/odh-alpinebits).

## Table of Contents

- [Getting started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Source code](#source-code)
  - [Execute without Docker](#execute-without-docker)
  - [Execute with Docker](#execute-with-docker)
- [Information](#information)
- [How to request data?](#how-to-request-data)

## Getting started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. Take a look at the [Deployment](#deployment) section for notes on how to deploy the project on a live system.

### Prerequisites

To build the project, the following prerequisites must be met:

- Java JDK 25 or higher (e.g. [OpenJDK](https://openjdk.java.net/))
- [Maven](https://maven.apache.org/) 3.x

If you want to run the application using [Docker](https://www.docker.com/), the environment is already set up with all dependencies for you. You only have to install [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/) and follow the instruction in the [dedicated section](#execute-with-docker).

### Source code

Get a copy of the repository, e.g. by cloning it from the following location:

```bash
https://github.com/noi-techpark/odh-alpinebits-server
```

Change directory:

```bash
cd odh-alpinebits-server/
```

### Execute without Docker

Copy the file `application-spring/src/main/resources/application.properties` to `application-spring/src/main/resources/application-local.properties` and adjust the variables that get their values from environment variables. You can take a look at the `.env.example` for some help.

Build the project:

```bash
mvn -Dspring.profiles.active=local clean install
```

Run the project:

```bash
mvn -Dspring.profiles.active=local spring-boot:run
```

The service will be available at localhost and your specified server port.

To execute the tests you can run the following command:

```bash
mvn clean test
```

### Execute with Docker

Copy the file `.env.example` to `.env` and adjust the configuration parameters.

Then you can start the application using the following command:

```bash
docker-compose up
```

The service will be available at localhost and your specified server port.

To execute the test you can run the following command:

```bash
docker-compose run --rm app mvn clean test
```

## Information

### Support

For support, please contact [Christian Gapp](https://github.com/gappc) or
[NOI Techpark](https://noi.bz.it/en).

### Contributing

If you'd like to contribute, please fork the repository and use a feature branch. Pull requests are warmly welcome.

### Documentation

More documentation can be found at [https://opendatahub.readthedocs.io/en/latest/index.html](https://opendatahub.readthedocs.io/en/latest/index.html).

### Versioning

This project uses [SemVer](https://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/noi-techpark/odh-alpinebits-server/tags). 

### License

The code in this project is licensed under the Mozilla Public License 2.0 license. See the [LICENSE](./LICENSE) file for more information.

### REUSE

This project is [REUSE](https://reuse.software) compliant, more information about the usage of REUSE in NOI Techpark repositories can be found [here](https://github.com/noi-techpark/odh-docs/wiki/Guidelines-for-developers-and-licenses#guidelines-for-contributors-and-new-developers).

Since the CI for this project checks for REUSE compliance you might find it useful to use a pre-commit hook checking for REUSE compliance locally. The [pre-commit-config](.pre-commit-config.yaml) file in the repository root is already configured to check for REUSE compliance with help of the [pre-commit](https://pre-commit.com) tool.

Install the tool by running:
```bash
pip install pre-commit
```
Then install the pre-commit hook via the config file by running:
```bash
pre-commit install
```

### Authors

* **Christian Gapp** - *Initial work* - [gappc](https://github.com/gappc)

### Acknowledgements

- [NOI Techpark](https://noi.bz.it/en)

## How to request data?

You can use the [AlpineBits Protocol Test Client](https://development.alpinebits.org/#/client) to request data from the [Open Data Hub](https://opendatahub.com/) using the AlpineBits Open Standard.

In the test client you have to specify the request settings:

```
end point (URL): https://alpinebits.opendatahub.com/AlpineBits
user name: someuser
password: somesecret
X-AlpineBits-ClientProtocolVersion: 2020-10
X-AlpineBits-ClientID (optional): development.alpinebits.org-test-client
action: OTA_Ping:Handshaking (since 2018-10)
request (optional): handshaking.xml
```

The *action* setting requires you to upload an XML file. You can create a plain text file called *handshaking.xml* on your machine and fill in the following XML content: 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<OTA_PingRQ xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns="http://www.opentravel.org/OTA/2003/05" xsi:schemaLocation="http://www.opentravel.org/OTA/2003/05 OTA_PingRQ.xsd" Version="8.000">

    <EchoData>
    {
      "versions": [
        {
          "version": "2017-10"
        },
        {
          "version": "2018-10",
          "actions": [
            {
              "action": "action_OTA_Ping"
            },
            {
              "action": "action_OTA_HotelDescriptiveInfo_Inventory",
              "supports": [
                "OTA_HotelRatePlan_BaseRates_deltas"
              ]
            },
            {
              "action": "action_OTA_HotelDescriptiveContentNotif_Inventory",
              "supports": [
                "OTA_HotelDescriptiveContentNotif_Inventory_use_rooms"
              ]
            },
            {
              "action": "action_OTA_HotelRatePlan_BaseRates",
              "supports": [
                "OTA_HotelRatePlan_BaseRates_deltas"
              ]
            },
            {
              "action": "action_OTA_HotelRatePlanNotif_RatePlans",
              "supports": [
                "OTA_HotelRatePlanNotif_accept_overlay",
                "OTA_HotelRatePlanNotif_accept_Supplements",
                "OTA_HotelRatePlanNotif_accept_RatePlan_BookingRule",
                "OTA_HotelRatePlanNotif_accept_FreeNightsOffers",
                "OTA_HotelRatePlanNotif_accept_FamilyOffers"
              ]
            }
          ]
        }
      ]
    }
    </EchoData>
</OTA_PingRQ>
```

The response should look like this:

```xml
end point response (status = 200)
=================================

headers
-------

Date: Mon, 09 Mar 2020 07:33:39 GMT
Server: Apache/2.4.39 (Amazon) OpenSSL/1.0.2k-fips
Content-Length: 1640
Connection: close


body
----

<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<OTA_PingRS xmlns="http://www.opentravel.org/OTA/2003/05" Version="8.000">
    <Success/>
    <Warnings>
        <Warning Type="11" Status="ALPINEBITS_HANDSHAKE">
        {
          "versions": [
            {
              "version": "2018-10",
              "actions": [
                {
                  "action": "action_OTA_HotelDescriptiveContentNotif_Inventory",
                  "supports": [
                    "OTA_HotelDescriptiveContentNotif_Inventory_use_rooms"
                  ]
                },
                {
                  "action": "action_OTA_Ping"
                },
                {
                  "action": "action_OTA_HotelDescriptiveInfo_Inventory"
                }
              ]
            },
            {
              "version": "2017-10"
            }
          ]
        }    
        </Warning>
    </Warnings>
    <EchoData>
    {
      "versions": [
        {
          "version": "2017-10"
        },
        {
          "version": "2018-10",
          "actions": [
            {
              "action": "action_OTA_Ping"
            },
            {
              "action": "action_OTA_HotelDescriptiveInfo_Inventory",
              "supports": [
                "OTA_HotelRatePlan_BaseRates_deltas"
              ]
            },
            {
              "action": "action_OTA_HotelDescriptiveContentNotif_Inventory",
              "supports": [
                "OTA_HotelDescriptiveContentNotif_Inventory_use_rooms"
              ]
            },
            {
              "action": "action_OTA_HotelRatePlan_BaseRates",
              "supports": [
                "OTA_HotelRatePlan_BaseRates_deltas"
              ]
            },
            {
              "action": "action_OTA_HotelRatePlanNotif_RatePlans",
              "supports": [
                "OTA_HotelRatePlanNotif_accept_overlay",
                "OTA_HotelRatePlanNotif_accept_Supplements",
                "OTA_HotelRatePlanNotif_accept_RatePlan_BookingRule",
                "OTA_HotelRatePlanNotif_accept_FreeNightsOffers",
                "OTA_HotelRatePlanNotif_accept_FamilyOffers"
              ]
            }
          ]
        }
      ]
    }
    </EchoData>
</OTA_PingRS>
```
