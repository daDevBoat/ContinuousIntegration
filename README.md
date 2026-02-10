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

## Way-of-working:
We agreed to follow a continuous integration workstyle, where for every feature or fix identified, we create an issue. Every issue is its own branch. After the work on an issue is completed, the issue branch will be rebased, the commits will be squashed, and then merged with main. This allows the main branch to have an easily understood commit history, with each commit representing one issue, while also allowing for tracing errors or bugs to a specific commit.

### Team

When evaluating the checklist for Team in the Essence standard, it is clear that we are in the state Collaborating and have fulfilled some of the points in Performing.

It is clear that we are in the state Collaborating since we fulfill all the points for the previous states as well as all the points for the Collaborating state. We are working as one cohesive unit by splitting up the work and working together on the parts that need collaboration. Our communication is open and honest, all team members know and trust each other, and are focused on achieving the team mission.

The obstacles to reach the next state consists of two things. One is that we need to minimize the avoidable backtracking and reworking. Right now we still have to backtrack and rework our solutions at times, because our original outline of the program might have been wrong, or because different team members' implementations do not align with each other. The second obstacle for reaching the next state is to continously identify and eliminate wasted work and potential wasted work. Right now we still get some wasted work at times, for example when one member's code has to be modified to fit another member's code. 