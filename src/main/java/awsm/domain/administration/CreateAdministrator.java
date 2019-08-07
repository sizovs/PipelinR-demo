package awsm.domain.administration;

import an.awesome.pipelinr.Voidy;
import awsm.application.commands.Registration;
import awsm.infra.pipeline.ExecutableCommand;
import org.springframework.stereotype.Component;

public class CreateAdministrator extends ExecutableCommand<Voidy> {

  public final Long memberId;

  private CreateAdministrator(Long memberId) {
    this.memberId = memberId;
  }

  @Component
  static class AfterRegistration implements Registration.Transaction {

    @Override
    public void joinTransaction(Long memberId) {
      new CreateAdministrator(memberId).execute();
    }

  }
}
