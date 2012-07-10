package codereview

import org.apache.maven.scm.ScmFileSet
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository
import org.apache.maven.scm.repository.ScmRepository
import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider
import org.apache.maven.scm.ChangeSet

class GitRepositoryService {

    def infrastructureService

    void checkoutProject(String gitScmUrl) {
        ScmFileSet allFilesInProject = prepareScmFileset(gitScmUrl)
        ScmRepository gitRepository = createScmRepositoryObject(gitScmUrl)

        new GitExeScmProvider().checkOut(gitRepository, allFilesInProject)
    }

    void updateProject(String gitScmUrl) {
        ScmFileSet allFilesInProject = prepareScmFileset(gitScmUrl)
        ScmRepository gitRepository = createScmRepositoryObject(gitScmUrl)

        new GitExeScmProvider().update(gitRepository, allFilesInProject)
    }

    Changeset[] fetchFullChangelog(String gitScmUrl) {

        ScmFileSet allFilesInProject = prepareScmFileset(gitScmUrl)
        ScmRepository gitRepository = createScmRepositoryObject(gitScmUrl)

        def changeLogScmResult = new GitExeScmProvider().changeLog(gitRepository, allFilesInProject, new Date(0), new Date(), 0, "master")
        List<ChangeSet> changes = changeLogScmResult.getChangeLog().getChangeSets()

        changes
                .collect { new Changeset(it.revision, it.author, it.date) }
                .sort { it.date.time } //TODO it seems that somehow sort order is build-depenent (IDEA vs Grails) - find cause
    }

    private ScmRepository createScmRepositoryObject(String gitScmUrl) {
        new ScmRepository("git", new GitScmProviderRepository(gitScmUrl))
    }

    private ScmFileSet prepareScmFileset(String gitScmUrl) {
        new ScmFileSet(infrastructureService.getProjectWorkingDirectory(gitScmUrl), "*.*")
    }


}
