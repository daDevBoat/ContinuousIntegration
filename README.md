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

In what state are you in? Why? What are obstacles to reach the next state?

When evaluating the checklist for Team in the Essence standard, it is clear that we are in the state Collaborating and have fulfilled some of the points in Performing.

It is clear that we are in the state Collaborating since the we fulfill all the points for the previous states as well as all the points for the Collaborating state. We are working as one cohesive unit by often sitting together on campus and working, as well as splitting up the work appropriately. Our communication is open and honest. All team members know and trust each other, and are focused on achieving the team mission.

The obstacles to reach the next state consists of two things. One is that we need to minimize the avoidable backtracking and reworking. As it is now we still sometimes have to backtrack and rework our solutions because our original outline of the program might have been wrong or because different team members' implementations do not align with each other. The other obstacle for reaching the next state is to continously identify and eliminate wasted work and potential wasted work. 