deltatrai-publisher Jenkins plugin
==================================

## Installation

```bash
mvn install
```

Copy the `target/deltatrail-publisher.hpi` file into the Jenkins plugin directory.

## Configuration

Set the **API root** (https://deltatrail.io/api/) and **API token** 
in Jenkins configuration.

## Usage

Add a **Post-build Action** of **Publish to Deltatrail**.