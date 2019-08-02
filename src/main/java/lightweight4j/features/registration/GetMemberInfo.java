package lightweight4j.features.registration;

import lightweight4j.lib.pipeline.ExecutableCommand;
import lightweight4j.lib.pipeline.tx.ReadOnly;

public class GetMemberInfo extends ExecutableCommand<GetMemberInfo.MemberInfo> implements ReadOnly {

    public final Long memberId;

    public GetMemberInfo(Long memberId) {
        this.memberId = memberId;
    }

    public static class MemberInfo {

        final String firstName;
        final String lastName;

        public MemberInfo(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }
}
