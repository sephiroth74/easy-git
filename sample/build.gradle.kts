import it.sephiroth.gradle.git.api.Git
import it.sephiroth.gradle.git.lib.GitBranchListCommand.BranchMode
import it.sephiroth.gradle.git.lib.Repository


val git = Git.open(rootDir)

tasks.create("testGit") {
    doLast {
        git.branch.list().mode(BranchMode.All).call().forEach { branch ->
            logger.lifecycle("\tbranch -> $branch")
        }

        logger.lifecycle("commit : " + git.repository.resolve(Repository.HEAD).short().call())
    }
}

