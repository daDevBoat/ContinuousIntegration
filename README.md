# Continuous Integration
This project implements a small continuous intergation CI server containing only the core features of continuous integration.

When a push occurs on the repository, the CI server first receives the webhook payload, then identifies the affected branch and then checks out that revision. Then the server builds the project, executes the automated tests, sets the commit status on the repository, and updates the build history. The build history persists even if the server is rebooted. 

## Build list urls
There is one webhook setup on github per group member which is configured to send to to that group member's
ngrok url. Any student can run the CI-server, so there are five urls with each commit history available at 
the page /history:
| Team member | History url |
| ---- | ---- |
| Alexander Mannertorn  | https://luetic-interrelatedly-melissia.ngrok-free.dev/history |
| Arnau Pelechano García | https://ariah-interbrachial-natisha.ngrok-free.dev/history |
| Elias Richard Næss | https://ara-listless-sinistrorsely.ngrok-free.dev/history |
| Jannis Häffner | https://corban-hyperscrupulous-jesse.ngrok-free.dev/history |
| Jonatan Bölenius | https://phagolytic-bertram-weighty.ngrok-free.dev/history |


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

### API Documentation (Javadoc)
- Generate the browsable API documentation with `./gradlew javadoc` 
- Open it with `python3 -m http.server 8000` and then 
visiting http://localhost:8000/build/docs/javadoc/ 


## Compilation and Test execution: Implementation and testing
### Overview
The compilation and testing feature is implemented in the CompilationService class.
When a push event is received via the webhook the CI server automatically compiles the project on the branch where the change was made, as specified in the HTTP payload.

### Implementation
The CompilationService.compile() method:
- Validates the project directory (must be non-null, existing, and a directory).
- Executes ./gradlew build via ProccessBuilder in the specified directory.
- Captures the build output line by line as List<String>
- Appends a message as the last element of the output list saying the build succeeded or failed.
- Returns a CompilationResult object containing the success status, output logs, and exit code.

By running `./gradlew build` in a bash shell (see `CompilationService.java`). The build command both builds and tests the program. Furthermore by passing the `-l` argument to bash the shell in which the build is running has access to the same resources (ssh keys etc.) as the user, such that cloning and checking out a build is possible. The method waits for the process to finish and returns a CompilationResult with:
* The process `exit code` which is **0** if the build/tests succeeded, and otherwise not
* A `success` boolean, which is true if the exit code is **0**
* The `output` logs from the process, as a list of strings, which is used to store commit/build history

This result is later persisted in the Status service as a CommitRecord containing the build result: SHA, build status, (“SUCCESS”, “FAILURE”, “ERROR”), timestamp and build logs.
### Testing
The compilation and testing feature is tested in the `CompilationTest` class, which contains 6 unit tests covering the test cases needed to cover all the requirements, this is done through... 
* ...testing to compile different invalid directories/files
* ...testing the CompilationResult model by checking that it manages both successful and failed results
* ...testing the getOutput() method for CompilationResult for two cases

The compilation logic is a part of the webhook integration test where it is mocked and used to see that the webhook returns the correct status. This tests the that the interaction between the compilation and the result that the webhook sends to Github is functioning correctly.


## CI server notification: Testing and Implementation
### Implementation
CI server notification is implemented using the GitHub REST API for commit status. Where the statuses is displayed in the commit message at GitHub as either **success**, **pending** or **failure**. GithubAPIHandler.java is the class that is used to construct the POST request that is sent to the Github API. 

### Testing
The notifications are unit tested using where a **success**, **pending**, **failure** and invalid commit status post request is sent to different commits on the test/commit_status_api branch. The unit tests checks whether the post request was sent successfully or if an excpetion was thrown.

## Way-of-working:
We agreed to follow a continuous integration workstyle, where for every feature or fix identified, we create an issue. Every issue is its own branch. After the work on an issue is completed, the issue branch will be rebased, the commits will be squashed, and then merged with main. This allows the main branch to have an easily understood commit history, with each commit representing one issue, while also allowing for tracing errors or bugs to a specific commit.

### Team

When evaluating the checklist for Team in the Essence standard, it is clear that we are in the state Collaborating and have fulfilled some of the points in Performing.

It is clear that we are in the state Collaborating since we fulfill all the points for the previous states as well as all the points for the Collaborating state. We are working as one cohesive unit by splitting up the work and working together on the parts that need collaboration. Our communication is open and honest, all team members know and trust each other, and are focused on achieving the team mission.

The obstacles to reach the next state consists of two things. One is that we need to minimize the avoidable backtracking and reworking. Right now we still have to backtrack and rework our solutions at times, because our original outline of the program might have been wrong, or because different team members' implementations do not align with each other. The second obstacle for reaching the next state is to continously identify and eliminate wasted work and potential wasted work. Right now we still get some wasted work at times, for example when one member's code has to be modified to fit another member's code. 

## Statement of Contributions

### [Elias Richard Næss] — GitHub: [@daDevBoat]
Elias was responsible for the implementation of the usage of the Github commit REST API, implementing a testing structure that allowed for the server being able to concurrently handle requests, implementing validatePushEvent and validateRepoName functions, and made the CiService detach and run asynchronously such that the webhook gets a 202 message returned once payload is recieved and validated and the building and testing is handled by a seperate thread. 

### [Alexander Mannertorn] - Github: [@knasssss]
Alexander wrote the home() function and its corresponding unit test, wrote the draft of the integration test for CIWebHookController, wrote the original code for storing the status for the latest commit and its unittest, added missing javadocs, fixed incorrect javadocs, wrote the overview, the Team Essence part, and the API Documentation part in the readme file. 

### [Jonatan Bölenius] — GitHub: [@djonte]
Jonatan started by creating a small proof-of-concept Springboot application with the ability to respond to Github's webhooks, this was then used by the team as a supporting skeleton for the actual application. Other than that, his main contributions to the code base were:
* Validation of the Webhook's secret (HMAC SHA256), which ensures no third party can send requests without knowing the shared secret. 
* Persisting commits (storing CommitRecords)
* Views for showing history of commits and checking out a specific commit by /commit/\<sha\>

This together with appropriate unit tests for each feature, and the `How to run` part of the README as well as working together with Arnau to write the `Compilation and Test execution: Implementation and testing`.

### [Jannis Häffner] — GitHub: [@dJannisHaeffner]
Jannis was responsible for the implementation of the RepoSetup class which is needed to create and clone the repo, fetch the newest changes and checkout a specific commit. He implemented, in addition to the corresponding tests for the RepoSetup class, also an integration test for the CIWebhook Controller and the CI Service class. This integration test verifies that in different sitations the expected HTTP status codes are returned. 

### [Arnau Pelechano García] - GitHub: [@arpega75]
Arnau was responsible for implementing the **compilation** feature and integrating it into the CI pipeline. He developed the CompilationService class, which executes `./gradlew build`, captures the exit code and logs, and returns the result indicating whether the build succeeded or failed. He integrated this functionality into the CiService, ensuring that build results are stored in the Status service for commit persistence, in collaboration with Jonatan. 
He also implemented the CompilationTest with unit tests, wrote the Javadoc for the CompilationService and the `Compilation and Test execution: Implementation and Testing` section of the README together with Jonatan.