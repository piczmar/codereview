package codereview

import grails.test.mixin.Mock
import spock.lang.Specification
import testFixture.Fixture
import org.apache.maven.scm.ChangeSet

@Mock([Changeset, ProjectFile])
class ScmAccessServiceSpec extends Specification {

    def "should fetch and save changesets in db"() {    //TODO it's inconsistent with our naming convention
        given:
            def (gitScmUrl, changesetId, commitComment, changesetAuthor)  = [Fixture.PROJECT_REPOSITORY_URL, "id", "comment", "agj@touk.pl"]
            ScmAccessService sas = new ScmAccessService()

            def cs = new ChangeSet(new Date(), commitComment, changesetAuthor, null)
            cs.setRevision(changesetId)
            GitRepositoryService gitRepositoryService = Mock()
            1 * gitRepositoryService.getAllChangeSets(Fixture.PROJECT_REPOSITORY_URL) >> [ cs ]

            sas.gitRepositoryService = gitRepositoryService

        when:
            sas.fetchAllChangesetsWithFilesAndSave(gitScmUrl)

        then:
            //1 * gitRepositoryService.getAllChangeSets(gitScmUrl)
            Changeset.count() == 1
            Changeset.findAllByIdentifierAndAuthor(changesetId, changesetAuthor).size() == 1
    }

    //TODO this testing is incomplete, because service has got many methods and they're aren't tested anywhere - More tests!
}