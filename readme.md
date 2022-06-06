# InputGenerator

Simple Kotlin application to generate test inputs for some target.

## What does it do?

InputGenerator is based on the definition of *Sequences* Each *Sequence* is defined by a *DataSource*, that read inputs, and a *DataSink* that writes them. They are interconnected by a *DataTransformer*.

Given a configured *Sequence* it *steps* through the provided data, transforms it and writes it out. Each *step* may be given a *burst* size and a target rate (steps by second).

The execution of the *Sequence* is wrapped in a Job (coroutine) given its own thread.

## Supported Modules

For *DataSource*:

* CsvSource;

For *DataTransformer*:

* StringToMqttMessageTransformer;

For *DataSink*:

* MqttSink;


## Configuration

***WIP***

Configured by command line options (e.g. --file). Current configurations:

* *file*: input file to use;
* *host*: target host name/IP;
* *port*: port for host
* *burst*: burst size to write. Defaults to 1;
* *rate*: target rate. Defaults to none (write as fast as possible);
* *password*: password field to use by sink, if needed;
* *username*: username field to use by sink, if needed;
* *topic*: topic field to use by sink, if needed;

