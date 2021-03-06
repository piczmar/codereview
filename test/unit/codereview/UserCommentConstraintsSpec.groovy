package codereview

import grails.test.mixin.TestFor
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll
import grails.buildtestdata.mixin.Build

@Build(UserComment)
@TestFor(UserComment)
class UserCommentConstraintsSpec extends Specification {

    @Unroll("Field '#field' of class UserComment should have constraint '#constraint' violated by value '#violatingValue'")
    def 'UserComment should have well defined constraints:'() {

        when:
        def userComment = new UserComment("$field": violatingValue)

        then:
        userComment.validate() == false
        userComment.errors.getFieldError(field).code == constraint

        true

        where:
        field    | constraint | violatingValue
        'text'   | 'blank'    | ''
        'text'   | 'nullable' | null
        'author' | 'nullable' | null

    }
    //TODO write a more compound validation test where objects are valid in general and only tested field is incorrect

    def 'should belongTo User'() {
        expect:
        UserComment.belongsTo.author == User
    }

    def "should add UserComment to author's changesetComments upon creation"() {
        given:
        User author = User.build()
        UserComment comment = new UserComment(author, 'asdf')

        expect:
        author.changesetComments.contains(comment)
    }
}

