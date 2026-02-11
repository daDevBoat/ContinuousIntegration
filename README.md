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

## Test execution: Implementation and testing
### Implementation
Test execution is implemented by running `./gradlew build` in a bash shell (see CompilationService.java). The build command includes testing, and by passing the `-l` argument to bash the shell in which the build is running has access to the same resources (ssh keys etc.) as the user, such that cloning and checking out a build is possible. The method waits for the process to finish and returns a CompilationResult with:
* The process `exit code` which is **0** if the build/tests succeeded, and otherwise not
* A `success` boolean, which is true if the exit code is **0**
* The `output` logs from the process, as a list of strings, which is used to store commit/build history
### Testing
The test execution workflow is unit tested...
* ...by testing to compile different invalid directories/files
* ...by testing the CompilationResult model by checking that it manages both successful and failed results
* ...by testing the getOutput() method for CompilationResult for two cases

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
