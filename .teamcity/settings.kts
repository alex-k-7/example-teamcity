import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2021.2"

project {

    vcsRoot(HttpsGithubComAlexK7exampleTeamcityGitRefsHeadsMaster)

    buildType(BuildTest)
    buildType(TestBuild)

    params {
        password("env.custom_prop", "credentialsJSON:bdde5187-7bd5-4bc5-b919-b87837bb4cc8", readOnly = true)
    }
}

object BuildTest : BuildType({
    name = "Build-test"

    artifactRules = "+:target/*.jar"
    publishArtifacts = PublishMode.SUCCESSFUL

    vcs {
        root(HttpsGithubComAlexK7exampleTeamcityGitRefsHeadsMaster)
    }

    steps {
        maven {
            name = "Maven test"

            conditions {
                doesNotContain("teamcity.build.branch", "master")
            }
            goals = "clean test"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
            userSettingsSelection = "custom_set"
        }
        maven {
            name = "Maven deploy"

            conditions {
                contains("teamcity.build.branch", "master")
            }
            goals = "clean deploy"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
            userSettingsSelection = "custom_set"
        }
    }

    triggers {
        vcs {
        }
    }
})

object TestBuild : BuildType({
    name = "test_build"

    params {
        param("custom_checkout_dir", "123")
    }

    vcs {
        root(DslContext.settingsRoot)

        checkoutDir = "%custom_checkout_dir%"
    }

    steps {
        script {
            name = "echo variables"
            scriptContent = """
                echo 1
                echo %custom_checkout_dir%
                export custom_prop=new
                echo ${'$'}custom_prop
            """.trimIndent()
        }
    }
})

object HttpsGithubComAlexK7exampleTeamcityGitRefsHeadsMaster : GitVcsRoot({
    name = "https://github.com/alex-k-7/example-teamcity.git#refs/heads/master"
    url = "https://github.com/alex-k-7/example-teamcity.git"
    branch = "refs/heads/master"
    branchSpec = "refs/heads/*"
})
