# Continuous Integration
This project implements a small continuous intergation CI server containing only the core features of continuous integration.

When a push occurs on the repository, the CI server first receives the webhook payload, then identifies the affected branch and then checks out that revision. Then the server builds the project, executes the automated tests, sets the commit status on the repository, and updates the build history. The build history persists even if the server is rebooted. 

## How to run

### Prerequisites
- [Language] Java 25
- [Buildtool] Gradle 9.3.0
- [Framework] Spring Boot 4.0.2
- [Views] JSP (Tomcat Jasper)
- [Test framework] Junit 5.10.1
- [System] Unix-based system for bash-compatability

### Run the program
**macOS/Linux**
<br>
`./gradlew bootRun`

### Build and test the program
**macOS/Linux**
<br>
- Build with `./gradlew build`
- Test with `./gradlew test`