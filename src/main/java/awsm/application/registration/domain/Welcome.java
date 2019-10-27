package awsm.application.registration.domain;

import static awsm.infrastructure.middleware.ReturnsNothing.NOTHING;

import awsm.infrastructure.middleware.Command;
import awsm.infrastructure.middleware.ReturnsNothing;
import awsm.infrastructure.middleware.impl.execution.Executor;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;

class Welcome implements Command<ReturnsNothing> {

  private final long customerId;

  Welcome(@JsonProperty("customerId") long customerId) {
    this.customerId = customerId;
  }

  @Component
  static class Exe implements Executor<Welcome, ReturnsNothing> {

    private final Customer.Repository customers;

    private Exe(Customer.Repository customers) {
      this.customers = customers;
    }

    @Override
    public ReturnsNothing execute(Welcome cmd) {
      var customerId = new CustomerId(cmd.customerId);
      var customer = customers.singleBy(customerId);
      System.out.printf("Sending email to %s: Welcome to the Matrix, %s", customer.email(), customer.name());
      return NOTHING;
    }
  }
}