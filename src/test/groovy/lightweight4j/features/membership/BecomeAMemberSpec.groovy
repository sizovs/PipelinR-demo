package lightweight4j.features.membership

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static groovy.json.JsonOutput.toJson
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class BecomeAMemberSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    def "creates a new member and returns its id"() {
        given:
            def request = [
                    email: 'eduards@sizovs.net',
                    firstName: 'Eduards',
                    lastName: 'Sizovs'
            ]
        when:
            def _ = mockMvc.perform(post("/members").contentType(APPLICATION_JSON).content(toJson(request)))
        then:
        _.andExpect(status().is(200))
        and:
        _.andExpect(content().string("1"))
    }

    def "returns validation error if a first name, last name or email are empty"() {
        given:
        def request = {}
        when:
        def _ = mockMvc.perform(post("/members").contentType(APPLICATION_JSON).content(toJson(request)))
        then:
        _.andExpect(status().is(400))
        and:
        _.andExpect(content().json(toJson([
                [ property: 'email',     message: 'must not be empty' ],
                [ property: 'firstName', message: 'must not be empty' ],
                [ property: 'lastName',  message: 'must not be empty' ]
        ])))
    }

}
