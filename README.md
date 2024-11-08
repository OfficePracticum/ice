# CDS Framework's ICE

## Table of Contents
- [Description](#description)
- [Pre-requisites](#pre-requisites)
- [Configuration](#configuration)
- [Building the Project](#building-the-project)
- [Testing](#testing)
- [Architecture Diagrams](#architecture-diagrams)
- [Contributing](#contributing)
- [License](#license)
- [Maintainers](#maintainers)
- [Versioning & Deployments & Contributions](#versioning--deployments--contributions)

## Description

This repository is a fork from the original [CDS Framework's ICE project](https://github.com/cdsframework/ice) Please refer to this project
for further info and CDS Framework documentation links. This fork was initially created
for the sake of implementing our own custom functions, attributes and updating business rules for the usage of it on
[ice-wrapper](https://github.com/OfficePracticum/ice-wrapper) for VL+ forecasting.

The folders we focus on for usage on the ice-wrapper project are the following:
- [opencds-ice-service](ice3/opencds-ice-service) where we define all the objects that drools is going to use as facts and its related objects
  to make actions on our forecasting series.
- [opencds-config-file](opencds/opencds-parent/opencds-config/opencds-config-file) folder that specifies the dependencies for the services being
  used at the time of forecasting, it is very unusual having the need to modify this folder.

## Pre-requisites

Before importing and building these projects, ensure the following software is installed on your system:
1. Java 8+
2. Maven (3.6.3 is recommended)

## Configuration

Configuration for this project could be divided on the following

### Pipeline configuration

Refer to the latest written [`.gitlab-ci.yml`](https://github.com/OfficePracticum/ice/blob/ADD_COVID_NOVAVAX_AND_BIVALENT_BOOSTERS/.gitlab-ci.yml) file

## Building the project:

### Step 1: Clone the Repository
```bash
git clone https://github.com/OfficePracticum/ice.git
```

### Step 2: Open the Project in Intellij

### Step 3: Clean install both artifcats
From the root directory run the following two commands:

```bash
mvn clean install -DskipTests -B -U -Dmaven.wagon.http.ssl.insecure=true -f ice3/opencds-ice-service/pom.xml
```

```bash
mvn clean install -DskipTests -B -U -Dmaven.wagon.http.ssl.insecure=true -f opencds/opencds-parent/opencds-config/opencds-config-file/pom.xml
```

These commands create a jar each in the target directory as well as the local .m2 repository
to be used by ice-wrapper as imported [ICE dependencies](https://github.com/OfficePracticum/ice-wrapper/blob/master/pom.xml#L87).

### Step 4: Running the project

For running the raw forecasting we would need to import this project inside ice-wrapper locally, build the ice-wrapper project
(as per [its readme instructions](https://github.com/OfficePracticum/ice-wrapper/tree/develop?tab=readme-ov-file#step-3-clean-installn))
and then import it and use it on hot-ice to see changes reflected. Be sure to import the target version that was built on ice-wrapper

### (Optional) Step 5: Deploy

Deploying to other servers is possible by setting the RELEASE_REPO/SNAPSHOT_REPO environment variables and using
```bash
mvn deploy -f ice3/opencds-ice-service/pom.xml -DskipTests -B -Dmaven.wagon.http.ssl.insecure=true
```

Such deployment is configured on the [`.gitlab-ci.yml`](https://github.com/OfficePracticum/ice/blob/ADD_COVID_NOVAVAX_AND_BIVALENT_BOOSTERS/.gitlab-ci.yml) file

## Testing

This project does not support direct unit testing but rather its testing can be done through the import of the modified version on the
[ice-wrapper](https://github.com/OfficePracticum/ice-wrapper) project
and therefore import ice-wrapper on the [hot-ice project](https://git.officepracticum.com/op-se/hot-ice), and then can be finally tested either
through pipeline or locally. Please refer to [How to test changes on ice-wrapper](https://opservice.atlassian.net/wiki/spaces/TEC/pages/564297738/How+to+test+changes+on+ice-wrapper) confluence article for more info.

## Architecture Diagrams

As this project is a library dependency from the [ice-wrapper](https://github.com/OfficePracticum/ice-wrapper) project, the sequence diagrams from this project
should reflect the main logic of VL+ forecasting making the usage of all its rules and models. Please refer to
[ice-wrapper's Architecture Diagrams section](https://github.com/OfficePracticum/ice-wrapper/tree/develop?tab=readme-ov-file#architecture-diagrams)

The most important diagram we use is the [Drools Evaluation Diagram](https://github.com/OfficePracticum/ice-wrapper/blob/develop/src/main/resources/diagrams/ice-wrapperDroolsEvaluationDiagram.png)
where many facts and objects being used there are defined on this project.

## Contributing
The main goal on modifying this repository is to change the two folders mentioned for adding behavior to models being used as facts from drools rules.
Some of the most used objects we use as facts and to end up with a final forecast are the following:
- [TargetDose](ice3/opencds-ice-service/src/main/java/org/cdsframework/ice/service/TargetDose.java): This object is initialized as part of the flow for drools evaluation
  being the step called "Identify Candidate Doses". This step loads the dose based on its respective immunity code, immunity group and its related series (It can have multiple related
  series, hence copies of this target dose are created with a different related series). Amongst its attributes this object contains an administration date and an evaluation state
- [TargetSeries](ice3/opencds-ice-service/src/main/java/org/cdsframework/ice/service/TargetSeries.java): This object is initialized as part of the flow for drools evaluation
  being the step called "Identify Candidate Series". This object contains recommendations to be considered for final forecasting, it contains and sets final dates and final
  recommended doses
- [Recommendation](ice3/opencds-ice-service/src/main/java/org/cdsframework/ice/service/Recommendation.java): This object contains an evaluation state, an evaluation reason,
  recommended dates and a recommended dose

Such models are some of the most frequently used ones on our drools rules both as conditions and modified as actions to some rules being triggered. The attributes
being accessed from the drools rules are the ones defined on the files listed above, there are more objects defined on the same folder that are being used on drools.

If you have questions regarding the drool rules please refer to [this link](https://opservice.atlassian.net/wiki/spaces/TEC/pages/558170115/How+to+understand+and+maintain+dsl+drl+and+dslr+files+on+ice-wrapper)

## License
This project is licensed under the GNU Lesser General Public License (LGPL). See [LICENSE file](License.txt) for more details

## Maintainers
- Mel Wang
- Elmer Hurtado
- Marco Mamani

## Known issues
- The way of publishing this was not standardized, the latest publish done was ran by local commands, please check the compare on the latest commit
  done for deploying the latest 1.39.1.1 version [using this link](https://github.com/cdsframework/ice/compare/main-v2...OfficePracticum:ice:ADD_COVID_NOVAVAX_AND_BIVALENT_BOOSTERS)

## Versioning & Deployments & Contributions
- [Semantic Versioning](https://semver.org/)
- [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)
- [GitFlow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)
- For a new version of rules, copy and paste the latest folder version from [rules directory](src/main/resources/rules),
  add a new version number and add your changes there

### CI/CD
Refer to the [`.gitlab-ci.yml`](https://github.com/OfficePracticum/ice/blob/ADD_COVID_NOVAVAX_AND_BIVALENT_BOOSTERS/.gitlab-ci.yml) file for pipelines configuring