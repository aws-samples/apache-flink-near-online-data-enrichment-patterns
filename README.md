# Apache Flink near online data enrichment patterns

This repository implements different [Apache Flink](https://flink.apache.org/) strategies for near online data enrichment patterns: synchronous, asynchronous and leveraging KeyedState.

## Table of contents

* [Overview](#overview)
* [Load testing template](#load-testing-template)
* [Project structure](#project-structure)
* [Security](#security)
* [License](#license)

## Overview

Data streaming workloads often require data in the stream to be enriched via external sources (such as databases or other data streams).

For example, assume you are receiving coordinates data from a GPS device and need to understand how these coordinates map with physical geographic locations; you need to enrich it with geolocation data.

For this repository we are using an example of a temperature sensor network, (1) that emits temperature information and status. These events get ingested into (2) [Amazon Kinesis Data Streams](https://aws.amazon.com/kinesis/data-streams). Downstream systems additionally require the brand and country code information, in order to analyze e.g. the reliability per brand and temperature per plant side.
Based on the sensor ID we (3) enrich this sensor information from the Sensor Info API. The resulting enriched stream (4) can then be analyzed in [Amazon Managed Service for Apache Flink Studio Notebook](https://aws.amazon.com/managed-service-apache-flink/studio/).

![Architecture overview](docs/Architecture%20overview.png)

You can use several approaches to enrich your real-time data in [Amazon Managed Service for Apache Flink](https://aws.amazon.com/managed-service-apache-flink) depending on your use case and Apache Flink abstraction level. Each method has different effects on the throughput, network traffic, and CPU (or memory) utilization.

For a general overview on how to use data enrichment patterns in Flink, see also our blog post [Common streaming data enrichment patterns in Amazon Kinesis Data Analytics for Apache Flink](https://aws.amazon.com/blogs/big-data/common-streaming-data-enrichment-patterns-in-amazon-kinesis-data-analytics-for-apache-flink).


## Load testing template
In order to test the application you can use the [Amazon Kinesis Data Generator (KDG)](https://github.com/awslabs/amazon-kinesis-data-generator) provides you with a user-friendly UI that runs directly in your browser. With the KDG, you can do the following:

* Create templates that represent records for your specific use cases
* Populate the templates with fixed data or random data
* Save the templates for future use
* Continuously send thousands of records per second to your Kinesis data stream or Firehose delivery stream

To generate events for this application, just use this template:

```
{
    "sensorId": {{random.number(100000)}},
    "temperature": {{random.number(
        {
            "min":10,
            "max":150
        }
    )}},
    "status": "{{random.arrayElement(
        ["OK","FAIL","WARN"]
    )}}",
    "timestamp": "{{date.now("x")}}"
}
```

For more details, see also the blog post [Test Your Streaming Data Solution with the New Amazon Kinesis Data Generator](https://aws.amazon.com/blogs/big-data/test-your-streaming-data-solution-with-the-new-amazon-kinesis-data-generator/).

As this approach is browser based, you are limited by the bandwidth of your connection, the round trip latency and have to keep the browser tab open to continue sending events.

To overcome this limitations, the repository [Amazon Kinesis Load Testing with Locust](https://github.com/aws-samples/amazon-kinesis-load-testing-with-locust) helps you to perform large scale Kinesis load testing with the help of Locust, a modern load testing framework.


## Project structure
```
docs/                               -- Contains project documentation
notebooks/                          -- Contains Zeppelin notebooks for analysing the data
infrastructure/                     -- Contains the CDK infrastructure definition
src/
├── main/java/...                   -- Contains all the Flink application code
│   ├── ProcessTemperatureStream    -- Main class that decides on the enrichment strategy
│   ├── enrichment.                 -- Contains the different enrichment strategies (sync, async and cached)
│   ├── event.                      -- Event POJOs
│   ├── serialize.                  -- Utils for serialization
│   └── utils.                      -- Utils for Parameter parsing
└── test/                           -- Contains all the Flink testing code
```

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This library is licensed under the MIT-0 License. See the LICENSE file.
