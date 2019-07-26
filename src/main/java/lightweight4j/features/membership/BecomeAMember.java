package lightweight4j.features.membership;


import an.awesome.pipelinr.Command;
import an.awesome.pipelinr.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;

public class BecomeAMember implements Command<Long> {

    @NotEmpty
    private final String email;

    @NotEmpty
    private final String firstName;

    @NotEmpty
    private final String lastName;

    public BecomeAMember(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @RestController
    private static class HttpEndpoint {

        @Autowired
        private Pipeline pipeline;

        @PostMapping("/members")
        public Long post(@RequestBody BecomeAMember command) {
            return pipeline.send(command);
        }

    }


    @Component
    private static class Handler implements Command.Handler<BecomeAMember, Long> {

        private final Members members;
        private final EmailBlacklist blacklist;

        @Autowired
        Handler(Members members, EmailBlacklist blacklist) {
            this.members = members;
            this.blacklist = blacklist;
        }

        @Override
        public Long handle(BecomeAMember $) {
            var email = new Email($.email);
            if (blacklist.contains(email)) {
                throw new EmailIsBlacklistedException(email);
            }

            var name = new Name($.firstName, $.lastName);
            var member = new Member(name, email);
            members.save(member);
            return member.id();
        }

    }
}

