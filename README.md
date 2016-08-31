Childcare Calculator Frontend Micro-service.
====================================================================

[![Build Status](https://travis-ci.org/hmrc/cc-frontend.svg?branch=master)](https://travis-ci.org/hmrc/cc-frontend)
 [ ![Download](https://api.bintray.com/packages/hmrc/releases/cc-frontend/images/download.svg) ]
 (https://bintray.com/hmrc/releases/cc-frontend/_latestVersion)



The Childcare Calculator will help parents quickly self-assess the options for their childcare support, allowing them to
make a decision on which scheme will best suit their needs. The Childcare Calculator will calculate the data input by
the users, inform them of their eligibility and how much support that they could receive for the Tax-Free Childcare (TFC),
Tax Credits (TC) and Employer-Supported Childcare (ESC) schemes.

The Childcare Calculator invokes cc-eligibility microservice([Eligibility documentation](https://github.tools.tax.service.gov.uk/DDCN/cc-eligibility/blob/master/README.md)) and cc-calculator microservice([Calculator documentation](https://github.tools.tax.service.gov.uk/DDCN/cc-calculator/blob/master/README.md)) to get the desired results.

The Childcare Calculator has a feature where in users can do email registration to send the email the calculator invokes cc-email-capture microserive([Email capture documentation](https://github.tools.tax.service.gov.uk/DDCN/cc-email-capture/blob/master/README.md))

The Childcare Calculator Frontend service, collects the data input by the users from the fields on the presented pages.
This data is collated and passed to the Childcare Calculator backend processes. The results are returned to the Childcare
Calculator Frontend service to display to the user.

* **Endpoint URL :** /childcare-calculator

* **Port Number :** 9366



cc-frontend - getting started
-----------------------------
The application uses [Grunt](http://gruntjs.com/) to manage the assets and perform pre-deployment compilation of custom stylesheets/javascript files etc.

### Configuring Grunt + NodeJS + NPM (Node package manager)

1. Clone the repository
2. Install [node.js](http://nodejs.org/)
..* `sudo apt-get install nodejs`
..* If you're using a Linux distro you may need to install `sudo apt-get install nodejs-legacy`
3. sudo apt-get install npm
4. Run `./setupgrunt.sh`
5. Run `sbt test`

### Compiling assets / Running grunt process

If you need to manually compile the assets then in the terminal run the following:

1. `cd $workspace/assets/js`
2. `grunt`

**NOTE:** This will run a local instance of browserSync, this is accessible at `http://localhost:3000/childcare-calculator`

**NOTE:** There is currently a complication issue with custom styles when running the service through Service Manager: `sm --start CC_FRONTEND -f`

All assets are compiled into the `/public` folder which has a route defined within the `conf/cc.routes` file

**NOTE:** `assets-frontend` is automatically pulled in from the govuk template, you have to have a json configuration within a `.conf` file
For example:

```
assets {
  version = "2.50.0"
  url = "http://localhost:9032/assets/"
}
```

Testing
-------------

#### Unit Tests
To run the unit tests for the application run the following:

1. `cd $workspace`
2. `sbt test`

To run a single unit test/spec

1. `cd $workspace`
2. `sbt`
3. `test-only */path/to/unitspec/Example*` - Example being the class name of your UnitSpec

#### Test Coverage
To run the test coverage suite `scoverage`

1. `sbt clean scoverage:test`

#### Acceptance Tests

**NOTE:** Cucumber/acceptance tests are available in a separate project at:
`http://github.tools.tax.service.gov.uk/ddcn/cc-acceptance-tests`

Messages
--------------
To provide messages files with variables that are passed in then use the following format:

```
@Messages("cc.compare.total.household.spend", totalHouseholdSpend)
cc.compare.total.household.spend = You told us your childcare costs are {0} a month
```
