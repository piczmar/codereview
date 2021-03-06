package codereview

import grails.buildtestdata.mixin.Build
import grails.plugins.springsecurity.SpringSecurityService
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Ignore

@TestFor(UserController)
@Build([User, Commiter])
class UserControllerSpec extends Specification {

    def 'should login newly created user'() {
        given:
        def username = 'agj@touk.pl'
        controller.springSecurityService = Mock(SpringSecurityService)

        when:
        String password = 'dupa.8'
        controller.save(new CreateUserCommand(
                email: username,
                password: password,
                password2: password
        ))

        then:
        1 * controller.springSecurityService.reauthenticate(username)
    }

    @Ignore //will fix after retrospection
    def "should reject 'jil@1' as a wrong email address"() {
        given:
        def username = 'jil@1'
        controller.springSecurityService = Mock(SpringSecurityService)

        when:
        String password = 'dupa.8'
        controller.save(new CreateUserCommand(
                email: username,
                password: password,
                password2: password
        ))

        then:
        view == '/user/create'
        model.command.errors.getFieldError('email').code == 'email.invalid'
    }

    @Ignore //will fix after retrospection
    def "should reject existing user's email as invalid"() {
        given:
        User user = User.build(springSecurityService: Mock(SpringSecurityService))

        when:
        String password = 'dupa.8'
        controller.save(new CreateUserCommand(
                email: user.email,
                password: password,
                password2: password
        ))

        then:
        view == '/user/create'
        model.command.errors.getFieldError('email').code == 'userExists'
    }
}
