package codereview

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Changeset)
class ChangesetConstraintsSpec extends Specification {

    static String alreadyUsedIdentifier = "alreadyUsedIdentifier"

    def setup() {
        mockForConstraintsTests(Changeset, [new Changeset(alreadyUsedIdentifier, "coding", new Date())])
    }

    @Unroll("Field '#field' of class Changeset should have constraint '#constraint' violated by value '#violatingValue'")
    def "Changeset should have well defined constraints:" () {

        when:
            def changeset = new Changeset("$field": violatingValue)

        then:
            changeset.validate() == false
            changeset.errors[field].toString() == constraint

        where:
            field           | constraint    | violatingValue
            'identifier'    | 'blank'       | ""
            'identifier'    | 'unique'      | alreadyUsedIdentifier
            'identifier'    | 'nullable'    | null
            'commitComment' | 'nullable'    | null
            'date'          | 'nullable'    | null
    }

}
