package codereview

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.ObjectLoader
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk

import static com.google.common.base.Preconditions.checkArgument
import static com.google.common.base.Preconditions.checkState

class GitRepositoryService {

    def diffAccessService
    def infrastructureService

    void updateOrCheckOutRepository(String scmUrl) {
        File projectRoot = infrastructureService.getProjectRoot(scmUrl)
        def repository
        try {
            repository = diffAccessService.openGitRepository(projectRoot)
        } catch (RepositoryNotFoundException e) {
            log.info("Could not find git repository at $projectRoot.absolutePath, creating one", e)
            repository = cloneRepository(scmUrl, projectRoot)
        }
        Git git = new Git(repository)
        def pullResult = git.pull().call()
        checkState(pullResult.successful, "Failed to update ${scmUrl}")
    }

    private Repository cloneRepository(String scmUrl, File projectRoot) {
        Git git = new Git(new FileRepositoryBuilder().setWorkTree(projectRoot).build())
        git.cloneRepository()
                .setURI(scmUrl)
                .setDirectory(projectRoot)
                .call()
        return git.repository
    }

    def getAllChangesets(String scmUrl) {
        Git git = prepareGit(scmUrl)
        Iterable<RevCommit> logOutput = git.log().call()
        prepareGitChangesets(logOutput, git.repository.workTree)
    }

    def getNewChangesets(String scmUrl, String lastChangesetPathSpec) {
        Git git = prepareGit(scmUrl)
        def lastChangesetId = git.repository.resolve(lastChangesetPathSpec)
        def logOutput = git.log().not(lastChangesetId).call()
        prepareGitChangesets(logOutput, git.repository.workTree)
    }

    private List<GitChangeset> prepareGitChangesets(Iterable<RevCommit> logOutput, File workTree) {
        def logIterator = logOutput.iterator()
        def changesets = []
        while (logIterator.hasNext()) {
            def commit = logIterator.next()
            def commitHash = commit.toString().split(" ")[1]
            GitChangeset gitChangeset = new GitChangeset(
                    commit.fullMessage,
                    commit.authorIdent.emailAddress,
                    commitHash,
                    new Date(commit.getCommitTime() * 1000L)
            )
            gitChangeset.files = diffAccessService.getFilesChangedInCommit(workTree, commitHash)
            changesets.add(gitChangeset)
        }
        return changesets
    }

    String getFileContentFromChangeset(String scmUrl, String revisionString, String fileName) {
        def git = prepareGit(scmUrl)
        def repository = git.repository
        ObjectId changesetId = repository.resolve(revisionString)
        checkArgument(changesetId != null, "No such revision in ${scmUrl}: ${revisionString}")
        RevCommit revCommit = new RevWalk(repository).parseCommit(changesetId)
        def treeWalk = TreeWalk.forPath(repository, fileName, revCommit.getTree())
        checkArgument(treeWalk != null, "No such file in ${scmUrl}/${revisionString}: ${fileName}")
        ObjectLoader objectLoader = repository.open(treeWalk.getObjectId(0))
        return new String(objectLoader.bytes)
    }

    private Git prepareGit(String scmUrl) {
        File projectRoot = infrastructureService.getProjectRoot(scmUrl)
        Repository repository = diffAccessService.openGitRepository(projectRoot)
        new Git(repository)
    }
}
