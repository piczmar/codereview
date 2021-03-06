package codereview

import org.springframework.transaction.support.DefaultTransactionStatus

class ProjectUpdateJob {

    def concurrent = false

    ScmAccessService scmAccessService

    private static final long REPEAT_INTERVAL_MILLISECONDS = 30 * 1000L

    static triggers = {
        simple repeatInterval: REPEAT_INTERVAL_MILLISECONDS
    }

    def execute() {
        time("execution of ProjectUpdateJob") {
            Project.all.each { Project project ->
                update(project)
            }
        }
    }

    def update(Project project) {
        String projectRepositoryUrl = project.url
        Project.withTransaction({ DefaultTransactionStatus ignoredStatus ->
            time("update of project $project.url") {
                scmAccessService.updateProject(projectRepositoryUrl)
                if (project.hasChangesets()) {
                    String lastChangesetHash = project.changesets.sort {it.date}.last().identifier
                    scmAccessService.importNewChangesets(projectRepositoryUrl, lastChangesetHash)
                } else {
                    scmAccessService.importAllChangesets(projectRepositoryUrl)
                }
            }
        })
    }

    def time(String actionName, Closure action) {
        long startTime = System.nanoTime()
        log.info("Starting $actionName")
        try {
            action()
            log.info("Finished $actionName. It completed successfully after ${durationSince(startTime)}")
        } catch (Exception e) {
            log.warn("Finished $actionName. It FAILED after ${durationSince(startTime)}")
            throw e
        }
    }

    private String durationSince(long startTimeNanos) {
        "${(System.nanoTime() - startTimeNanos) / 1000 / 1000 / 1000} seconds."
    }
}
